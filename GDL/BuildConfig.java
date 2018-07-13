package GDL;

import java.nio.file.Paths;
import java.security.InvalidParameterException;

public class BuildConfig {
    private String path;
    private String _package;
    private Builder.AccessModifier classAccess = Builder.AccessModifier.Default;
    private Builder.AccessModifier constructorAccess = Builder.AccessModifier.Protected;
    private Builder.AccessModifier getterAccess = Builder.AccessModifier.Public;
    private Builder.AccessModifier patternBuilderConstructorAccess = Builder.AccessModifier.Public;

    public BuildConfig(String _package, String path) {
        this._package = _package;
        this.path = path;
    }

    public BuildConfig(String _package, String path, Builder.AccessModifier access) {
        this(_package, path);
        constructorAccess = access;
        getterAccess = access;
    }

    BuildConfig(String _package) {
        this(_package, "");
    }

    Builder.AccessModifier getClassAccess() {
        return classAccess;
    }

    Builder.AccessModifier getConstructorAccess() {
        return constructorAccess;
    }

    Builder.AccessModifier getGetterAccess() {
        return getterAccess;
    }

    Builder.AccessModifier getPatternBuilderConstructorAccess() {
        return patternBuilderConstructorAccess;
    }

    public void setClassAccess(String classAccess) {
        this.classAccess = getAccessModifier(classAccess);
    }

    public void setPatternBuilderConstructorAccess(String patternBuilderConstructorAccess) {
        this.patternBuilderConstructorAccess = getAccessModifier(patternBuilderConstructorAccess);
    }

    public void setConstructorAccess(String constructorAccess) {
        this.constructorAccess = getAccessModifier(constructorAccess);
    }

    public void setGetterAccess(String getterAccess) {
        this.getterAccess = getAccessModifier(getterAccess);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasPackage() {
        return !_package.equals("");
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String _package) {
        this._package = _package;
    }

    public String getFullPath() {
        char sep = Paths.get("a", "b").toString().charAt(1);
        return path + (!path.equals("") && !_package.equals("") ? "/" : "") + _package.replace('.', sep);
    }

    private static Builder.AccessModifier getAccessModifier(String s) {
        switch (s) {
            case "default": return Builder.AccessModifier.Default;
            case "protected": return Builder.AccessModifier.Protected;
            case "public": return Builder.AccessModifier.Public;
        }

        throw new InvalidParameterException("Invalid access-modifier '" + s + "'");
    }
}
