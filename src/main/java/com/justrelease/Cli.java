package com.justrelease;

import com.justrelease.config.ConfigParser;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.project.type.GruntProject;
import com.justrelease.project.type.MavenProject;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

public class Cli {
    private static final Logger log = Logger.getLogger(Cli.class.getName());
    private String[] args = null;
    private Options options = new Options();
    private CommandLine cmd;
    private ProjectInfo projectInfo;
    DefaultVersionInfo versionInfo = null;
    CredentialsProvider cp;
    String projectType = "maven";
    ReleaseConfig releaseConfig = new ReleaseConfig();

    public Cli(String[] args) throws VersionParseException {

        this.args = args;
        options.addOption("repo", true, "repo url");
        options.addOption("localDirectory", true, "local source repository directory");
        options.addOption("name", true, "github username");
        options.addOption("password", true, "github password");
        options.addOption("c", true, "current snapshot version");
        options.addOption("h", false, "help");
        options.addOption("dryRun", false, "release without push");
        options.addOption("type", true, "project type info");
    }

    public void parse() throws Exception {
        CommandLineParser parser = new BasicParser();

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
            }
            ConfigParser configParser = new ConfigParser();
            configParser.parse(releaseConfig);
            projectInfo = createProjectInfo();
            projectInfo.setup();
            cloneRepo();
            findVersions();
            replaceReleaseVersion();
            projectInfo.createArtifacts();
            commitAndTagVersion();
            replaceNextVersion();
            commitNextVersion();

        } catch (Exception e) {
            System.out.println(e);
            help();
        }
    }

    private ProjectInfo createProjectInfo() {
        if (cmd.hasOption("type")) {
            projectType = cmd.getOptionValue("type");
        } else projectType = releaseConfig.getProjectType();

        if (projectType.equals("maven")) {
            return new MavenProject(cmd, releaseConfig);
        }
        return new GruntProject(cmd, releaseConfig);
    }

    private void commitNextVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getNextVersion()).call();

        if (!cmd.hasOption("dryRun")) {
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setCredentialsProvider(cp).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().setCredentialsProvider(cp).call();
        }

    }

    private void replaceNextVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory()), null, false);

        while (it.hasNext()) {
            File f = (File) it.next();
            String content = FileUtils.readFileToString(f);
            FileUtils.writeStringToFile(f, content.replaceAll(releaseConfig.getReleaseVersion(), releaseConfig.getNextVersion()));
        }
    }

    private void commitAndTagVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getReleaseVersion()).call();
        git.tag().setName("v" + releaseConfig.getReleaseVersion()).call();
    }

    private void replaceReleaseVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory()), null, false);
        while (it.hasNext()) {
            File f = (File) it.next();
            String content = FileUtils.readFileToString(f);
            FileUtils.writeStringToFile(f, content.replaceAll(projectInfo.getCurrentVersion(), releaseConfig.getReleaseVersion()));
        }
    }

    private void cloneRepo() throws IOException, GitAPIException {
        FileUtils.deleteDirectory(new File(releaseConfig.getLocalDirectory()));
        cp = new UsernamePasswordCredentialsProvider(releaseConfig.getGithubName(), releaseConfig.getGithubPassword());
        Git.cloneRepository()
                .setURI(releaseConfig.getMainRepo())
                .setDirectory(new File(releaseConfig.getLocalDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setCredentialsProvider(cp)
                .call();

    }

    private void printHelp() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("OptionsTip", options);
    }

    private void findVersions() throws VersionParseException {
        versionInfo = new DefaultVersionInfo(projectInfo.getCurrentVersion());
        System.out.println("current version:" + versionInfo);

        if (releaseConfig.getReleaseVersion().equals("")) {
            releaseConfig.setReleaseVersion(versionInfo.getReleaseVersionString());
        }
        System.out.println("releasing to the version:" + releaseConfig.getReleaseVersion());

        if (releaseConfig.getNextVersion().equals("")) {
            releaseConfig.setNextVersion(versionInfo.getNextVersion().getSnapshotVersionString());
        }
        System.out.println("updating to the next version:" + releaseConfig.getNextVersion());

    }


    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("JustRelease", options);
        System.exit(0);
    }
}
