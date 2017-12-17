package edu.colorado.clear.wsd.corpus;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.corpus.OntoNotesConverter.TreebankTreeNode;

import static org.junit.Assert.assertEquals;

/**
 * {@link OntoNotesConverter} tests.
 *
 * @author jamesgung
 */
public class OntoNotesConverterTest {

    @Test
    public void testParse() throws IOException {
        String ptb = new String(Files.readAllBytes(Paths.get("src/test/resources/ptb_test.txt").toAbsolutePath()));
        String result = OntoNotesConverter.parse(ptb).stream().map(tree -> tree.allChildren().stream()
                .filter(TreebankTreeNode::isLeaf)
                .map(TreebankTreeNode::value)
                .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
        assertEquals("In the summer of 2005 , a picture that people have long been looking forward to *T*-1 started *-2 emerging"
                + " with frequency in various major Hong Kong media .\nWith their unique charm , these well - known cartoon images "
                + "once again caused Hong Kong to be a focus of worldwide attention .", result);
    }

}