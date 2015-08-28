package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.ConfigParser;
import com.justrelease.config.GithubRepo;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.VersionUpdateConfig;
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
    CredentialsProvider cp;
    ReleaseConfig releaseConfig = new ReleaseConfig();

    public Cli(String[] args) throws VersionParseException {

        this.args = args;
        releaseConfig.getMainRepo().setRepoName(args[0]);
        releaseConfig.setConfigLocation("https://raw.githubusercontent.com/" + args[0] + "/master/justrelease.yml");

        options.addOption("releaseType", true, "release type (major | minor | patch");
        options.addOption("c", true, "current version of the project");
        options.addOption("r", true, "release version of the project");
        options.addOption("n", true, "next version of the project");
        options.addOption("h", false, "help");
        options.addOption("dryRun", false, "release without push");
    }

    public void parse() throws Exception {
        CommandLineParser parser = new BasicParser();

        try {
            cmd = parser.parse(options, args);
            ConfigParser configParser = new ConfigParser(releaseConfig.getConfigLocation());
            configParser.parse(releaseConfig);
            // clone main repo
            releaseConfig.getMainRepo().setRepoUrl(createGithubUrl(releaseConfig.getMainRepo().getRepoName()));
            cloneMainRepo();
            parseOptions();
            projectInfo = createProjectInfo();  // maven or grunt project

            if (releaseConfig.getCurrentVersion().equals(""))
                releaseConfig.setCurrentVersion(createProjectInfo().getCurrentVersion());


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


            System.out.println("Find Version:");
//            findVersions();
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

    private void parseOptions() {
        if (cmd.hasOption("h")) {
            printHelp();
        }
        if (cmd.hasOption("c")) {
            releaseConfig.setCurrentVersion(cmd.getOptionValue("c"));
        }
        if (cmd.hasOption("n")) {
            releaseConfig.setNextVersion(cmd.getOptionValue("c"));
        }
        if (cmd.hasOption("r")) {
            releaseConfig.setReleaseVersion(cmd.getOptionValue("c"));
        }
    }

    private void cloneMainRepo() throws GitAPIException, IOException {
        FileUtils.deleteDirectory(new File(releaseConfig.getLocalDirectory()));
        cp = new UsernamePasswordCredentialsProvider(releaseConfig.getGithubName(), releaseConfig.getGithubPassword());
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        Git.cloneRepository()
                .setURI(mainRepo.getRepoUrl())
                .setDirectory(new File(releaseConfig.getLocalDirectory() + "/" + mainRepo.getDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setCredentialsProvider(cp)
                .setBranch(mainRepo.getBranch())
                .call();
    }

    private ProjectInfo createProjectInfo() {
        String workingDir = System.getProperty("user.dir") + "/" + releaseConfig.getLocalDirectory() + "/";
        File file = new File(workingDir + releaseConfig.getMainRepo().getDirectory() + "/package.json");
        if (file.exists()) return new GruntProject(cmd, releaseConfig);
        return new MavenProject(cmd, releaseConfig);

    }

    private void commitNextVersion() throws IOException, GitAPIException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        Git git = Git.open(new File(releaseConfig.getLocalDirectory() + File.separator + mainRepo.getDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getNextVersion()).call();

        if (!cmd.hasOption("dryRun")) {
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setCredentialsProvider(cp).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().setCredentialsProvider(cp).call();
        }
    }

    private void replaceNextVersion() throws IOException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
            Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory() + File.separator + mainRepo.getDirectory()),
                    versionUpdateConfig.getRegex().split(","), true);
            while (it.hasNext()) {
                File f = (File) it.next();
                if (f.getAbsolutePath().contains(".git")) continue;
                if (f.isHidden() || f.isDirectory()) continue;
                String content = FileUtils.readFileToString(f);
                FileUtils.writeStringToFile(f, content.replaceAll(releaseConfig.getReleaseVersion(), releaseConfig.getNextVersion()));
            }
        }
    }

    private void commitAndTagVersion() throws IOException, GitAPIException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        Git git = Git.open(new File(releaseConfig.getLocalDirectory() + File.separator + mainRepo.getDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getReleaseVersion()).call();
        git.tag().setName("v" + releaseConfig.getReleaseVersion()).call();
    }

    private void replaceReleaseVersion() throws IOException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
            Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory() + File.separator + mainRepo.getDirectory()),
                    versionUpdateConfig.getRegex().split(","), true);
            while (it.hasNext()) {
                File f = (File) it.next();
                if (f.getAbsolutePath().contains(".git")) continue;
                if (f.isHidden() || f.isDirectory()) continue;
                String content = FileUtils.readFileToString(f);
                FileUtils.writeStringToFile(f, content.replaceAll(releaseConfig.getCurrentVersion(), releaseConfig.getReleaseVersion()));
            }
        }
    }

    private void printHelp() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("OptionsTip", options);
    }


    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("JustRelease", options);
        System.exit(0);
    }

    private String createGithubUrl(String repo) {
        if (!releaseConfig.getGithubName().equals("") && !releaseConfig.getGithubPassword().equals(""))
            return String.format("https://github.com/%s.git", repo);
        return String.format("git@github.com:%s.git", repo);
    }
}
