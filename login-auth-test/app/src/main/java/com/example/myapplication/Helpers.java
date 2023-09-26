package com.example.myapplication;

public class Helpers {
    private static String[] segmentString(String str, int lowerLimit) {
        /* Splits string into segments of lowerLimit length + remaining characters on the current word. (so as to not cut string mid-sentance.) */
        String[] stringSegments = new String[99];
        int stepper;
        int currentSubstring;
        for (currentSubstring = 0; !str.isEmpty(); currentSubstring++) {
            for (stepper = lowerLimit; ; stepper++){
                if (stepper >= str.length()){
                    stringSegments[currentSubstring] = str;
                    str = "";
                    break;
                }
                if (Character.isWhitespace(str.charAt(stepper))) {
                    stringSegments[currentSubstring] = str.substring(0, stepper);
                    str = str.substring(stepper);
                    break;
                }
            }
        }

        //Removes trailing nulls by creating a new string array. Also removes leading whitespace after segmenting string.
        String[] out = new String[currentSubstring];
        for (stepper = 0; stepper < currentSubstring; stepper++){
            out[stepper] = shaveLeadingWhitespace(stringSegments[stepper]);
        }

        return out;
    }

    private static String shaveLeadingWhitespace(String toShave){
        //Returns the string, after removing leading whitespace.
        String out;
        for (int i = 0; i < toShave.length(); i++){
            if (!Character.isWhitespace(toShave.charAt(i))){
                out = toShave.substring(i);
                return out;
            }
        }
        return "ERROR IN Helpers.shaveLeadingWhitespace";
    }

    private static String cutoffString(String toCut, int cutoff){
        //Cuts the string toCut off at cutoff, and appending trailing dots "...".
        if (toCut.length() <= cutoff){ return toCut + "...";}
        return toCut.substring(0, cutoff) + "...";
    }

    private static String linebreakString(String str, int lowerLimit){
        //Inserts linebreaks in a string after every lowerLimit characters (end of word).
        String[] strs = segmentString(str, lowerLimit);
        String out = "";
        for (int i = 0; i < strs.length; i++){
            out = out + "\n" + strs[i];
        }
        shaveLeadingWhitespace(out);
        return out;
    }
}