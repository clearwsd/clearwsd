/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class OntoNotesSenseInventory implements SenseInventory<OntoNotesSense>, Serializable {

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
                .map(sense -> id(inventory.getLemma(), sense.getNumber()))
                .collect(Collectors.toSet());
    }

    @Override
    public String defaultSense(String lemma) {
        OntoNotesInventory inventory = inventoryMap.get(lemma);
        if (inventory == null) {
            return SenseInventory.DEFAULT_SENSE;
        }
        return inventory.getSenses().stream()
                .findFirst()
                .map(sense -> id(inventory.getLemma(), sense.getNumber())).orElse(DEFAULT_SENSE);
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

    private String id(String lemma, String number) {
        return lemma + ":" + number;
    }

    @Override
    public OntoNotesSense getSense(String id) {
        String[] fields = id.split(":");
        String lemma = fields[0];
        String number = fields[1];
        OntoNotesInventory inventory = inventoryMap.get(lemma);
        if (inventory == null) {
            return null;
        }
        return inventory.getSenses()
                .stream()
                .filter(s -> s.getNumber().equals(number))
                .findFirst()
                .orElse(null);
    }

}
