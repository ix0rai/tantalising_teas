package io.ix0rai.tantalisingteas.blocks.cinnamon;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.random.RandomGenerator;

public enum CinnamonTreeLeaves implements StringIdentifiable {
    NONE("none"),
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String id;

    CinnamonTreeLeaves(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public String asString() {
        return this.toString();
    }

    public static CinnamonTreeLeaves random(RandomGenerator random) {
        return values()[random.nextInt(values().length)];
    }
}
