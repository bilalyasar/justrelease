package com.justrelease;

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
    String releaseVersion, nextVersion;


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
            if (cmd.hasOption("type")) {
                projectType = cmd.getOptionValue("type");
            }

            if (projectType.equals("maven")) {
                projectInfo = new MavenProject(cmd);
            } else projectInfo = new GruntProject(cmd);

            projectInfo.setup();
            cloneRepo();
            findVersions();
            replaceReleaseVersion();
            commitAndTagVersion();
            replaceNextVersion();
            commitNextVersion();

        } catch (Exception e) {
            System.out.println(e);
            help();
        }
    }

    private void commitNextVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(projectInfo.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(nextVersion).call();

        if (!cmd.hasOption("dryRun")) {
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setCredentialsProvider(cp).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().setCredentialsProvider(cp).call();
        }

    }

    private void replaceNextVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(projectInfo.getLocalDirectory()), null, false);

        while (it.hasNext()) {
            File f = (File) it.next();
            String content = FileUtils.readFileToString(f);
            FileUtils.writeStringToFile(f, content.replaceAll(releaseVersion, nextVersion));
        }
    }

    private void commitAndTagVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(projectInfo.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseVersion).call();
        git.tag().setName("v" + releaseVersion).call();
    }

    private void replaceReleaseVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(projectInfo.getLocalDirectory()), null, false);
        while (it.hasNext()) {
            File f = (File) it.next();
            String content = FileUtils.readFileToString(f);
            FileUtils.writeStringToFile(f, content.replaceAll(projectInfo.getCurrentVersion(), releaseVersion));
        }
    }

    private void cloneRepo() throws IOException, GitAPIException {
        FileUtils.deleteDirectory(new File(projectInfo.getLocalDirectory()));
        cp = new UsernamePasswordCredentialsProvider(projectInfo.getName(), projectInfo.getPassword());
        Git.cloneRepository()
                .setURI(projectInfo.getRepoUrl())
                .setDirectory(new File(projectInfo.getLocalDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setCredentialsProvider(cp)
                .call();

    }

    private void printHelp() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("OptionsTip", options);
    }

    private void findVersions() throws VersionParseException {
        versionInfo = new DefaultVersionInfo(projectInfo.getVersion());
        System.out.println("current version:" + versionInfo);

        releaseVersion = versionInfo.getReleaseVersionString();
        System.out.println("releasing to the version:" + releaseVersion);

        nextVersion = versionInfo.getNextVersion().getSnapshotVersionString();
        System.out.println("updating to the next version:" + nextVersion);

    }


    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("JustRelease", options);
        System.exit(0);
    }
}