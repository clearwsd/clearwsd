package io.github.clearwsd.corpus.ontonotes;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import lombok.extern.slf4j.Slf4j;


/**
 * OntoNotes sense inventory object factory.
 */
@Slf4j
@XmlRegistry
public class OntoNotesSenseInventoryFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Read a map from lemmas onto corresponding {@link OntoNotesInventory OntoNotes sense inventories} at a given directory.
     *
     * @param directory inventory directory
     * @param extension inventory extension (e.g. ".xml")
     * @return sense inventory map
     */
    public static Map<String, OntoNotesInventory> readInventories(Path directory, String extension) {
        try {
            Map<String, OntoNotesInventory> inventoryMap = Files.walk(directory, Integer.MAX_VALUE)
                    .filter(f -> f.toString().endsWith(extension))
                    .map(path -> {
                        try {
                            return readInventory(Files.newInputStream(path));
                        } catch (Exception e) {
                            throw new RuntimeException("Error reading sense inventory at " + path.toString(), e);
                        }
                    })
                    .collect(Collectors.toMap(OntoNotesInventory::getLemma, i -> i));
            log.debug("Read {} inventories at {}", inventoryMap.size(), directory.toString());
            return inventoryMap;
        } catch (Exception e) {
            throw new RuntimeException("Error reading sense inventories at " + directory.toString(), e);
        }
    }

    /**
     * Read a map from lemmas onto corresponding {@link OntoNotesInventory OntoNotes sense inventories} at a given directory.
     *
     * @param directory inventory directory
     * @return sense inventory map
     */
    public static Map<String, OntoNotesInventory> readInventories(Path directory) {
        return readInventories(directory, ".xml");
    }

    /**
     * Read a single OntoNotes sense inventory.
     *
     * @param inputStream sense inventory input
     * @return sense inventory
     */
    public static OntoNotesInventory readInventory(InputStream inputStream) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            SAXSource source = new SAXSource(parserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
            return (OntoNotesInventory) JAXBContext.newInstance(OntoNotesInventory.class).createUnmarshaller().unmarshal(source);
        } catch (Exception e) {
            throw new RuntimeException("Error reading sense inventory: " + e.getMessage(), e);
        }
    }

}
