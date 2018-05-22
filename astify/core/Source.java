package astify.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class Source {
    public abstract String getName();
    public abstract String getContent();

    public String getLine(int line) {
        String content = getContent();
        int startIndex = 0, endIndex;

        if (content == null) return "";

        for (; line > 1; line--) {
            if ((startIndex = content.indexOf('\n', startIndex) + 1) == 0) {
                startIndex = content.length();
                break;
            }
        }

        if ((endIndex = content.indexOf('\n', startIndex)) == -1) {
            endIndex = content.length();
        }

        return content.substring(startIndex, endIndex);
    }

    public static class FileSource extends Source {
        private final String fileName;
        private final String filePath;

        public FileSource(String path, String name) {
            assert path != null;
            assert name != null;
            filePath = path;
            fileName = name;
        }

        public FileSource(String path) {
            this(path, Paths.get(path).getFileName().toString());
        }

        @Override public String getName() {
            return fileName;
        }

        @Override public String getContent() {
            try {
                return new String(Files.readAllBytes(Paths.get(filePath)));
            }
            catch (IOException e) {
                return null;
            }
        }
    }

    public static class VirtualSource extends Source {
        private final String name;
        private final String content;

        public VirtualSource(String name, String content) {
            assert name != null;
            assert content != null;
            this.name = name;
            this.content = content;
        }

        @Override public String getName() {
            return name;
        }

        @Override public String getContent() {
            return content;
        }
    }

    @Override public String toString() {
        return "<source " + getName() + ">";
    }
}
