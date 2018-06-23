
import astify.GDL.BuildConfig;
import astify.GDL.GDLException;
import astify.GDL.GrammarDefinition;
import astify.ParserException;
import astify.token.TokenException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ASTifyGDL {
    private static final Set<String> options = new HashSet<>();
    private static final Map<String, String> aliases = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            printUsage();
            return;
        }

        Map<String, String> options = getOptions(args);

        if (options == null) return;

        String file = args[0];
        String packageName = options.getOrDefault("package", new File(file).getParent().replace("/", ".").replace("\\", "."));
        String outputPath = options.getOrDefault("output", "");

        BuildConfig config = new BuildConfig(packageName, outputPath);

        if (options.containsKey("access-classes")) {
            config.setClassAccess(options.get("access-classes"));
        }
        if (options.containsKey("access-getters")) {
            config.setGetterAccess(options.get("access-getters"));
        }
        if (options.containsKey("access-constructors")) {
            config.setConstructorAccess(options.get("access-constructors"));
        }
        if (options.containsKey("access-pattern-builder")) {
            config.setPatternBuilderConstructorAccess(options.get("access-pattern-builder"));
        }

        try {
            GrammarDefinition.parseAndBuild(file, config);
        }
        catch (TokenException | ParserException e) {
            System.err.println("Syntax error:");
            System.err.println(e.toString());
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        catch (GDLException e) {
            System.err.println(e.toString());
        }
    }

    private static Map<String, String> getOptions(String[] params) {
        Map<String, String> options = new HashMap<>();
        String currentOption = null;

        for (int i = 1; i < params.length; ++i) {
            if (currentOption == null) {
                String option = params[i];

                if (option.contains("=")) {
                    int index = option.indexOf("=");
                    if (!setOption(options, option.substring(0, index), option.substring(index + 1))) return null;
                }
                else {
                    currentOption = option;
                }
            }
            else {
                if (!setOption(options, currentOption, params[i])) return null;
                currentOption = null;
            }
        }

        if (currentOption != null) {
            System.err.println("Expected value for '" + currentOption + "'");
            return null;
        }

        return options;
    }

    private static boolean setOption(Map<String, String> options, String option, String value) {
        String optionResolved = getOption(option);

        if (optionResolved == null) {
            System.err.println("Unknown option '" + option + "'");
            return false;
        }

        options.put(optionResolved, value);

        return true;
    }

    private static String getOption(String opt) {
        if (opt.charAt(0) == '-') {
            if (opt.charAt(1) == '-') {
                return options.contains(opt.substring(2)) ? opt.substring(2) : null;
            }
            else {
                aliases.get(opt.substring(1));
            }
        }
        return null;
    }

    private static void printUsage() {
        System.out.println("ASTify GDL Usage");
        System.out.println(" astify-gdl <grammar-file> [--package(-p) <output-package>] {options}");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" --access-classes(-ac) (default|protected|public)");
        System.out.println(" --access-getters(-ag) (default|protected|public)");
        System.out.println(" --access-constructors(-an) (default|protected|public)");
        System.out.println(" --access-pattern-builder(-ap) (default|protected|public)");
        System.out.println(" --output(-o) <output-directory>");
    }

    static {
        aliases.put("ac", "access-classes");
        aliases.put("ag", "access-getters");
        aliases.put("an", "access-constructors");
        aliases.put("ap", "access-pattern-builder");
        aliases.put("o", "output");
        aliases.put("p", "package");

        options.addAll(aliases.values());
    }
}
