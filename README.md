# clear-wsd

ClearWSD is a word sense disambiguation tool for the JVM, with core modules available under an Apache 2.0 license. It provides 
simple APIs for integration with other libraries, as well as a CLI for non-programmatic use. It is modular, allowing for alternative 
implementations of sub-components such as parsers or resources used for feature extraction.

It is meant for use in both research and production settings. Main features include

- State-of-the-art results in verb sense disambiguation over VerbNet classes
- Automatic optimization of feature subsets and hyperparameters
- Production-ready pre-trained models
- Easy retraining/packaging of models


## Setup/Installation

To build ClearWSD, you will need JDK (Java) 8 and [Apache Maven](https://maven.apache.org/).
On Mac/Linux, you can then build the project for CLI use:
```bash
git clone https://github.com/jgung/clear-wsd.git
cd clear-wsd
mvn package
```
Alternatively (or additionally), for use as an API, install the package in your local Maven repo (`~/.m2/repository`), use
```bash
mvn install
```

## API
To make use of the ClearWSD library in its entirety in your project, you can simply add the following dependency:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd</artifactId>
  <version>1.0.0</version>
</dependency>
```

ClearWSD is composed of several modules. To try out ClearWSD in your project with no modifications, it is typically sufficient to
include just two of these, the first being `clear-wsd-core`:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-core</artifactId>
  <version>1.0.0</version>
</dependency>
```
and the second being a parser module, used for pre-processing before word sense disambiguation.

For the Stanford Parser wrapper module (GPL licensed), add the following dependency:
```xml
<dependency>
  <groupId>io.github.clearwsd</groupId>
  <artifactId>clear-wsd-stanford</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Command Line Interface

ClearWSD provides a command-line interface for training, evaluation, and application of word sense disambiguation models.
You can see a help message and available options with the following command (assuming you have already run `mvn package`):
```bash
java -jar clear-wsd-cli-*.jar --help
```

#### Training
To train a new model, you must specify the path to a training data file with `-train`, as well as a path for the resulting saved
model, using `-model`:
```bash
java -jar clear-wsd-cli-*.jar -train path/to/training/file.txt -model path/to/save/model.bin
```

#### Evaluation
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

#### Application
To apply a trained model to new (raw) data, specify a path with `-input`. Optionally specify an output path with `-output`:
```bash
java -jar clear-wsd-cli-*.jar -input path/to/raw/data.txt -output path/to/predictions.txt -model path/to/trained/model.bin
```

#### Interactive Testing
`--loop` or `--itl` can be used to start an interactive command line test loop, where you can input sentences and see predictions.
```bash
java -jar clear-wsd-cli-*.jar --loop -model path/to/saved/model.bin
```
