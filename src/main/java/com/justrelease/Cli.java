package com.justrelease;

import com.justrelease.config.ConfigParser;
import com.justrelease.config.GithubRepo;
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
    String configLocation = "justrelease.xml";
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
        options.addOption("config", true, "release config file location");
    }

    public void parse() throws Exception {
        CommandLineParser parser = new BasicParser();

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
            }
            if (cmd.hasOption("config")) {
                configLocation = cmd.getOptionValue("config");
            }
            ConfigParser configParser = new ConfigParser(configLocation);
            configParser.parse(releaseConfig);
            projectInfo = createProjectInfo();
            projectInfo.setup();
            System.out.println("Cloning Main Repo:");
            cloneRepo();
            System.out.println("Cloning Dependency Repo:");
            cloneDependencyRepos();
            System.out.println("Find Version:");
            findVersions();
            System.out.println("Replace Release Version:");
            replaceReleaseVersion();
            System.out.println("Create Artifact:");
            projectInfo.createArtifacts();
            System.out.println("Commit And Tag Version:");
            commitAndTagVersion();
            System.out.println("Replace Next Version:");
            replaceNextVersion();
            System.out.println("Commit Next Version:");
            commitNextVersion();

        } catch (Exception e) {
            System.out.println(e);
            help();
        }
    }

    private void cloneDependencyRepos() throws GitAPIException {
        if (releaseConfig.getDependencyRepos().size() == 0) return;
        for (GithubRepo repo : releaseConfig.getDependencyRepos()) {
            cp = new UsernamePasswordCredentialsProvider(releaseConfig.getGithubName(), releaseConfig.getGithubPassword());
            Git.cloneRepository()
                    .setURI(repo.getRepoUrl())
                    .setDirectory(new File(releaseConfig.getLocalDirectory() + "/" + repo.getDirectory()))
                    .setTransportConfigCallback(getTransportConfigCallback())
                    .setCredentialsProvider(cp)
                    .setBranch(repo.getBranch())
                    .call();
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
        Git git = Git.open(new File(releaseConfig.getLocalDirectory() + File.separator + releaseConfig.getMainRepo().getDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getNextVersion()).call();

        if (!cmd.hasOption("dryRun")) {
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setCredentialsProvider(cp).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().setCredentialsProvider(cp).call();
        }

    }

    private void replaceNextVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory() + File.separator + releaseConfig.getMainRepo().getDirectory()), null, true);

        while (it.hasNext()) {
            File f = (File) it.next();
            String content = FileUtils.readFileToString(f);
            FileUtils.writeStringToFile(f, content.replaceAll(releaseConfig.getReleaseVersion(), releaseConfig.getNextVersion()));
        }
    }

    private void commitAndTagVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(releaseConfig.getLocalDirectory() + File.separator + releaseConfig.getMainRepo().getDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getReleaseVersion()).call();
        git.tag().setName("v" + releaseConfig.getReleaseVersion()).call();
    }

    private void replaceReleaseVersion() throws IOException {
        Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory() + File.separator + releaseConfig.getMainRepo().getDirectory()), null, true);
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
                .setURI(releaseConfig.getMainRepo().getRepoUrl())
                .setDirectory(new File(releaseConfig.getLocalDirectory() + File.separator + releaseConfig.getMainRepo().getDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setCredentialsProvider(cp)
                .setBranch(releaseConfig.getMainRepo().getBranch())
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
