package io.github.clearwsd.corpus.semlink;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.ImmutableSet.of;

/**
 * Utilities for reading VerbNet annotations.
 *
 * @author jamesgung
 */
@Slf4j
public final class VerbNetCorpusUtils {

    private VerbNetCorpusUtils() {

    }

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class Annotation {
        private String path;
        private int sentence;
        private int token;
        private String lemma;
        private String label;

        public static Annotation of(String line) {
            String[] fields = line.split(" ");
            if (fields.length != 5) {
                log.warn("Unexpected number of fields in line: {}", line);
            }
            return Annotation.of(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), fields[3], fields[4]);
        }

        @Override
        public String toString() {
            return String.format("%s %d %d %s %s", path, sentence, token, lemma, label);
        }
    }

    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor(staticName = "of")
    public static class AnnotationLine {
        private String tokens;
        private Annotation annotation;

        @Override
        public String toString() {
            return annotation.toString() + "\t" + tokens;
        }
    }


    private static Map<String, Set<String>> LEMMA_MAPPINGS = new HashMap<String, Set<String>>() {
        {
            // SemLink -- many of these are due to unification mappings for nominals
            put("be", of("is", "are", "were", "was", "am", "'re", "'s", "'m", "ai", "s")); // ai -> aint
            put("rise", of("uprising"));
            put("logroll", of("rolled"));
            put("affect", of("effect", "effects"));
            put("wake", of("awoke"));
            put("finetune", of("tuning"));
            put("crowd", of("overcrowding"));
            put("secondguess", of("guessed", "guessing", "guess"));
            put("put", of("output"));
            put("raise", of("fundraising", "fundraise"));
            put("doublecross", of("crossed", "cross"));
            put("shoehorn", of("horn"));
            put("outsmart", of("smart"));
            put("inflate", of("stagflation"));
            put("eat", of("ate"));
            put("pay", of("repayments"));
            put("wrack", of("racked"));
            put("complain", of("grievance"));
            put("namedrop", of("drops"));
            put("catch", of("ketchup"));
            put("shortcircuit", of("circuited"));
            put("massproduce", of("producing"));
            put("crossbreed", of("bred"));
            put("say", of("wad")); // typo

            put("go", of("went", "when", "undergoing", "undergo", "underwent", "undergone")); // when is a typo is BOLT
            put("fear", of("afraid"));

            // DF/SMS
            put("see", of("c"));
            put("know", of("no"));
            put("have", of("'ve", "'s"));
            put("imagine", of("machine"));
            put("follow", of("afollow"));
            put("understand", of("stand"));
            put("engage", of("ingage"));
            // web text
            put("accept", of("excepted"));

        }
    };

    private static final Pattern EMPTY_PATTERN = Pattern.compile("^\\*\\S*|0|\\[\\S+]|<\\S+>$");

    private static String unescapePtb(String token) {
        return token.replaceAll("-LRB-", "(")
                .replaceAll("-RRB-", ")")
                .replaceAll("-LSB-", "[")
                .replaceAll("-RSB-", "]")
                .replaceAll("-LCB-", "{")
                .replaceAll("-RCB-", "}")
                .replaceAll("\\\\/", "/")
                .replaceAll("/\\.", ".")
                .replaceAll("/\\?", "?")
                .replaceAll("/-", "-")
                .replaceAll("``", "\"")
                .replaceAll("''", "\"");
    }

    /**
     * Read SemLink-style annotations from a given path. Annotations are expected to have 5 fields:
     * <p>
     * Path Token Sentence Lemma Sense
     * </p>
     *
     * @param annotationsPath path to annotations file
     * @return map from annotation paths to a multimap from sentence indices to corresponding annotations
     */
    public static Map<String, Multimap<Integer, Annotation>> readAnnotations(Path annotationsPath) {
        log.info("Reading annotations at {}", annotationsPath);
        Map<String, Multimap<Integer, Annotation>> anns = new HashMap<>();
        try (Stream<String> lines = Files.lines(annotationsPath)) {
            lines.forEach(line -> {
                line = line.trim();
                if (line.isEmpty()) {
                    return;
                }
                try {
                    Annotation annotation = Annotation.of(line);
                    Multimap<Integer, Annotation> pathAnns = anns.computeIfAbsent(annotation.path, k -> HashMultimap.create());
                    pathAnns.put(annotation.sentence, annotation);
                } catch (Exception e) {
                    log.warn("Error reading annotation for line: {}", line, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int total = 0;
        for (Multimap<Integer, Annotation> map : anns.values()) {
            total += map.size();
        }
        log.info("Read {} annotations from {}", total, annotationsPath);
        return anns;
    }

    private static Optional<AnnotationLine> processLine(Annotation annotation, String[] tokens) {
        // filter out likely offset errors
        String lemmaToken = tokens[Math.min(annotation.token, tokens.length - 1)].toLowerCase();
        if (annotation.token >= tokens.length ||
                annotation.lemma.toLowerCase().charAt(0) != lemmaToken.charAt(0)) {
            if (!LEMMA_MAPPINGS.containsKey(annotation.lemma)
                    || !LEMMA_MAPPINGS.get(annotation.lemma).contains(lemmaToken.toLowerCase())) {
                return Optional.empty();
            }
        }

        // remove unwanted text, such as disfluency and speaker annotatations, and traces -- correct resulting annotations
        int index = 0;
        int annotationIndex = annotation.token;
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (EMPTY_PATTERN.matcher(token).matches()) {
                if (index < annotation.token) {
                    annotationIndex--;
                }
            } else {
                token = unescapePtb(token);
                result.add(token);
            }
            ++index;
        }

        return Optional.of(AnnotationLine.of(String.join(" ", result),
                Annotation.of(annotation.path(), annotation.sentence(), annotationIndex, annotation.lemma(), annotation.label())));

    }

    private static List<Annotation> processSourceFile(Multimap<Integer, Annotation> pathAnns, List<String> lines,
                                                      List<AnnotationLine> result) {
        int index = 0;
        List<Annotation> failed = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (pathAnns.containsKey(index)) {
                String[] tokens = line.split(" ");
                for (Annotation annotation : pathAnns.get(index)) {
                    Optional<AnnotationLine> processed = processLine(annotation, tokens);
                    if (processed.isPresent()) {
                        result.add(processed.get());
                    } else {
                        failed.add(annotation);
                    }
                }
            }
            index++;
        }
        return failed;
    }

    /**
     * Process annotations at a given path using source text in another directory. Write processed annotations to a file at the
     * output path.
     */
    public static void writeAnnotations(Path annotationsPath, Path sourcePath, Path outPath) {
        Map<String, Multimap<Integer, Annotation>> anns = readAnnotations(annotationsPath);
        log.info("Writing annotations using source text at {} to {}", sourcePath, outPath);

        try (PrintWriter out = new PrintWriter(Files.newOutputStream(outPath))) {
            try (Stream<Path> sourcePaths = Files.find(sourcePath, 999, (p, bfa) -> bfa.isRegularFile())) {
                sourcePaths.forEach(path -> {

                            try {
                                String pathName = path.toString().replaceFirst(sourcePath.toString() + "/", "");
                                if (!anns.containsKey(pathName)) {
                                    return;
                                }
                                Multimap<Integer, Annotation> pathAnns = anns.get(pathName);
                                List<String> lines = Files.readAllLines(path);

                                List<AnnotationLine> result = new ArrayList<>();
                                List<Annotation> failed = processSourceFile(pathAnns, lines, result);

                                // try reading annotations from parse file (should be temporary code, till we fix annotation pointers)
                                if (failed.size() > 0) {
                                    String name = path.getFileName().toString().replaceAll("\\.v\\d+_word", ".parse");
                                    Path parsePath = Paths.get(sourcePath.toString(), "word", name);
                                    if (Files.exists(parsePath)) {
                                        lines = Files.readAllLines(parsePath);
                                        Multimap<Integer, Annotation> failedMap = HashMultimap.create();
                                        for (Annotation fail : failed) {
                                            failedMap.put(fail.sentence, fail);
                                        }

                                        failed = processSourceFile(failedMap, lines, result);
                                    } else {
                                        log.warn("Missing parse file for word file {}", pathName);
                                    }

                                    if (failed.size() > 0) {
                                        log.warn("Failed to process file at {}", pathName);
                                        failed.forEach(System.out::println);
                                    } else {
                                        log.trace("Fixed offset issue by using parse file at {} instead of {}", parsePath,
                                                pathName);
                                    }
                                }

                                for (AnnotationLine line : result) {
                                    out.println(line.toString());
                                }

                                out.flush();

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                );
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        //        String filename = "bolt_new_anns";
        //        String filename = "google_new_anns";
        //        String filename = "ewt_new_anns";
        //        String filename = "semlink1.3";
        String filename = "all-annotated";

        writeAnnotations(
                Paths.get("data/anns/" + filename + ".txt"),
                Paths.get("data"),
                Paths.get("data/anns/" + filename + ".word.txt")
        );
    }

}
