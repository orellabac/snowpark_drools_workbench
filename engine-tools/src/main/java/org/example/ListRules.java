package org.example;

import java.util.stream.Stream;
class OutputRules {
    public String rule_name;
    public OutputRules(String rule_name) {
        this.rule_name = rule_name;
    }
}
public class ListRules {

    public ListRules() {

    }

    public static Class getOutputClass() {
      return OutputRules.class;
    }

    public Stream<OutputRules> process() {
        return DroolsConfig.getRules().stream().map(OutputRules::new);
    }

}
