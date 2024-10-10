package com.stashwalker.utils;

public class StringUtil {

    public static String convertCamelCaseToWords (String camelCaseString) {

        // Regular expression to find capital letters and insert a space before each of them
        String spaced = camelCaseString.replaceAll("([a-z])([A-Z])", "$1 $2");
        
        // Capitalize only the first word and return the result
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }
}
