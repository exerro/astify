package astify.GDL;

class NameHelper {
    static String toUpperCamelCase(String name) {
        name = name.replace("_", "-");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

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

    static String getGetterName(String name, boolean isBoolean) {
        return (isBoolean ? "is" : "get") + toUpperCamelCase(name);
    }
}
