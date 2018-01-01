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

package io.github.clearwsd.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Prediction list.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class Predictions<T> {

    private Function<T, String> displayFunction;
    private Function<T, String> labelFunction;

    @Getter
    private List<Prediction> predictions = new ArrayList<>();
    @Getter
    private Evaluation evaluation = new Evaluation();

    public Predictions(Function<T, String> displayFunction, Function<T, String> labelFunction) {
        this.displayFunction = displayFunction;
        this.labelFunction = labelFunction;
    }

    public Predictions<T> add(T instance, String prediction) {
        String goldLabel = labelFunction.apply(instance);
        evaluation.add(prediction, goldLabel);
        predictions.add(new Prediction(instance, prediction, goldLabel));
        return this;
    }

    public List<Prediction> incorrect() {
        return predictions.stream()
                .filter(p -> !p.prediction.equals(p.gold))
                .collect(Collectors.toList());
    }

    public List<Prediction> correct() {
        return predictions.stream()
                .filter(p -> p.prediction.equals(p.gold))
                .collect(Collectors.toList());
    }

    /**
     * Format input list of predictions.
     *
     * @param predictions list of predictions
     * @param tsv         use TSV format (instead of pretty format)
     * @return formatted prediction string
     */
    public String print(List<Prediction> predictions, boolean tsv) {
        String format;
        if (tsv) {
            format = "%s\t%s\t%s";
        } else {
            int labelLength = Math.max(evaluation.labels().stream()
                    .mapToInt(String::length).max()
                    .orElse(0), 7);
            format = "%-" + labelLength + "s %-" + labelLength + "s %s";
        }
        return String.format(format, "System", "Gold", "Instance\n") + predictions.stream()
                .map(p -> String.format(format, p.prediction, p.gold, displayFunction.apply(p.instance)))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    @AllArgsConstructor
    public class Prediction {
        T instance;
        String prediction;
        String gold;
    }

}
