package com.k.midishapes.midi;

import java.awt.Color;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static Color complementary(Color color) {
        return new HSLColor(color).getComplementary();
    }

}
