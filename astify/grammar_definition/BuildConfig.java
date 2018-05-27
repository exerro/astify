package astify.grammar_definition;

public class BuildConfig {
    private String path;
    private String _package;

    public BuildConfig(String _package, String path) {
        this._package = _package;
        this.path = path;
    }

    public BuildConfig(String _package) {
        this(_package, "");
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
}
