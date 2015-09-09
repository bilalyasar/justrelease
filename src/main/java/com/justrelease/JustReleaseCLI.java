package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.ConfigParser;
import com.justrelease.config.GithubRepo;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.project.type.MavenProject;
import com.justrelease.project.type.NPMProject;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.logging.Logger;

public class JustReleaseCLI {
    private final static Logger LOGGER = Logger.getLogger(JustReleaseCLI.class.getName());

    public static void main(String[] args) throws Exception {


        Options options = new Options();
        options.addOption("snapshotVersion", true, "version number that will be updated after the release. maven spesific feature");
        options.addOption("dryRun", false, "release without push");
        options.addOption("h", false, "help");

        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Something wrong with your arguments, You can look usage via -help option...");
            System.exit(0);
        }

        if (args.length < 2) {
            printHelp(options);
        }

        String[] tokens = args[0].split("/");

        if (tokens.length != 2) {
            printHelp(options);
        }

        String username = tokens[0];
        String reponame = tokens[1];
        String releaseType = args[1];

        ReleaseConfig releaseConfig = new ReleaseConfig(new GithubRepo(username, reponame));

        FileUtils.deleteDirectory(new File(releaseConfig.getLocalDirectory()));

        if (commandLine.hasOption("h")) {
            printHelp(options);
        }


        if (commandLine.hasOption("dryRun")) {
            releaseConfig.setDryRun(true);
        }

        releaseConfig.cloneMainRepo();
        ProjectInfo projectInfo = createProjectInfo(releaseConfig);  // maven or grunt project
        releaseConfig.setCurrentVersion(projectInfo.getCurrentVersion());
        Version.Builder builder = new Version.Builder(releaseConfig.getCurrentVersion());


        if (releaseType.equals("major")) {
            releaseConfig.setReleaseVersion(builder.build().incrementMajorVersion().getNormalVersion());
        } else if (releaseType.equals("minor")) {
            releaseConfig.setReleaseVersion(builder.build().incrementMinorVersion().getNormalVersion());
        } else if (releaseType.equals("patch")) {
            releaseConfig.setReleaseVersion(builder.build().incrementPatchVersion().getNormalVersion());
        } else {
            //TODO - check if format of release type match X.Y.Z
            releaseConfig.setReleaseVersion(releaseType);
        }

        if (projectInfo instanceof MavenProject) {

            if (commandLine.hasOption("snapshotVersion")) {
                releaseConfig.setNextVersion(commandLine.getOptionValue("snapshotVersion"));
            } else {
                if (((MavenProject) projectInfo).isSnapShot()) {
                    releaseConfig.setNextVersion(releaseConfig.getReleaseVersion() + "-SNAPSHOT");
                    releaseConfig.setReleaseVersion(builder.build().getNormalVersion());
                }
            }
        }

        releaseConfig.intializeConfig();
        ConfigParser configParser = new ConfigParser(releaseConfig);
        configParser.parse();

        new JustRelease(releaseConfig, projectInfo).release();


    }

    private static void printHelp(Options options) {
        HelpFormatter f = new HelpFormatter();
        System.out.println("");
        System.out.println("Thanks for using justrelease x.y.z!");
        System.out.println("");
        f.printHelp("justrelease <username/repository> <major|minor|patch|X.Y.Z>", options);
        System.exit(0);
    }

    private static ProjectInfo createProjectInfo(ReleaseConfig releaseConfig) {
        File file = new File(releaseConfig.getLocalDirectory() + "/package.json");
        if (file.exists()) return new NPMProject(releaseConfig);
        return new MavenProject(releaseConfig);

    }

}
