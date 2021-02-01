import io.github.utk003.json.JSONParser;
import io.github.utk003.json.elements.JSONValue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class JsonElementSearchTest {
    private static final String IN = "test/in/";
    public static void main(String[] args) throws IOException {
        String fileName = "test.json";

        JSONValue json = JSONParser.parseNonRecursive(new FileInputStream(IN + fileName));
        System.out.println("JSON -> " + json);

        System.out.println();

        // manual test.... change search path here ........... vvv
        Collection<JSONValue> elements = json.findElements("a.*[*][*]");
        System.out.println("# Elements Found = " + elements.size());

        System.out.println();

        for (JSONValue element : elements)
            System.out.println(element);
    }
}
