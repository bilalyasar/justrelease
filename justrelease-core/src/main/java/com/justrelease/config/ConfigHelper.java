package com.justrelease.config;


import org.apache.commons.cli.Options;

import static com.justrelease.JustReleaseCLI.printHelp;

public class ConfigHelper {
    public static void checkReleaseType(String releaseType, Options options) {
        if (releaseType.equals("major") || releaseType.equals("minor") || releaseType.equals("patch")) return;
        if (releaseType.matches("^(\\d+\\.)(\\d+\\.)(\\*|\\d+)$")) return;
        printHelp(options);
    }

}
