package com.justrelease;

import org.apache.maven.shared.release.versions.VersionParseException;

public class JustRelease {

    public static void main(String[] args) throws VersionParseException {
        new Cli(args).parse();

    }

}
