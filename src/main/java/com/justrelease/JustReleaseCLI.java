package com.justrelease;

import com.github.zafarkhaja.semver.Version;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

public class JustReleaseCLI {

    public static void main(String[] args) throws Exception{

        Options options = new Options();
        options.addOption("releaseType", true, "release type (major | minor | patch)");
        options.addOption("r", true, "release version of the project");
        options.addOption("n", true, "next version of the project");
        options.addOption("h", false, "help");
        options.addOption("dryRun", false, "release without push");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
        }

        ReleaseConfig releaseConfig = new ReleaseConfig();

        FileUtils.deleteDirectory(new File(releaseConfig.getLocalDirectory()));
        releaseConfig.setMainRepo(new GithubRepo(args[0]));
        releaseConfig.getMainRepo().setDirectory(args[0].replace('/','_'));
        releaseConfig.setConfigLocation("https://raw.githubusercontent.com/" + args[0] + "/master/justrelease.yml");

        if (cmd.hasOption("h")) {
            printHelp(options);
        }

        if (cmd.hasOption("n")) {
            releaseConfig.setNextVersion(cmd.getOptionValue("n"));
        }
        if (cmd.hasOption("r")) {
            releaseConfig.setReleaseVersion(cmd.getOptionValue("r"));
        }

        if (cmd.hasOption("dryRun")) {
            releaseConfig.setDryRun(true);
        }

        releaseConfig.getMainRepo().setRepoUrl(String.format("git@github.com:%s.git", args[0]));
        cloneMainRepo(releaseConfig);

        ProjectInfo projectInfo = createProjectInfo(releaseConfig);  // maven or grunt project

        if (releaseConfig.getCurrentVersion().equals(""))
            releaseConfig.setCurrentVersion(projectInfo.getCurrentVersion());


        if (cmd.hasOption("releaseType")) {
            Version.Builder builder = new Version.Builder(releaseConfig.getCurrentVersion());
            releaseConfig.setReleaseVersion(builder.build().getNormalVersion());
            if (cmd.getOptionValue("releaseType").equals("major")) {
                releaseConfig.setNextVersion(builder.build().incrementMajorVersion().getNormalVersion());
            } else if (cmd.getOptionValue("releaseType").equals("minor")) {
                releaseConfig.setNextVersion(builder.build().incrementMinorVersion().getNormalVersion());
            } else if (cmd.getOptionValue("releaseType").equals("patch")) {
                releaseConfig.setNextVersion(builder.build().incrementPatchVersion().getNormalVersion());
            }
        }

        new JustRelease(releaseConfig,projectInfo).release();


    }

    private static void printHelp(Options options) {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("justrelease <githubuser/repository>", options);
        System.exit(0);
    }

    private static ProjectInfo createProjectInfo(ReleaseConfig releaseConfig) {
        String workingDir = System.getProperty("user.dir") + "/" + releaseConfig.getLocalDirectory() + "/";
        File file = new File(workingDir + releaseConfig.getMainRepo().getDirectory() + "/package.json");
        if (file.exists()) return new NPMProject(releaseConfig);
        return new MavenProject(releaseConfig);

    }
    private static void cloneMainRepo(ReleaseConfig releaseConfig) throws GitAPIException, IOException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        Git.cloneRepository()
                .setURI(mainRepo.getRepoUrl())
                .setDirectory(new File(releaseConfig.getLocalDirectory() + "/" + mainRepo.getDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setBranch(mainRepo.getBranch())
                .call();
    }

}
