package com.k.midishapes;

import java.util.ArrayList;
import java.util.Properties;

public class CommandLine {

    public static String[] normalizeCommandArgs(String[] args) {
        ArrayList<String> joined = new ArrayList<String>();
        String key = "";
        String value = "";
        boolean incomplete = false, strReading = false;
        for (int i = 0; i < args.length; i++) {
            String curr = args[i];
            if (curr.contains("\"") && !strReading && incomplete) {
            } else if (curr.contains("\"") && strReading && incomplete) {

            }
            if (curr.startsWith("-") && !incomplete) {
                incomplete = true;
                key = curr;
            } else if (curr.startsWith("-")) {
                incomplete = true;
                joined.add(key);
                joined.add(value);
                key = curr;
            } else {
                if (incomplete) {
                    if (i + 1 < args.length && args[i + 1].startsWith("-")) {
                        // Last part
                        incomplete = false;
                        if (value.equals("")) {
                            value = curr;
                        } else {
                            value += " " + curr;
                        }
                        joined.add(key);
                        joined.add(value);
                        key = "";
                        value = "";
                    } else if (i + 1 >= args.length) {
                        // Also last part, but last in array
                        incomplete = false;
                        if (value.equals("")) {
                            value = curr;
                        } else {
                            value += " " + curr;
                        }
                        joined.add(key);
                        joined.add(value);
                        key = "";
                        value = "";
                        break;
                    } else {
                        // Continue making value aditions
                        incomplete = true;
                        if (value.equals("")) {
                            value = curr;
                        } else {
                            value += " " + curr;
                        }
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Value missing key! This shouldn't be happening.");
                }
            }
        }
        if (incomplete) {
            joined.add(key);
            joined.add(value);
        }
        return joined.toArray(new String[joined.size()]);
    }

    private static Properties clprops = new Properties();

    public static void acceptPair(String key, String val) {
        key = key.replace("-", "");
        clprops.put(key, val);
        System.out.println("Added " + key + ":" + val);
    }

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static String getProperty(String key, String def) {
        return clprops.getProperty(key, def);
    }

    public static boolean hasKey(String key) {
        return clprops.containsKey(key);
    }
}