package com.justrelease;

public class JustRelease {

    public static void main(String[] args) throws VersionParseException{
        new Cli(args).parse();

    }

}
