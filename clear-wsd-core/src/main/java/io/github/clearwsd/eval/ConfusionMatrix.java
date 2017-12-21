package io.github.clearwsd.eval;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * Confusion matrix implemented as a map of {@link Multiset} predictions.
 *
 * @author jamesgung
 */
public class ConfusionMatrix {

    @Getter
    private Map<String, Multiset<String>> confusions;

    public ConfusionMatrix() {
        confusions = new HashMap<>();
    }

    public void add(String system, String gold, int count) {
        Multiset<String> set = confusions.get(gold);
        if (set == null) {
            set = HashMultiset.create();
            confusions.put(gold, set);
        }
        set.add(system, count);
    }

    public void add(String system, String gold) {
        add(system, gold, 1);
    }

    public int getCount(String system, String gold) {
        Multiset<String> set = confusions.get(gold);
        if (set == null) {
            return 0;
        }
        return set.count(system);
    }

    public int getTotalSystem(String system) {
        int total = 0;
        for (String actual : confusions.keySet()) {
            total += getCount(actual, system);
        }
        return total;
    }

    public int getTotalGold(String gold) {
        int total = 0;
        for (String prediction : confusions.keySet()) {
            total += getCount(prediction, gold);
        }
        return total;
    }

    public void add(ConfusionMatrix matrix) {
        for (Map.Entry<String, Multiset<String>> entry : matrix.getConfusions().entrySet()) {
            for (String val : entry.getValue().elementSet()) {
                add(entry.getKey(), val, entry.getValue().count(val));
            }
        }
    }

    public String toTsv() {
        StringBuilder sb = new StringBuilder();
        // Header Row
        sb.append("\t\tSystem Class\t\n");
        // Predicted Classes Header Row
        sb.append("\t\t");
        List<String> classes = confusions.keySet().stream().sorted().collect(Collectors.toList());
        for (String predicted : classes) {
            sb.append(String.format("%s\t", predicted));
        }
        sb.append("Total Gold\n");
        // Data Rows
        String firstColumnLabel = "Gold Class\t";
        for (String actual : classes) {
            sb.append(firstColumnLabel);
            firstColumnLabel = "\t";
            sb.append(String.format("%s\t", actual));

            for (String predicted : classes) {
                sb.append(getCount(actual, predicted));
                sb.append("\t");
            }
            // Actual Class Totals Column
            sb.append(getTotalGold(actual));
            sb.append("\n");
        }
        // Predicted Class Totals Row
        sb.append("\tTotal System\t");
        for (String predicted : classes) {
            sb.append(getTotalSystem(predicted));
            sb.append("\t");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toTsv();
    }

}
