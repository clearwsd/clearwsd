package io.github.clearwsd.corpus;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import io.github.clearwsd.corpus.OntoNotesConverter.TreebankTreeNode;

import static org.junit.Assert.assertEquals;

/**
 * {@link OntoNotesConverter} tests.
 *
 * @author jamesgung
 */
public class OntoNotesConverterTest {

    private static final String PTB_TEST = "src/test/resources/ptb_test.txt";
    private static final String TOKENIZED = "src/test/resources/ptb_test.tokenized.txt";

    @Test
    public void testParse() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(PTB_TEST)));
        String expected = new String(Files.readAllBytes(Paths.get(TOKENIZED)));
        String result = OntoNotesConverter.parse(input).stream().map(tree -> tree.allChildren().stream()
                .filter(TreebankTreeNode::isLeaf)
                .map(TreebankTreeNode::value)
                .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
        assertEquals(expected, result);
    }

}