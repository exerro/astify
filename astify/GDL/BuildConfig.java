package astify.GDL;

import java.nio.file.Paths;

public class BuildConfig {
    private String path;
    private String _package;
    private String classAccess = "default";
    private String constructorAccess = "protected";
    private String getterAccess = "public";
    private String patternBuilderConstructorAccess = "public";

    public BuildConfig(String _package, String path) {
        this._package = _package;
        this.path = path;
    }

    public BuildConfig(String _package, String path, String access) {
        this(_package, path);
        ClassBuilder.validateAccessModifier(access);
        constructorAccess = access;
        getterAccess = access;
    }

    public BuildConfig(String _package) {
        this(_package, "");
    }

    public String getClassAccess() {
        return classAccess;
    }

    public void setClassAccess(String classAccess) {
        this.classAccess = classAccess;
    }

    public String getPatternBuilderConstructorAccess() {
        return patternBuilderConstructorAccess;
    }

    public void setPatternBuilderConstructorAccess(String patternBuilderConstructorAccess) {
        this.patternBuilderConstructorAccess = patternBuilderConstructorAccess;
    }

    public String getConstructorAccess() {
        return constructorAccess;
    }

    public void setConstructorAccess(String constructorAccess) {
        assert ClassBuilder.validateAccessModifier(constructorAccess);
        this.constructorAccess = constructorAccess;
    }

    public String getGetterAccess() {
        return getterAccess;
    }

    public void setGetterAccess(String getterAccess) {
        assert ClassBuilder.validateAccessModifier(getterAccess);
        this.getterAccess = getterAccess;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String _package) {
        this._package = _package;
    }

    public String getFullPath() {
        return path + (!path.equals("") && !_package.equals("") ? "/" : "") + _package.replace('.', Paths.get("a", "b").toString().charAt(1));
    }
}
