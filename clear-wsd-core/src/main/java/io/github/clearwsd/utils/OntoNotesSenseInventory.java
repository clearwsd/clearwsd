package io.github.clearwsd.utils;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.corpus.ontonotes.OntoNotesInventory;
import io.github.clearwsd.corpus.ontonotes.OntoNotesSense;
import io.github.clearwsd.corpus.ontonotes.OntoNotesSenseInventoryFactory;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * OntoNotes groupings-based {@link SenseInventory} implementation.
 *
 * @author jamesgung
 */
@Slf4j
@Setter
@NoArgsConstructor
public class OntoNotesSenseInventory implements SenseInventory, Serializable {

    private static final long serialVersionUID = 3267895989039842451L;

    private Map<String, OntoNotesInventory> inventoryMap = new HashMap<>();

    /**
     * Initialize OntoNotes sense inventory from {@link Path} to directory containing inventory XML files.
     *
     * @param path sense inventories path
     */
    public OntoNotesSenseInventory(Path path) {
        inventoryMap = OntoNotesSenseInventoryFactory.readInventories(path);
    }

    @Override
    public Set<String> senses(String lemma) {
        OntoNotesInventory inventory = inventoryMap.get(lemma);
        if (inventory == null) {
            return new HashSet<>();
        }
        return inventory.getSenses().stream()
                .map(OntoNotesSense::getNumber)
                .collect(Collectors.toSet());
    }

    @Override
    public String defaultSense(String lemma) {
        OntoNotesInventory inventory = inventoryMap.get(lemma);
        if (inventory == null) {
            return SenseInventory.DEFAULT_SENSE;
        }
        return inventory.getSenses().stream().findFirst().map(OntoNotesSense::getNumber).orElse(DEFAULT_SENSE);
    }

    @Override
    public void addSense(String lemma, String sense) {
        OntoNotesInventory inventory = inventoryMap.get(lemma);
        if (inventory == null) {
            log.warn("Unrecognized lemma: {}", lemma);
        } else {
            if (!senses(lemma).contains(sense)) {
                log.warn("Unrecognized sense: {}", sense);
            }
        }
    }

}
