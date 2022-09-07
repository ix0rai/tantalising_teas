package io.ix0rai.tantalisingteas.data;

import net.minecraft.util.StringIdentifiable;

public enum TeaColour implements StringIdentifiable {
    VERY_LIGHT_RED(255, 0, 0, 1),
    LIGHT_RED(200, 0, 0, 2),
    RED(128, 0, 0, 2),
    DARK_RED(64, 0, 0, 4),
    VERY_DARK_RED(32, 0, 0, 5),
    VERY_LIGHT_GREEN(0, 255, 0, 10),
    LIGHT_GREEN(0, 200, 0, 9),
    GREEN(0, 128, 0, 9),
    DARK_GREEN(0, 64, 0, 8),
    VERY_DARK_GREEN(0, 32, 0, 7),
    OLIVE(128, 128, 0, 8),
    VERY_LIGHT_BLUE(0, 200, 255, 6),
    LIGHT_BLUE(0, 128, 255, 5),
    BLUE(0, 0, 255, 6),
    DARK_BLUE(0, 0, 128, 5),
    VERY_DARK_BLUE(0, 0, 64, 4),
    CYAN(0, 255, 255, 7),
    TEAL(0, 128, 128, 5),
    YELLOW(255, 255, 0, 3),
    ORANGE(255, 128, 0, 3),
    PINK(255, 0, 255, 3),
    PURPLE(128, 0, 128, 3),
    BLACK(0, 0, 0, 13),
    WHITE(255, 255, 255, 12),
    BROWN(76, 50, 40, 11);

    private final int red;
    private final int green;
    private final int blue;
    private final int priority;

    TeaColour(int r, int g, int b, int priority) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.priority = priority;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getRgbSum() {
        return red + green + blue;
    }

    public int getPriority() {
        return priority;
    }

    public int getTotalDiff(int r, int g, int b) {
        return Math.abs(this.red - r) + Math.abs(this.green - g) + Math.abs(this.blue - b);
    }

    public String getId() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.getId();
    }

    @Override
    public String asString() {
        return this.getId();
    }
}
