package com.qatest.api.utils;

import java.util.List;

public final class DataHelper {

    private DataHelper() {}

    public static List<String> knownBreeds() {
        return List.of("bulldog", "labrador", "poodle", "beagle");
    }

    public static String validBreed() {
        return "labrador";
    }

    public static String invalidBreed() {
        return "invalidbreed123";
    }
}
