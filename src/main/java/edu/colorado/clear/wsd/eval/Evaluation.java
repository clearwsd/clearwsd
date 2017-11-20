package edu.colorado.clear.wsd.eval;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Evaluation statistics.
 *
 * @author jamesgung
 */
public class Evaluation {

    private Multiset<String> systemCounts;
    private Multiset<String> goldCounts;
    private Multiset<String> correctCounts;

    @Getter
    private ConfusionMatrix matrix;

    public Evaluation() {
        this.matrix = new ConfusionMatrix();
        systemCounts = HashMultiset.create();
        goldCounts = HashMultiset.create();
        correctCounts = HashMultiset.create();
    }

    public void add(String system, String gold) {
        add(system, gold, 1);
    }

    public void add(String system, String gold, int count) {
        matrix.add(system, gold, count);
        systemCounts.add(system, count);
        goldCounts.add(gold, count);
        if (system.equals(gold)) {
            correctCounts.add(system, count);
        }
    }

    public void add(Evaluation evaluation) {
        matrix.add(evaluation.getMatrix());
        for (String value : evaluation.systemCounts.elementSet()) {
            systemCounts.add(value, evaluation.systemCounts.count(value));
        }
        for (String value : evaluation.goldCounts.elementSet()) {
            goldCounts.add(value, evaluation.goldCounts.count(value));
        }
        for (String value : evaluation.correctCounts.elementSet()) {
            correctCounts.add(value, evaluation.correctCounts.count(value));
        }
    }

    public int countCorrect(String value) {
        return correctCounts.count(value);
    }

    public int countPredictions(String value) {
        return systemCounts.count(value);
    }

    public int countGold(String value) {
        return goldCounts.count(value);
    }

    public int countCorrect() {
        return correctCounts.size();
    }

    public int countPredictions() {
        return systemCounts.size();
    }

    public int countGold() {
        return goldCounts.size();
    }

    public double precision() {
        int count = countPredictions();
        return count == 0 ? 1.0 : (double) countCorrect() / count;
    }

    public double precision(String value) {
        int count = countPredictions(value);
        return count == 0 ? 1.0 : (double) countCorrect(value) / count;
    }

    public double recall() {
        int count = countGold();
        return count == 0 ? 1.0 : (double) countCorrect() / count;
    }

    public double recall(String value) {
        int count = countGold(value);
        return count == 0 ? 1.0 : (double) countCorrect(value) / count;
    }

    public double fb(double beta) {
        double precision = this.precision();
        double recall = this.recall();
        double numerator = precision * recall * (1d + beta * beta);
        double denominator = (beta * beta * precision) + recall;
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }

    public double fb(double beta, String outcome) {
        double precision = this.precision(outcome);
        double recall = this.recall(outcome);
        double numerator = (1d + beta * beta) * precision * recall;
        double denominator = (beta * beta * precision) + recall;
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }

    public double f1() {
        return fb(1d);
    }

    public double f1(String outcome) {
        return fb(1d, outcome);
    }

    private int labelLength() {
        return Sets.union(goldCounts.elementSet(), systemCounts.elementSet()).stream()
                .map(Object::toString)
                .sorted((s1, s2) -> Integer.compare(s2.length(), s1.length()))
                .findFirst().orElse("").length();
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("##.####");
        StringBuilder sb = new StringBuilder();
        String formatter = "%" + (Math.max(7, labelLength())) + "s  %7s  %7s  %7s  %12s  %12s  %12s\n";
        String heading = String.format(formatter, "", "Correct", "System", "Gold", "Precision", "Recall", "F-Measure");
        String line = String.join("", Collections.nCopies(heading.length() - 1, "-")) + "\n";
        sb.append(heading);
        sb.append(line);
        sb.append(String.format(formatter,
                "Overall",
                this.correctCounts.size(),
                this.systemCounts.size(),
                this.goldCounts.size(),
                df.format(this.precision()),
                df.format(this.recall()),
                df.format(this.f1())));
        List<String> outcomes = new ArrayList<>(Sets.union(goldCounts.elementSet(), systemCounts.elementSet()));
        if (outcomes.size() > 1) {
            Collections.sort(outcomes);
            for (String outcome : outcomes) {
                sb.append(String.format(formatter,
                        outcome,
                        this.correctCounts.count(outcome),
                        this.systemCounts.count(outcome),
                        this.goldCounts.count(outcome),
                        df.format(this.precision(outcome)),
                        df.format(this.recall(outcome)),
                        df.format(this.f1(outcome))));
            }
        }
        return sb.toString();
    }

}
