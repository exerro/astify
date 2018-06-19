package astify.GDL.support;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static<T> String concatList(List<T> list) {
        List<String> strings = new ArrayList<>();

        for (T elem : list) {
            strings.add(elem == null ? "null" : elem.toString());
        }

        return String.join(", ", strings);
    }
}
