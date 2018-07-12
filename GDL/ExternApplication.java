package GDL;

import java.util.List;

class ExternApplication {
    static class Call implements Parameter {
        private final Definition.ExternDefinition extern;
        private final List<Parameter> parameters;

            Call(Definition.ExternDefinition extern, List<Parameter> parameters) {
            this.extern = extern;
            this.parameters = parameters;
        }

        Definition.ExternDefinition getExtern() {
            return extern;
        }

        List<Parameter> getParameters() {
            return parameters;
        }
    }

    static class Reference implements Parameter {
        private final String name;

        Reference(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    interface Parameter {}

    private final List<Pattern> patternList;
    private final Call call;

    ExternApplication(List<Pattern> patternList, Call call) {
        this.patternList = patternList;
        this.call = call;
    }

    List<Pattern> getPatternList() {
        return patternList;
    }

    Call getCall() {
        return call;
    }
}
