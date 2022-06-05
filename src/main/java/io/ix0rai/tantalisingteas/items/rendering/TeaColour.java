package io.ix0rai.tantalisingteas.items.rendering;

public enum TeaColour {
    // greys
    BLACK(0, 0, 0, 13),
    WHITE(255, 255, 255, 12),
    BROWN(165, 42, 42, 11),

    // reds
    RED(255, 0, 0, 1),
    MAROON(128, 0, 0, 2),

    // greens
    LIME(0, 255, 0, 10),
    GREEN(0, 128, 0, 9),
    OLIVE(128, 128, 0, 8),

    // blues
    CYAN(0, 255, 255, 7),
    BLUE(0, 0, 255, 6),
    TEAL(0, 128, 128, 5),
    NAVY(0, 0, 128, 4),

    // misc
    PURPLE(128, 0, 128, 3),
    MAGENTA(255, 0, 255, 3);

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

    public static TeaColour getHighestPriority(TeaColour[] colours) {
        TeaColour highestPriority = BLACK;

        for (TeaColour colour : colours) {
            if (colour.priority > highestPriority.priority) {
                highestPriority = colour;
            }
        }

        return highestPriority;
    }

    public static TeaColour getClosest(int r, int g, int b) {
        TeaColour closest = WHITE;

        for (TeaColour colour : TeaColour.values()) {
            if (colour != closest) {
                int redDiff = Math.abs(colour.red - r);
                int greenDiff = Math.abs(colour.green - g);
                int blueDiff = Math.abs(colour.blue - b);

                int totalDiff = redDiff + greenDiff + blueDiff;

                if (totalDiff < closest.getTotalDiff(r, g, b)) {
                    closest = colour;
                }
            }
        }

        return closest;
    }

    public int getRgbSum() {
        return red + green + blue;
    }

    private int getTotalDiff(int r, int g, int b) {
        return Math.abs(this.red - r) + Math.abs(this.green - g) + Math.abs(this.blue - b);
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }
}
