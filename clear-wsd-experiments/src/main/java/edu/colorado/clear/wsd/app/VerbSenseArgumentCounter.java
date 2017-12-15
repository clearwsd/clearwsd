package edu.colorado.clear.wsd.app;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.WordSenseAnnotator;
import edu.colorado.clear.wsd.WordSenseClassifier;
import edu.colorado.clear.wsd.corpus.CorpusReader;
import edu.colorado.clear.wsd.corpus.semlink.VerbNetReader;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.verbnet.DefaultPredicateAnnotator;
import edu.stanford.nlp.util.Comparators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for counting arguments of verbs by sense. Can apply a trained classifier, or read from an existing annotated corpus.
 * Saves to a file-backed DB via MapDB (https://github.com/jankotek/mapdb).
 *
 * @author jamesgung
 */
@Slf4j
public class VerbSenseArgumentCounter {

    @Data
    @AllArgsConstructor
    private static class Argument implements Serializable {
        private static final long serialVersionUID = 2148073117043088204L;
        private String predicate;
        private String sense;
        private String relation;
        private String argument;

        @Override
        public String toString() {
            return String.format("%s\t%s\t%s\t%s", predicate, sense, relation, argument);
        }
    }

    @Getter
    private static class ArgumentGroup {
        String key;
        List<Map.Entry<Argument, Long>> entries;
        long total;

        ArgumentGroup(String key, List<Map.Entry<Argument, Long>> entries) {
            this.key = key;
            this.entries = entries;
            total = entries.stream().mapToLong(Map.Entry::getValue).sum();
        }
    }

    @Getter
    private enum ArgField {
        PREDICATE(Comparator.comparing(a -> a.getKey().predicate), a -> a.getKey().predicate),
        SENSE(Comparator.comparing(a -> a.getKey().sense), a -> a.getKey().sense),
        RELATION(Comparator.comparing(a -> a.getKey().relation), a -> a.getKey().relation),
        ARGUMENT(Comparator.comparing(a -> a.getKey().argument), a -> a.getKey().argument),
        COUNT((a1, a2) -> Long.compare(a2.getValue(), a1.getValue()), a -> Long.toString(a.getValue()));

        Comparator<Map.Entry<Argument, Long>> comparator;
        Function<Map.Entry<Argument, Long>, String> toString;

        ArgField(Comparator<Map.Entry<Argument, Long>> comparator, Function<Map.Entry<Argument, Long>, String> toString) {
            this.comparator = comparator;
            this.toString = toString;
        }
    }

    private static final String COUNTS = "counts";
    private static final String FILES = "files";

    @Parameter(names = {"-corpus", "-c"}, description = "Path to corpus directory or file for argument counting", required = true)
    private String corpusPath;
    @Parameter(names = "-ext", description = "Extension of files in corpus to be processed")
    private String corpusExt = ".dep";
    @Parameter(names = {"-model", "-m"}, description = "Path to word sense classifier model")
    private String modelPath;
    @Parameter(names = "-db", description = "Path to MapDB DB file to persist or restore counts", required = true)
    private String dbPath;
    @Parameter(names = {"-outputDir", "o", "-out"}, description = "Path to output directory")
    private String outputPath;
    @Parameter(names = "-relations", description = "Relations to be included in counts")
    private Set<String> relations = Sets.newHashSet("dobj");
    @Parameter(names = "--update", description = "Update existing DB")
    private boolean update = true;
    @Parameter(names = "--overwrite", description = "Overwrite existing DB")
    private boolean overwrite = false;

    @Parameter(names = "-limit", description = "Maximum number of entries to return in output")
    private int limit = 1000000;
    @Parameter(names = "-sort", description = "Ordered list of fields to sort by in output")
    private List<ArgField> sortFields = Collections.singletonList(ArgField.COUNT);
    @Parameter(names = "-group", description = "Field to group by in output")
    private ArgField groupBy = ArgField.SENSE;
    @Parameter(names = "-print", description = "Ordered list of fields to print in output")
    private List<ArgField> printFields = Arrays.asList(ArgField.PREDICATE, ArgField.ARGUMENT, ArgField.COUNT);

    private CorpusReader<DependencyTree> corpusReader = new VerbNetReader.VerbNetCoNllDepReader();
    private Annotator<DependencyTree> senseAnnotator;
    private DB db;
    private HTreeMap<Argument, Long> countMap;
    private Set<String> processed;

    private VerbSenseArgumentCounter(String... args) {
        JCommander cmd = new JCommander(this);
        cmd.setProgramName(this.getClass().getSimpleName());
        try {
            if (args.length == 0) {
                cmd.usage();
                System.exit(0);
            }
            cmd.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }
        initializeDb();
        initializeAnnotator();
    }

    private void initializeDb() {
        if (!update && !overwrite && new File(dbPath).exists()) {
            throw new RuntimeException("DB already exists at specified location. Please specify '--update' or '--overwrite' "
                    + "to loading existing DB or overwrite. Otherwise, change the path name.");
        }
        db = DBMaker.fileDB(dbPath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .make();
        if (overwrite) {
            boolean deleted = new File(dbPath).delete();
            if (!deleted) {
                throw new RuntimeException("Unable to remove old DB");
            }
            //noinspection unchecked
            countMap = db.<Argument, Long>hashMap(COUNTS, Serializer.JAVA, Serializer.LONG).create();
            processed = db.hashSet(FILES).serializer(Serializer.STRING).create();
        }
        //noinspection unchecked
        countMap = db.<Argument, Long>hashMap(COUNTS, Serializer.JAVA, Serializer.LONG).createOrOpen();
        processed = db.hashSet(FILES).serializer(Serializer.STRING).createOrOpen();
    }

    private void initializeAnnotator() {
        if (modelPath != null) {
            Stopwatch sw = Stopwatch.createStarted();
            log.debug("Loading sense annotator at {}...", modelPath);
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
                WordSenseClassifier classifier = new WordSenseClassifier(ois);
                senseAnnotator = new WordSenseAnnotator(classifier, new DefaultPredicateAnnotator(
                        classifier.predicateDictionary()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to locate model at path " + modelPath, e);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load word sense classifier model: " + e.getMessage(), e);
            }
            log.debug("Loaded sense annotator in {}", sw);
        }
    }

    private List<File> getCorpusFiles() {
        List<File> results = new ArrayList<>();
        try {
            File corpusFile = new File(corpusPath);
            if (corpusFile.isDirectory()) {
                Files.walk(Paths.get(corpusPath))
                        .filter(path -> path.endsWith(corpusExt))
                        .map(Path::toFile)
                        .forEach(results::add);
            } else {
                results.add(corpusFile);
            }
        } catch (IOException e) {
            log.warn("Error reading corpus files at {}", corpusPath, e);
        }
        return results;
    }

    private void run() {
        for (File file : getCorpusFiles()) {
            if (processed.contains(file.getPath())) { // skip already-processed files
                continue;
            }
            List<DependencyTree> instances = readTrees(file);
            if (senseAnnotator != null) {
                instances.parallelStream().forEach(senseAnnotator::annotate);
            }
            instances.forEach(this::process);
            processed.add(file.getPath());
        }
        outputEntries();
        db.close();
    }

    private List<DependencyTree> readTrees(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return corpusReader.readInstances(is);
        } catch (Exception e) {
            log.warn("Error reading file at {}", file.getAbsolutePath(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Update counts for a single dependency tree.
     *
     * @param tree dependency tree
     */
    private void process(DependencyTree tree) {
        for (DepNode depNode : tree.tokens()) {
            String sense = depNode.feature(FeatureType.Sense);
            if (null == sense || sense.isEmpty()) {
                continue;
            }
            String lemma = depNode.feature(FeatureType.Lemma);
            //noinspection SuspiciousMethodCalls
            depNode.children().stream()
                    .filter(child -> relations.contains(child.feature(FeatureType.Dep)))
                    .forEach(child -> {
                        Argument key = new Argument(lemma, sense,
                                child.feature(FeatureType.Dep),
                                child.feature(FeatureType.Lemma));
                        Long result = countMap.getOrDefault(key, 0L);
                        countMap.put(key, result + 1);
                    });
        }
    }

    private String formatted(Map.Entry<Argument, Long> entry, List<ArgField> fields) {
        return String.join("\t", fields.stream()
                .map(f -> f.getToString().apply(entry))
                .collect(Collectors.toList()));
    }

    private void outputEntries() {
        List<Map.Entry<Argument, Long>> counts = countMap.getEntries().stream()
                .sorted((a1, a2) -> Long.compare(a2.getValue(), a1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
        // group entries by provided field
        List<ArgumentGroup> groups = groupBy == null
                ? Collections.singletonList(new ArgumentGroup(COUNTS, counts)) : group(groupBy, counts);
        groups.sort(Comparator.comparingLong(ArgumentGroup::getTotal).reversed());
        for (ArgumentGroup argumentGroup : groups) {
            // sort grouped entries by sort fields
            Comparator<Map.Entry<Argument, Long>> comparator = Comparators.chain(sortFields.stream()
                    .map(ArgField::getComparator)
                    .collect(Collectors.toList()));
            List<Map.Entry<Argument, Long>> sorted = argumentGroup.getEntries().stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
            // print entries with provided formatter
            System.out.println(argumentGroup.getKey()); // TODO: provide options to output to files
            for (Map.Entry<Argument, Long> entry : sorted) {
                System.out.println(formatted(entry, printFields));
            }
            System.out.println();
        }
    }

    private List<ArgumentGroup> group(ArgField field, List<Map.Entry<Argument, Long>> counts) {
        ListMultimap<String, Map.Entry<Argument, Long>> index = Multimaps.index(counts, entry -> field.getToString().apply(entry));
        return index.asMap().entrySet().stream()
                .map(e -> new ArgumentGroup(e.getKey(), (List<Map.Entry<Argument, Long>>) e.getValue()))
                .collect(Collectors.toList());
    }

    public static void main(String... args) {
        new VerbSenseArgumentCounter(args).run();
    }

}
