package com.justrelease;

import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

public class JustRelease {
    private static final Logger logger = Logger.getLogger(JustRelease.class.getName());
    private ProjectInfo projectInfo;
    private ReleaseConfig releaseConfig;
    private String tweet = "I have just released %s version of %s";
    private String latestTag;


    public JustRelease(ReleaseConfig releaseConfig, ProjectInfo projectInfo) {
        this.releaseConfig = releaseConfig;
        this.projectInfo = projectInfo;
    }

    public void release() throws Exception {

        System.out.println("Starting to Release: " + releaseConfig.getMainRepo().getRepository());
        replaceReleaseVersion();
        projectInfo.createArtifacts();
        getLatestTag();
        commitAndTagVersion();
        checkAndCommitNextVersion();
        publish();
        System.out.println("Done! Thanks for using JustRelease...");
    }

    private void checkAndCommitNextVersion() throws IOException, GitAPIException {
        if (releaseConfig.getNextVersion() != null) {
            replaceNextVersion();
            commitNextVersion();
        }
    }

    private void publish() throws Exception {
        if (releaseConfig.isDryRun()) {
            System.out.println("You enabled the dryRun config, so anything will be published or pushed.");
            return;
        }
        System.out.println("Pushing tag: " + releaseConfig.getTagName());
        System.out.println("Pushing repo " + releaseConfig.getMainRepo().getRepository());
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
        git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().call();


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


        System.out.println("Connecting to GitHub for uploading artifacts");
        GitHub github = GitHub.connect();
        GHUser user = github.getUser(releaseConfig.getMainRepo().getUsername());

        GHRepository releaseRepository = user.getRepository(releaseConfig.getMainRepo().getRepository());
        GHReleaseBuilder ghReleaseBuilder = new GHReleaseBuilder(releaseRepository, releaseConfig.getTagName());
        ghReleaseBuilder.name(releaseConfig.getTagName());

        // git.log().addRange

        if (releaseConfig.getMainRepo().getDescriptionFileName() == null) {
            String[] command2;
            if (!latestTag.equals("")) {
                command2 = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + "git log " + latestTag + "..HEAD --oneline --pretty=format:'* %s (%h)'"};
            } else {
                command2 = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + "git log --oneline --pretty=format:'* %s (%h)'"};
            }
            Process p2 = Runtime.getRuntime().exec(command2);
            p2.waitFor();
            String output = IOUtils.toString(p2.getInputStream());
            String errorOutput = IOUtils.toString(p2.getErrorStream());
            ghReleaseBuilder.body(output);
        } else {
            System.out.println("Tag Description File Name: " + releaseConfig.getMainRepo().getDescriptionFileName());
            InputStream fis = new FileInputStream(releaseConfig.getLocalDirectory() +
                    File.separator +
                    releaseConfig.getMainRepo().getDescriptionFileName());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line;
            String out = "";
            while ((line = br.readLine()) != null) {
                out += line;
                out += "\n";
            }
            ghReleaseBuilder.body(out);
        }

        GHRelease ghRelease = ghReleaseBuilder.create();
        if (releaseConfig.getMainRepo().getAttachmentFile() != null)
            ghRelease.uploadAsset(new File((releaseConfig.getLocalDirectory() +
                    File.separator +
                    releaseConfig.getMainRepo().getAttachmentFile())), "Project Artifact");

    }

    private void commitNextVersion() throws IOException, GitAPIException {
        System.out.println("Commit Next Version:");
        System.out.println("Committing Next Version: " + releaseConfig.getNextVersion());
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setMessage(releaseConfig.getNextVersion()).call();
    }

    private void replaceNextVersion() throws IOException {
        System.out.println("Replace Next Version:");
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
            System.out.println("Updating " + versionUpdateConfig.getRegex() +
                    " extensions from " + releaseConfig.getCurrentVersion() + " to " + releaseConfig.getNextVersion());
            Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory()),
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
        System.out.println("Commit And Tag Version:");
        System.out.println("Tagging: " + releaseConfig.getTagName());
        System.out.println("Committing with message: " + releaseConfig.getCommitMessage());
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setMessage(releaseConfig.getCommitMessage()).call();
        git.tag().setName(releaseConfig.getTagName()).call();
    }

    private void replaceReleaseVersion() throws IOException {
        System.out.println("Replace  Release Version");
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
            System.out.println("Updating " + versionUpdateConfig.getRegex() +
                    " extensions from " + releaseConfig.getCurrentVersion() + " to " + releaseConfig.getReleaseVersion());
            Iterator it = FileUtils.iterateFiles(new File(releaseConfig.getLocalDirectory()),
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

    public void getLatestTag() throws InterruptedException, IOException {
        String[] command = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + "git describe --tags --abbrev=0"};
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        latestTag = IOUtils.toString(p.getInputStream()).replaceAll("(\\r|\\n|\\t)", "");
    }
}
