package com.justrelease;

import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;


public class JustRelease {
    private ReleaseConfig releaseConfig;
    private String tweet = "I have just released %s version of %s";
    private String latestTag;


    public JustRelease(ReleaseConfig releaseConfig, ProjectInfo projectInfo) {
        this.releaseConfig = releaseConfig;
    }

    public void release() throws Exception {

        System.out.println("Starting to Release: " + releaseConfig.getMainRepo().getRepository());

        replaceVersionsAndCommit(releaseConfig.getVersionUpdateConfigs(), releaseConfig.getCurrentVersion(),
                releaseConfig.getReleaseVersion(), releaseConfig.getLocalDirectory());

        createArtifacts();
        getLatestTag();
        commitAndTagVersion();
        if (releaseConfig.getNextVersion() != null) {
            replaceVersionsAndCommit(releaseConfig.getVersionUpdateConfigs(), releaseConfig.getReleaseVersion(),
                    releaseConfig.getNextVersion(), releaseConfig.getLocalDirectory());
        }

        if (!releaseConfig.isDryRun()) {
            GitOperations.pushRepoWithTags();
            makeAnnouncement();
            GitOperations.createGithubReleasePage(releaseConfig, latestTag);
            System.out.println("Done! Thanks for using JustRelease...");
        } else {
            System.out.println("dryRun is enabled so nothing has been pushed to github repository.");
        }
    }

    private void commitAndTagVersion() throws IOException, GitAPIException {
        System.out.println("Tagging: " + releaseConfig.getTagName());
        System.out.println("Committing with message: " + releaseConfig.getCommitMessage());
        GitOperations.tagAndCommit(releaseConfig.getCommitMessage(), releaseConfig.getTagName());
    }

    private void makeAnnouncement() throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && !releaseConfig.isDryRun()) {
            String text = String.format(tweet, releaseConfig.getReleaseVersion(),
                    releaseConfig.getMainRepo().getRepository());
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String via = "justrelease";
            String encodedURL = URLEncoder.encode(releaseConfig.getMainRepo().getUrl(), "UTF-8");
            String hashtags = "justreleased";
            String encodedParameters = "text=" + encodedText + "&" + "via=" + via + "&" + "url=" + encodedURL + "&" + "hashtags=" + hashtags;
            String uri = "https://twitter.com/intent/tweet?" + encodedParameters;
            Desktop.getDesktop().browse(new URI(uri));
        }
    }


    private void replaceVersionsAndCommit(List<VersionUpdateConfig> configs, String oldVersion, String newVersion, String localDirectory) throws IOException, GitAPIException {
        for (VersionUpdateConfig versionUpdateConfig : configs) {
            System.out.println("Updating " + versionUpdateConfig.getRegex() +
                    " extensions from " + oldVersion + " to " + newVersion);
            Iterator it = FileUtils.iterateFiles(new File(localDirectory),
                    versionUpdateConfig.getRegex().split(","), true);
            while (it.hasNext()) {
                File f = (File) it.next();
                if (f.getAbsolutePath().contains(".git")) continue;
                if (f.isHidden() || f.isDirectory()) continue;
                String content = FileUtils.readFileToString(f);
                FileUtils.writeStringToFile(f, content.replaceAll(oldVersion, newVersion));
            }
        }

        GitOperations.commit(newVersion);

    }

    public void getLatestTag() throws InterruptedException, IOException {
        String[] command = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + "git describe --tags --abbrev=0"};
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        latestTag = IOUtils.toString(p.getInputStream()).replaceAll("(\\r|\\n|\\t)", "");
    }

    private void createArtifacts() {
        System.out.println("Create Artifacts:");
        BuildConfig buildConfig = releaseConfig.getBuildConfig();
        for (ExecConfig execConfig : buildConfig.getExecConfigs()) {
            String[] command = createCommand(execConfig);
            runCommand(command);
        }
    }

    private void runCommand(String[] command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String[] createCommand(ExecConfig execConfig) {
        System.out.println("cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand());
        String[] command = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand()};
        System.out.println(command.toString());
        return command;
    }


}
