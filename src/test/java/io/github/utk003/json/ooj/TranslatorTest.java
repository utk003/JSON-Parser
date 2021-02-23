package io.github.utk003.json.ooj;

import io.github.utk003.json.ooj.classes.Biome;
import io.github.utk003.json.traditional.JSONParser;
import io.github.utk003.json.traditional.node.JSONValue;

import java.io.FileInputStream;

public class TranslatorTest {
    public static void main(String[] args) throws Exception {
        JSONValue json = JSONParser.parseRecursive(new FileInputStream("test/in/ooj.json"));

        OOJParser parser = new OOJParser();
        Biome.prepareForJSON(parser);

        Biome biome = OOJTranslator.translateRecursive(parser, json, Biome.class);

        System.out.println(json);
        System.out.println(biome);
    }
}
