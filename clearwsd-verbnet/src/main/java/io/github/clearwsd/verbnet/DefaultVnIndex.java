/*
 * Copyright 2019 James Gung
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

package io.github.clearwsd.verbnet;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Default {@link VnIndex} implementation.
 *
 * @author jamesgung
 */
public class DefaultVnIndex implements VnIndex {

    /**
     * Return the base lemma of a phrasal verb (e.g. "go_ballistic" to "go").
     *
     * @param phrasalVerb phrasal verb
     * @return base lemma
     */
    public static String getBaseForm(@NonNull String phrasalVerb) {
        String[] fields = phrasalVerb.replaceAll("\\s+", "_").split("_");
        return fields[0].toLowerCase();
    }

    @Getter
    @Accessors(fluent = true)
    private List<VnClass> roots;

    private SetMultimap<String, VnClass> lemmaVnMap;
    private SetMultimap<String, WnKey> lemmaWnMap;
    private SetMultimap<WnKey, VnMember> wordNetMemberMap;
    private SetMultimap<String, VnMember> lemmaMemberMap;
    private Map<String, VnClass> senseVnMap;

    public DefaultVnIndex(@NonNull List<VnClass> verbClasses) {
        this.roots = ImmutableList.copyOf(verbClasses);
        lemmaVnMap = HashMultimap.create();
        lemmaWnMap = HashMultimap.create();
        lemmaMemberMap = HashMultimap.create();
        senseVnMap = new HashMap<>();
        wordNetMemberMap = LinkedHashMultimap.create();

        for (VnClass cls : verbClasses) {
            senseVnMap.put(cls.verbNetId().classId(), cls);
            for (VnClass subcls : cls.descendants(true)) {
                for (VnMember member : subcls.members()) {
                    String name = getBaseForm(member.name());
                    lemmaVnMap.put(name, cls);
                    lemmaWnMap.putAll(name, member.wn());
                    lemmaMemberMap.put(name, member);
                    for (WnKey key : member.wn()) {
                        wordNetMemberMap.put(key, member);
                    }
                }
            }
        }
    }

    @Override
    public VnClass getById(@NonNull String id) {

        if (Strings.isNullOrEmpty(id)) {
            return null;
        }

        try {
            VnClassId verbNetId = VnClassId.parse(id);

            VnClass rootClass = senseVnMap.get(verbNetId.rootId());

            if (null == rootClass) {
                return null;
            }

            for (VnClass cls : rootClass.descendants(true)) {
                if (cls.verbNetId().classId().equals(verbNetId.classId())) {
                    return cls;
                }
            }
        } catch (IllegalArgumentException ignored) {
            // just return empty if class is invalid
        }

        return null;

    }

    @Override
    public Set<VnClass> getByLemma(@NonNull String lemma) {
        return lemmaVnMap.get(getBaseForm(lemma));
    }

    @Override
    public Set<VnMember> getMembersByLemma(@NonNull String lemma) {
        return lemmaMemberMap.get(getBaseForm(lemma));
    }

    @Override
    public Set<VnMember> getMembersByWordNetKey(@NonNull WnKey wnKey) {
        return wordNetMemberMap.get(wnKey);
    }

    @Override
    public Set<WnKey> getWordNetKeysByLemma(@NonNull String lemma) {
        return lemmaWnMap.get(getBaseForm(lemma));
    }

}
