package com.justrelease;

import com.justrelease.config.BuildInfoProvider;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.git.GithubRepo;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JustReleaseCLI {

    public static void main(String[] args) throws Exception {


        Options options = new Options();
        options.addOption("snapshotVersion", true, "version number that will be updated after the release. maven spesific feature");
        options.addOption("dryRun", false, "release without push");
        options.addOption("h", false, "help");
        options.addOption(OptionBuilder.withLongOpt("version")
                .withDescription("Print the version of the application")
                .create('v'));

        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Something wrong with your arguments, You can look usage via -help option...");
            System.exit(0);
        }
        if (commandLine.hasOption("v")) {
            printVersion();
        }
        if (args.length < 2) {
            printHelp(options);
        }

        String[] tokens = args[0].split("/");

        if (tokens.length != 2) {
            printHelp(options);
        }

        if (commandLine.hasOption("h")) {
            printHelp(options);
        }

        String username = tokens[0];
        String reponame = tokens[1];
        String releaseType = args[1];

        GithubRepo githubRepo = new GithubRepo(username, reponame);
        GitOperations.initializeLocalRepository(githubRepo);


        boolean dryRun = false;
        String snapshotVersion = null;

        if (commandLine.hasOption("dryRun")) {
            dryRun = true;
        }

        if (commandLine.hasOption("snapshotVersion")) {
            snapshotVersion = commandLine.getOptionValue("snapshotVersion");
        }

        ReleaseConfig releaseConfig = new ReleaseConfig(githubRepo, dryRun, snapshotVersion, releaseType);

        new JustRelease(releaseConfig).release();


    }

    private static void printVersion() {
        System.out.println("JustRelease " + BuildInfoProvider.getVersion());
        System.exit(0);
    }

    private static void printHelp(Options options) {
        HelpFormatter f = new HelpFormatter();
        System.out.println("");
        System.out.println("Thanks for using justrelease " + BuildInfoProvider.getVersion() + "!");
        System.out.println("");
        f.printHelp("justrelease <username/repository> <major|minor|patch|X.Y.Z>", options);
        System.exit(0);
    }


}
