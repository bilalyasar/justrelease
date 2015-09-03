package com.justrelease;

import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

public class JustRelease {
    private static final Logger logger = Logger.getLogger(JustRelease.class.getName());
    private ProjectInfo projectInfo;
    ReleaseConfig releaseConfig;
    String tweet = "I have just released %s version of %s";



    public JustRelease(ReleaseConfig releaseConfig, ProjectInfo projectInfo) {
        this.releaseConfig = releaseConfig;
        this.projectInfo = projectInfo;
    }

    public void release() throws Exception {

        logger.info("Replace Release Version:");
        replaceReleaseVersion();
        logger.info("Create Artifact:");
        projectInfo.createArtifacts();
        logger.info("Commit And Tag Version:");
        commitAndTagVersion();

        if (releaseConfig.getNextVersion() != null) {
            logger.info("Replace Next Version:");
            replaceNextVersion();
            logger.info("Commit Next Version:");
            commitNextVersion();
        }


        if (!releaseConfig.isDryRun()) {
            Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
            git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().call();


            if (Desktop.isDesktopSupported()) {
                String text = String.format(tweet,releaseConfig.getReleaseVersion(),
                        releaseConfig.getMainRepo().getRepository());
                String encodedText = URLEncoder.encode(text,"UTF-8");
                String via = "justrelease";
                String encodedURL = URLEncoder.encode(releaseConfig.getMainRepo().getUrl(),"UTF-8");
                String hashtags = "justreleased";
                String encodedParameters = "text="+encodedText+"&"+"via="+via+"&"+"url="+encodedURL+"&"+"hashtags="+hashtags;
                String uri ="https://twitter.com/intent/tweet?" + encodedParameters;
                Desktop.getDesktop().browse(new URI(uri));
            }


            GitHub github = GitHub.connect();
            GHUser user = github.getUser(releaseConfig.getMainRepo().getUsername());

            GHRepository releaseRepository = user.getRepository(releaseConfig.getMainRepo().getRepository());
            GHReleaseBuilder ghReleaseBuilder = new GHReleaseBuilder(releaseRepository,releaseConfig.getTagName());
            ghReleaseBuilder.name(releaseConfig.getTagName());
            // git.log().addRange
            //ghReleaseBuilder.body("testbody");

            GHRelease ghRelease = ghReleaseBuilder.create();
            //ghRelease.uploadAsset(releaseConfig.get)

        }


    }

    private void commitNextVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setMessage(releaseConfig.getNextVersion()).call();
    }

    private void replaceNextVersion() throws IOException {
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
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
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setMessage(releaseConfig.getCommitMessage()).call();
        git.tag().setName(releaseConfig.getTagName()).call();
    }

    private void replaceReleaseVersion() throws IOException {
        for (VersionUpdateConfig versionUpdateConfig : releaseConfig.getVersionUpdateConfigs()) {
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

}
