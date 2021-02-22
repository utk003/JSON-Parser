package io.github.utk003.json.ooj;

import io.github.utk003.json.ooj.classes.Biome;

import java.io.FileInputStream;

public class FromJSONTest {
    public static void main(String[] args) throws Exception {
        OOJParser parser = new OOJParser();
        Biome.prepareForJSON(parser);
        Biome biome = parser.parseRecursive(new FileInputStream("test/in/ooj.json"), Biome.class);
        System.out.println(biome);
    }
}