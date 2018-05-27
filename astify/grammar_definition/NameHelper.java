package astify.grammar_definition;

import java.util.HashSet;
import java.util.Set;

public class NameHelper {
    private Set<String> names = new HashSet<>();

    String getName(String name) {
        if (names.contains(name)) {
            name = "_" + name;

            for (int i = 0; ; ++i) {
                if (!names.contains(name + i)) {
                    name += i;
                    break;
                }
            }
        }

        names.add(name);

        return name;
    }

    void define(String name) {
        names.add(name);
    }

    static String toUpperCamelCase(String name) {
        name = name.replace("_", "-");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        if (name.contains("-")) {
            int index = name.indexOf("-");
            return name.substring(0, index) + toUpperCamelCase(name.substring(index + 1));
        }

        return name;
    }

    static String toLowerCamelCase(String name) {
        name = name.substring(0, 1) + name.substring(1).replace("_", "-");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        if (name.contains("-")) {
            int index = name.indexOf("-");
            return name.substring(0, index) + toUpperCamelCase(name.substring(index + 1));
        }

        return name;
    }

    static String toLowerLispCase(String name) {
        name = name.replace("_", "-");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        for (int i = 1; i < name.length(); ++i) {
            if (Character.isUpperCase(name.charAt(i))) {
                return name.substring(0, i) + "-" + toLowerLispCase(name.substring(i));
            }
        }

        return name;
    }

    static String getGetterName(String name) {
        return "get" + toUpperCamelCase(name);
    }

    static String getBooleanGetterName(String name) {
        return "is" + toUpperCamelCase(name);
    }
}
