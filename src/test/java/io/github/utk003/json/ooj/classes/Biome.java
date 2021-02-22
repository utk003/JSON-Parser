package io.github.utk003.json.ooj.classes;

import io.github.utk003.json.ooj.OOJParser;

import java.awt.*;

public class Biome {
    public Color color;

    public HeightRange height;
    public Climate climate;
    public Weights weights;

    @Override
    public String toString() {
        return color + "\t" + height + "\t" + climate + "\t" + weights;
    }

    public static void prepareForJSON(OOJParser exchanger) {
        exchanger.storeArrayTransformer(Color.class, Biome.class, "initializeColorFromJSON", int.class, int.class, int.class);
        exchanger.storeArrayTransformer(HeightRange.class, Biome.class, "initializeHeightRangeFromJSON", double.class, double.class);
    }

    private static Color initializeColorFromJSON(int r, int g, int b) {
        return new Color(r, g, b);
    }
    private static HeightRange initializeHeightRangeFromJSON(double low, double high) {
        return new HeightRange(low, high);
    }

    public static class HeightRange {
        public final double LOW, HIGH;

        public HeightRange(double low, double high) {
            LOW = low;
            HIGH = high;
        }

        @Override
        public String toString() {
            return "[" + LOW + "," + HIGH + "]";
        }
    }

    public static class Climate {
        public double temperature, humidity;

        @Override
        public String toString() {
            return "(" + temperature + "," + humidity + ")";
        }
    }

    public static class Weights {
        public double relative, variants;

        @Override
        public String toString() {
            return "{w1=" + relative + ",w2=" + variants + "}";
        }
    }
}
