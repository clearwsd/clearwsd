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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode(of = "classId")
@Accessors(fluent = true)
public class VnClassId implements Comparable<VnClassId> {

    public static final Pattern VN_ID_PATTERN = Pattern.compile(
            "(?:(?<name>[a-zA-Z-_]+)-)?(?<fullId>(?<rootId>(?<number>\\d+)(?:\\.\\d+)*)(?<subcls>(?:-\\d+)*))");

    private final String classId;
    private final String name;
    private final String rootId;
    private final Integer number;

    private VnClassId(String id) {
        Matcher matcher = VN_ID_PATTERN.matcher(id);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid VerbNet class ID: " + id);
        }
        name = matcher.group("name");
        rootId = matcher.group("rootId");
        classId = matcher.group("fullId");
        number = Integer.parseInt(matcher.group("number"));
    }

    @Override
    public String toString() {
        return name == null ? classId : name + "-" + classId;
    }

    public static VnClassId parse(@NonNull String id) {
        return new VnClassId(id);
    }

    @Override
    public int compareTo(@NonNull VnClassId other) {
        int comparison = Integer.compare(number, other.number);
        if (comparison != 0) {
            return comparison;
        }
        return classId.compareTo(other.classId);
    }
}