# ClearWSD

ClearWSD is a word sense disambiguation tool for the JVM, with core modules available under an Apache 2.0 license.
It provides simple APIs for integration with other libraries, as well as a command-line interface (CLI) for non-programmatic use.
It is modular, allowing for alternative implementations of sub-components such as parsers or resources used for feature extraction.

It is meant for use in both research and production settings. Main features include

- State-of-the-art results in verb sense disambiguation over VerbNet classes
- Automatic optimization of feature subsets and hyperparameters
- Production-ready pre-trained models
- Easy training of new models using CLI
- 1000+ sense predictions per second on a 2014 MacBook Pro

## Setup/Installation

To build ClearWSD, you will need JDK (Java) 8 and [Apache Maven](https://maven.apache.org/).
On Mac/Linux, you can then build the project for CLI use:
```bash
git clone https://github.com/jgung/clear-wsd.git
cd clear-wsd
mvn package -P build-nlp4j-cli
```
Alternatively (or additionally), for use as an API, install the package in your local Maven repo (`~/.m2/repository`), use
```bash
mvn install -D skipTests
```

## API
The easiest way to make use of ClearWSD in your project is through [Maven](https://maven.apache.org/), by simply adding corresponding
ClearWSD dependencies to your project's `pom.xml`.

ClearWSD is composed of several modules. To try out ClearWSD in your project with no modifications, it is typically sufficient to
include just two of these, the first being `clear-wsd-core`:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-core</artifactId>
  <version>1.0.0</version>
</dependency>
```
and the second being a parser module, used for pre-processing and feature extraction.
A wrapper for the [NLP4J](https://emorynlp.github.io/nlp4j/) dependency parser is provided:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-nlp4j</artifactId>
  <version>1.0.0</version>
</dependency>
```
For the [Stanford Parser](https://stanfordnlp.github.io/CoreNLP/) wrapper module (GPL licensed), you can instead add the following dependency:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-stanford</artifactId>
  <version>1.0.0</version>
</dependency>
```

If you want to use pre-trained word sense disambiguation models, just add the following:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-models</artifactId>
  <version>1.0.0</version>
</dependency>
```

You can then try out a pre-trained model (from OntoNotes) with the following:
```Java
import java.util.List;

import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.corpus.ontonotes.OntoNotesSense;
import io.github.clearwsd.parser.Nlp4jDependencyParser;

public class Test {
    public static void main(String[] args) {
        Nlp4jDependencyParser parser = new Nlp4jDependencyParser(); // load dependency parser
        DefaultSensePredictor<OntoNotesSense> wsd = DefaultSensePredictor.loadFromResource(
                "models/nlp4j-ontonotes.bin", parser); // load WSD model

        String sentence = "Mary took the bus to school (which " // 8 --> travel by means of
                + "took about 30 minutes), and studiously "     // 3 --> require or necessitate
                + "took notes about the Bolsheviks "            // 2 --> light verb usage
                + "taking over the Winter Palace";              // 9 --> claim or conquer, become in control of

        List<String> tokens = parser.tokenize(sentence); // split sentence into tokens

        // display sense predictions and their definitions
        for (SensePrediction<OntoNotesSense> prediction : wsd.predict(tokens)) {
            System.out.println(prediction.sense().getNumber() + " --> " + prediction.sense().getName());
        }
    }
}
```

## Command Line Interface

ClearWSD provides a command-line interface for training, evaluation, and application of word sense disambiguation models.
You can see a help message and available options with the following command (assuming you have already run `mvn package`):
```bash
java -jar clear-wsd-cli-*.jar --help
```

```text
Usage: WordSenseCLI [options]
  Options:
    -model, -m
      Path to classifier model (for loading or saving)
    -input, -i
      Path to unlabeled input file for new predictions
    -train, -t
      Path to training data (required for training)
    -valid, -dev, -v
      Path to validation data
    -cv, -folds
      Number of cross-validation folds
      Default: 0
    -test
      Path to test data
    --itl, --interactive, --loop
      Start an interactive test session on provided model (after training 
      and/or testing)
      Default: false
    --om
      Output misses on evaluation data in separate files
      Default: false
    --reparse
      Reparse, even if a parsed file of the same name already exists
      Default: false
    --help, --usage
      Display usage
    -corpus
      Training/evaluation corpus type
      Default: Semlink
      Possible Values: [Semeval, Semlink]
    -dataExt
      Extension for training data file (only needed for Semeval XML corpora)
      Default: .data.xml
    -ext
      Parse file extension, appended to input file names to save parses
      Default: .dep
    -inventory, -inv
      Sense inventory
      Possible Values: [VerbNet, WordNet, OntoNotes, Counting]
    -inventoryPath
      Sense inventory path (optional)
    -keyExt
      Extension for sense key file (only needed for Semeval XML corpora)
      Default: .gold.key.txt
    -output, -o
      Path to output file where predictions on the input file are stored
```

#### Training
To train a new model, you must specify the path to a training data file with `-train`, as well as a path for the resulting saved
model, using `-model`:
```bash
java -jar clear-wsd-cli-*.jar -train path/to/training/file.txt -model path/to/save/model.bin
```

The default corpus (`Semlink`) expects files with an instance per line in the following format:
```text
document_id <space> sentence_id <space> token# <space> lemma <space> sense_label <tab> sentence_text
```
`sentence_text` should be a single sentence containing the instance, with tokens separated by spaces:
```text
example.txt 25 3 get comprehend-87.2-1	Oh , I get it .
example.txt 57 2 get get-13.5.1-1	Did you get that part ?
```

#### Evaluation
The CLI provides several modes of evaluation/application. You can perform cross-validation, test on a specific dataset,
apply a trained model to raw text, or try out a model interactively by typing in test sentences.
##### Cross Validation
Specify the number of folds with `-cv`. `-cv 5`, for example, can be used for 5-fold cross validation.:
```bash
java -jar clear-wsd-cli-*.jar -train path/to/training/file.txt -cv 5
```
##### Test Dataset
Specify a test file with `-test`:
```bash
java -jar clear-wsd-cli-*.jar -test path/to/test/file.txt -model path/to/trained/model.bin
```

##### Application
To apply a trained model to new (raw) data, specify a path with `-input`. Optionally specify an output path with `-output`:
```bash
java -jar clear-wsd-cli-*.jar -input path/to/raw/data.txt -output path/to/predictions.txt \
-model clear-wsd-models/src/main/resources/models/ontonotes.bin
```

##### Interactive Testing
`--loop` or `--itl` can be used to start an interactive command line test loop, where you can input sentences and see predictions.
```bash
java -jar clear-wsd-cli-*.jar --loop -model path/to/saved/model.bin
```
After the parser and model finish loading, you should then be able to enter test sentences and see predicted senses:
```text
Enter test input ("EXIT" to quit).
> Call me Ishmael

Call[Dub-29.3]
me
Ishmael

> Call a cab

Call[Get-13.5.1]
a
cab

> Call for change

Call[Order-60]
for
change

```
## License

Please refer to the `LICENSE.txt` in individual modules.