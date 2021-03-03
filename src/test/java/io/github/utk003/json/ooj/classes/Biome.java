package io.github.utk003.json.ooj.classes;

import io.github.utk003.json.ooj.OOJParser;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Biome {
    public final Color RENDER_COLOR;

    public final HeightRange GENERATION_HEIGHT_RANGE;
    public final Climate BIOME_CLIMATE;
    public final Weights GENERATION_WEIGHTS;

    public Biome(Color color, HeightRange heightRange, Climate climate, Weights weights) {
        RENDER_COLOR = color;
        GENERATION_HEIGHT_RANGE = heightRange;
        BIOME_CLIMATE = climate;
        GENERATION_WEIGHTS = weights;
    }

    @Override
    public String toString() {
        return RENDER_COLOR + "\t" + GENERATION_HEIGHT_RANGE + "\t" + BIOME_CLIMATE + "\t" + GENERATION_WEIGHTS;
    }

    public static void prepareForJSON(OOJParser parser) throws NoSuchMethodException {
        Map<String, Class<?>> fieldTypesMap = new HashMap<>();
        {
            fieldTypesMap.put("color", Color.class);
            fieldTypesMap.put("height", HeightRange.class);
            fieldTypesMap.put("climate", Climate.class);
            fieldTypesMap.put("weights", Weights.class);
        }
        parser.storeObjectTransformerConstructor(Biome.class, fieldTypesMap, new String[]{"color", "height", "climate", "weights"});

        parser.storeArrayTransformerConstructor(Color.class, new Class<?>[]{int.class, int.class, int.class}, false);
        parser.storeArrayTransformerConstructor(HeightRange.class, new Class<?>[]{int.class, int.class}, false);
    }

    public static class HeightRange {
        public final int LOW, HIGH;

        public HeightRange(int low, int high) {
            LOW = low;
            HIGH = high;
        }

        @Override
        public String toString() {
            return "[" + LOW + "," + HIGH + "]";
        }
    }

    public static class Climate {
        public final double temperature, humidity;
        public Climate() {
            temperature = humidity = Double.NaN;
        }

        @Override
        public String toString() {
            return "(" + temperature + "," + humidity + ")";
        }
    }

    public static class Weights {
        public final double relative, variants;
        public Weights() {
            relative = variants = Double.NaN;
        }

        @Override
        public String toString() {
            return "{w1=" + relative + ",w2=" + variants + "}";
        }
    }
}
