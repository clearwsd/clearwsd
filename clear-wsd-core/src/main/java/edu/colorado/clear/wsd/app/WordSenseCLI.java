package edu.colorado.clear.wsd.app;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.SenseDisambiguatingParser;
import edu.colorado.clear.wsd.WordSenseAnnotator;
import edu.colorado.clear.wsd.WordSenseClassifier;
import edu.colorado.clear.wsd.corpus.CoNllDepTreeReader;
import edu.colorado.clear.wsd.corpus.CorpusReader;
import edu.colorado.clear.wsd.corpus.TextCorpusReader;
import edu.colorado.clear.wsd.corpus.semeval.ParsingSemevalReader;
import edu.colorado.clear.wsd.corpus.semeval.SemevalReader;
import edu.colorado.clear.wsd.corpus.semlink.ParsingSemlinkReader;
import edu.colorado.clear.wsd.corpus.semlink.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.eval.Predictions;
import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.WhitespaceTokenizer;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.utils.CountingSenseInventory;
import edu.colorado.clear.wsd.utils.InteractiveTestLoop;
import edu.colorado.clear.wsd.utils.LemmaDictionary;
import edu.colorado.clear.wsd.utils.SenseInventory;
import edu.colorado.clear.wsd.utils.WordNetSenseInventory;
import edu.colorado.clear.wsd.verbnet.DefaultPredicateAnnotator;
import edu.colorado.clear.wsd.verbnet.DefaultVerbNetClassifier;
import edu.colorado.clear.wsd.verbnet.VerbNetSenseInventory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.app.WordSenseCLI.SenseInventoryType.VerbNet;
import static edu.colorado.clear.wsd.app.WordSenseCLI.SenseInventoryType.WordNet;
import static edu.colorado.clear.wsd.type.FeatureType.Gold;
import static edu.colorado.clear.wsd.type.FeatureType.Sense;
import static edu.colorado.clear.wsd.type.FeatureType.Text;

/**
 * Command line interface for training, evaluating and applying a word sense classifier.
 *
 * @author jamesgung
 */
@Slf4j
public abstract class WordSenseCLI {

    public enum SenseInventoryType {
        VerbNet(VerbNetSenseInventory::new),
        WordNet(WordNetSenseInventory::new),
        Counting(CountingSenseInventory::new);
        private Supplier<SenseInventory> senseInventory;

        SenseInventoryType(Supplier<SenseInventory> senseInventory) {
            this.senseInventory = senseInventory;
        }

        public SenseInventory senseInventory() {
            return senseInventory.get();
        }

    }

    public enum CorpusType {

        Semeval(ParsingSemevalReader::new, SemevalReader::new, WordNet),
        Semlink((unused, parser) -> new ParsingSemlinkReader(parser), (unused) -> new VerbNetReader(), VerbNet);

        private BiFunction<String, DependencyParser, CorpusReader<FocusInstance<DepNode, DependencyTree>>> corpusParser;
        private Function<String, CorpusReader<FocusInstance<DepNode, DependencyTree>>> corpusReader;
        @Getter
        private SenseInventoryType defaultInventory;

        CorpusType(BiFunction<String, DependencyParser, CorpusReader<FocusInstance<DepNode, DependencyTree>>> corpusParser,
                   Function<String, CorpusReader<FocusInstance<DepNode, DependencyTree>>> corpusReader, SenseInventoryType type) {
            this.corpusParser = corpusParser;
            this.corpusReader = corpusReader;
            this.defaultInventory = type;
        }

        /**
         * Return a {@link CorpusReader} that parses the input corpus while reading it.
         *
         * @param path      key path
         * @param depParser dependency parser used to parse the input corpus
         * @return parsing {@link CorpusReader}
         */
        public CorpusReader<FocusInstance<DepNode, DependencyTree>> corpusParser(String path, DependencyParser depParser) {
            return corpusParser.apply(path, depParser);
        }

        /**
         * Return a {@link CorpusReader} that reads a pre-parsed corpus, avoiding the need to re-parse.
         *
         * @param path key path
         * @return {@link CorpusReader} for reading a pre-parsed corpus
         */
        public CorpusReader<FocusInstance<DepNode, DependencyTree>> corpusReader(String path) {
            return corpusReader.apply(path);
        }

    }

    private final String helpMessage = WordSenseCLI.class.getSimpleName()
            + " can be used to train, evaluate, or apply a word sense classifier on provided data. \n" +
            " 1. In order to train the classifier, a path to a training data file must be provided, using '-train':" +
            "\n\t-train path/to/training/data.txt\n 2. You can also save a model to a " +
            "specific path with \"-model path/to/saved/model.bin\".\n 3. To evaluate, you must provide a test file:\n" +
            "\t-test path/to/test/data/txt -model path/to/saved/model.bin\n 4. In order to " +
            "apply the classifier to new data, use the \"-input\" option with an input file:\n" +
            "\t-input path/to/raw/data.txt -model path/to/saved/model.bin\n 5. You can perform k-fold " +
            "cross-validation using the \"-cv\" option, e.g. \"-cv 5\" for 5-fold cross-validation.\n 6. You can " +
            "start an interactive test loop to try how various inputs are classified using \"--itl\".\n" +
            " 7. Note that all of these options can be combined, so it is possible to train, test, apply, and " +
            "interactively test in a single command:\n\t-train path/to/training/data.txt -test " +
            "path/to/test/data.txt -apply path/to/input/data.txt --itl\n";

    @Parameter(names = {"-model", "-m"}, description = "Path to classifier model for loading and/or saving", order = 0)
    private String modelPath;

    @Parameter(names = {"-input", "-i"}, description = "Path to unlabeled input file for new predictions", order = 1)
    private String inputPath;

    @Parameter(names = {"-output", "-o"}, description = "Path to output file where predictions on the input file are stored (optional)")
    private String outputPath;
    @Parameter(names = "--reparse", description = "Reparse, even if a parsed file of the same name already exists (false by default)")
    private Boolean reparse = false;
    @Parameter(names = "-ext", description = "Parse file extension")
    private String parseSuffix = ".dep";

    @Parameter(names = "--om", description = "Output misses on evaluation data in separate files")
    private Boolean outputMisses = false;

    @Parameter(names = {"-train", "-t"}, description = "Path to training data file (required for training)", order = 2)
    private String trainPath;
    @Parameter(names = {"-valid", "-dev", "-v"}, description = "Path to validation data file (recommended for training)", order = 3)
    private String validPath;

    @Parameter(names = "-seed", description = "Random seed for cross-validation fold selection", hidden = true)
    private Integer seed = 0;
    @Parameter(names = {"-cv", "-folds"}, description = "Number of cross validation folds to sample", order = 5)
    private Integer folds = 0;
    @Parameter(names = "-per", description = "Percentage of instances to use for training in each fold, if using stratified "
            + "sampling cross-validation", hidden = true)
    private Double trainPer = 0.8;

    @Parameter(names = "-test", description = "Path to testing data file", order = 6)
    private String testPath;

    @Parameter(names = {"--itl", "--interactive", "--loop"}, description = "Start an interactive testing session on provided model "
            + "(after training/testing)", order = 7)
    private Boolean itl = false;

    @Parameter(names = {"--help", "--usage"}, description = "Display usage", help = true)
    private Boolean help = false;

    @Parameter(names = {"-corpus", "-corpusType"}, description = "Training/evaluation corpus type")
    private CorpusType corpusType = CorpusType.Semlink;
    @Parameter(names = "-keyExt", description = "Extension for sense key file (only needed for Semeval XML corpora)")
    private String keyExt = ".gold.key.txt";
    @Parameter(names = "-dataExt", description = "Extension for training data file (only needed for Semeval XML corpora)")
    private String dataExt = ".data.xml";

    @Parameter(names = {"-inventory", "-inv"}, description = "Sense inventory")
    private SenseInventoryType senseInventory;

    private WordSenseClassifier classifier;
    protected DependencyParser parser;
    private Pattern depPattern;

    private JCommander cmd;

    WordSenseCLI(String[] args) {
        cmd = new JCommander(this);
        cmd.setProgramName(this.getClass().getSimpleName());
        try {
            if (help || args.length == 0) {
                System.out.println(helpMessage);
                cmd.usage();
                System.exit(0);
            }
            cmd.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }
    }

    protected abstract DependencyParser parser();

    public void run() {
        try {
            checkParameters();     // (1) validate parameters
            crossValidate();       // (2) perform cross validation
            train();               // (3) train with training data and validation data
            evaluate();            // (4) evaluate on test data
            apply();               // (5) apply model to input data
            interactiveTestLoop(); // (6) test in interactive loop
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            cmd.usage();
        }
    }

    private void checkParameters() {
        depPattern = Pattern.compile("\\" + parseSuffix + "$");
        if (modelPath == null) {
            if (trainPath == null) {
                throw new IllegalStateException(
                        "Must specify a model (e.g. using \"-model path/to/model.bin\"), or provide training data "
                                + "(e.g. using \"-train path/to/training/data.txt\").");
            }
            modelPath = trainPath + ".bin";
            log.warn("No model path specified, saving to {} instead.", modelPath);
        }
        if (trainPath == null && !itl && testPath == null && folds == 0) {
            System.out.println(helpMessage);
            cmd.usage();
            System.exit(0);
        }
        senseInventory = senseInventory == null ? corpusType.getDefaultInventory() : senseInventory;
        trainPath = validatePath(trainPath);
        validPath = validatePath(validPath);
        testPath = validatePath(testPath);
        inputPath = validatePath(inputPath);
        outputPath = validatePath(outputPath);
        modelPath = new File(modelPath).getAbsolutePath();
        File modelFile = new File(modelPath);
        if (modelFile.getParentFile() != null && !modelFile.getParentFile().exists()) {
            if (!modelFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Unable to create model at path " + modelPath);
            }
        }
    }

    private String validatePath(String path) {
        if (path == null) {
            return null;
        }
        if (!reparse) {
            File depFile = new File(path + parseSuffix);
            if (depFile.exists()) {
                return depFile.getAbsolutePath();
            }
            return path;
        }
        return depPattern.matcher(path).replaceAll("");
    }

    private String getKeyPath(String dataPath) {
        return dataPath.replaceAll(dataExt + ".*", "") + keyExt;
    }

    private boolean parsed(String path) {
        return depPattern.asPredicate().test(path);
    }

    private void train() {
        if (trainPath == null) {
            return;
        }
        List<FocusInstance<DepNode, DependencyTree>> trainInstances = getParseTrees(trainPath, parsed(trainPath)
                ? corpusType.corpusReader(getKeyPath(trainPath)) : corpusType.corpusParser(getKeyPath(trainPath), getParser()));
        List<FocusInstance<DepNode, DependencyTree>> validInstances = validPath == null ? new ArrayList<>()
                : getParseTrees(validPath, parsed(validPath) ? corpusType.corpusReader(getKeyPath(validPath))
                : corpusType.corpusParser(getKeyPath(validPath), getParser()));
        classifier = newClassifier();
        log.debug("Training classifier on {} instances from corpus at {}", trainInstances.size(), trainPath);
        classifier.train(trainInstances, validInstances);
        if (validInstances.size() > 0) {
            evaluate(validInstances, validPath);
        }
        saveClassifier();
    }

    private void crossValidate() {
        if (folds <= 0) {
            return;
        }
        Preconditions.checkState(trainPath != null,
                "Specified %s-fold cross validation, but did not provide training instances. "
                        + "Please include the train input option as well, e.g. \"-train path/to/train.txt\"", folds);
        Preconditions.checkState(trainPer < 1 && trainPer > 0,
                "Percentage of training data must be between 0 and 1 (got %f). "
                        + "Please set ratio of percentage of training instances per fold (e.g. \"-per 0.8\")", trainPer);
        List<FocusInstance<DepNode, DependencyTree>> trainInstances = getParseTrees(trainPath, parsed(trainPath)
                ? corpusType.corpusReader(getKeyPath(trainPath)) : corpusType.corpusParser(getKeyPath(trainPath), getParser()));
        log.info("Performing {}-fold cross validation on {} instances in training corpus at {}", folds,
                trainInstances.size(), trainPath);
        CrossValidation<FocusInstance<DepNode, DependencyTree>> cv = new CrossValidation<>(seed, i -> i.feature(FeatureType.Gold));
        List<Evaluation> evaluations = cv.crossValidate(newClassifier(), cv.createFolds(trainInstances, folds, trainPer));
        int index = 0;
        for (Evaluation evaluation : evaluations) {
            log.info("Fold {} results:\n{}", index++, evaluation);
        }
        log.info("Overall {}-fold cross validation results on corpus at {}:\n{}", folds, trainPath, new Evaluation(evaluations));
    }

    private void evaluate() {
        if (testPath == null) {
            return;
        }
        if (classifier == null) {
            classifier = loadClassifier();
        }
        List<FocusInstance<DepNode, DependencyTree>> testInstances = getParseTrees(testPath, parsed(testPath)
                ? corpusType.corpusReader(getKeyPath(testPath)) : corpusType.corpusParser(getKeyPath(testPath), getParser()));
        evaluate(testInstances, testPath);
    }

    private void evaluate(List<FocusInstance<DepNode, DependencyTree>> instances, String path) {
        log.info("Evaluating word sense classifier at {} on {} instances in corpus at {}", modelPath, instances.size(), path);
        Predictions<FocusInstance<DepNode, DependencyTree>> predictions = new Predictions<>(
                instance -> instance.sequence().tokens().stream()
                        .map(token -> token == instance.focus() ? "{" + token.feature(Text) + "}" : token.feature(Text))
                        .collect(Collectors.joining(" ")), instance -> instance.feature(FeatureType.Gold));
        for (FocusInstance<DepNode, DependencyTree> instance : instances) {
            String prediction = classifier.classify(instance);
            // multiple acceptable gold senses per word, if available
            Set<String> allPredictions = instance.feature(FeatureType.AllSenses);
            if (allPredictions != null && allPredictions.size() > 1 && allPredictions.contains(prediction)) {
                instance.addFeature(Gold, prediction);
            }
            predictions.add(instance, prediction);
        }
        if (outputMisses && predictions.incorrect().size() > 0) {
            String missesFile = new File(path).getAbsolutePath() + ".misses.txt";
            log.info("Writing missed predictions to {}", missesFile);
            try {
                Files.write(Paths.get(missesFile), predictions.print(predictions.incorrect(), false)
                        .getBytes(Charset.defaultCharset()));
            } catch (IOException e) {
                log.warn("An error occurred while writing misses to {}:", missesFile, e);
            }
        }
        log.info("Results on test corpus at {}:\n{}", path, predictions.evaluation());
    }

    private void apply() {
        if (inputPath == null) {
            return;
        }
        if (outputPath == null) {
            outputPath = inputPath + ".vn.txt";
            log.warn("No output path provided, saving predictions to {}", outputPath);
        }
        WordSenseAnnotator annotator = getAnnotator();
        List<DependencyTree> instances = getParseTrees(inputPath,
                parsed(inputPath) ? new CoNllDepTreeReader() : new TextCorpusReader(getParser()));
        log.info("Applying word sense annotator to {} instances", modelPath, instances.size());
        instances.parallelStream().forEach(annotator::annotate);
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            new ParsingSemlinkReader(getParser(), new WhitespaceTokenizer())
                    .writeInstances(ParsingSemlinkReader.getFocusInstances(instances), fos);
        } catch (IOException e) {
            log.warn("An error occurred while writing results to {}:", outputPath, e);
        }
    }

    private void interactiveTestLoop() {
        if (!itl) {
            return;
        }
        DependencyParser parser = new SenseDisambiguatingParser(getAnnotator(), getParser());
        InteractiveTestLoop.test(parser, Sense.name());
    }

    private DependencyParser getParser() {
        if (parser == null) {
            log.debug("Initializing parser...");
            Stopwatch sw = Stopwatch.createStarted();
            parser = parser();
            log.debug("Initialized parser in {}", sw);
        }
        return parser;
    }

    private WordSenseAnnotator getAnnotator() {
        if (classifier == null) {
            classifier = loadClassifier();
        }
        return new WordSenseAnnotator(classifier, new DefaultPredicateAnnotator(classifier.predicateDictionary()));
    }

    private WordSenseClassifier newClassifier() {
        return new WordSenseClassifier(new DefaultVerbNetClassifier(),
                senseInventory.senseInventory(), new LemmaDictionary());
    }

    private WordSenseClassifier loadClassifier() {
        log.debug("Loading saved classifier model from {}", modelPath);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return new WordSenseClassifier(ois);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to locate model at path " + modelPath, e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load word sense classifier model: " + e.getMessage(), e);
        }
    }

    private void saveClassifier() {
        log.debug("Saving trained classifier model to {}", modelPath);
        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(modelPath))) {
            classifier.save(ois);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to save model to path " + modelPath, e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to save word sense classifier model: " + e.getMessage(), e);
        }
    }

    private <T extends NlpInstance> List<T> getParseTrees(String path, CorpusReader<T> reader) {
        return parseSafe(path, reader, reparse && !parsed(path));
    }

    private <T extends NlpInstance> List<T> parseSafe(String inputPath, CorpusReader<T> reader, boolean save) {
        try (InputStream inputStream = new FileInputStream(inputPath)) {
            List<T> instances = reader.readInstances(inputStream);
            if (save) {
                String outputFilePath = new File(inputPath + parseSuffix).getAbsolutePath();
                try (OutputStream outputStream = new FileOutputStream(outputFilePath)) {
                    log.debug("Saving parsed instances to {}", outputFilePath);
                    reader.writeInstances(instances, outputStream);
                } catch (Exception e) {
                    log.warn("Unable to save parsed instances", e);
                }
            }
            return instances;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to locate input file at " + inputPath, e);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing file at " + inputPath, e);
        }
    }

}
