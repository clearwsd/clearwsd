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
public class VerbNetId implements Comparable<VerbNetId> {

    public static final Pattern VN_ID_PATTERN = Pattern.compile(
        "(?:(?<name>[a-zA-Z-_]+)-)?(?<rootId>(?<fullId>(?<number>\\d+)(?:\\.\\d+)*)(?<subcls>(?:-\\d+)*))");

    private final String classId;
    private final String name;
    private final String rootId;
    private final Integer number;

    private VerbNetId(String id) {
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

    public static VerbNetId parse(@NonNull String id) {
        return new VerbNetId(id);
    }

    @Override
    public int compareTo(@NonNull VerbNetId other) {
        int comparison = Integer.compare(number, other.number);
        if (comparison != 0) {
            return comparison;
        }
        return classId.compareTo(other.classId);
    }
}