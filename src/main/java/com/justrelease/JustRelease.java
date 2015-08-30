package com.justrelease;

import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import com.justrelease.project.type.ProjectInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

public class JustRelease {
    private static final Logger log = Logger.getLogger(JustRelease.class.getName());
    private ProjectInfo projectInfo;
    ReleaseConfig releaseConfig;


    public JustRelease(ReleaseConfig releaseConfig, ProjectInfo projectInfo) {
        this.releaseConfig = releaseConfig;
        this.projectInfo = projectInfo;
    }

    public void release() throws Exception {

        System.out.println("Replace Release Version:");
        replaceReleaseVersion();
        System.out.println("Create Artifact:");
        projectInfo.createArtifacts();
        System.out.println("Commit And Tag Version:");
        commitAndTagVersion();
        System.out.println("Replace Next Version:");
        replaceNextVersion();

        if(releaseConfig.getNextVersion() != null){
            System.out.println("Commit Next Version:");
            commitNextVersion();
        }


        if (!releaseConfig.isDryRun()) {
            Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
            git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().call();
        }


    }

    private void commitNextVersion() throws IOException, GitAPIException {
        Git git = Git.open(new File(releaseConfig.getLocalDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setMessage(releaseConfig.getReleaseVersion()).call();
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
                FileUtils.writeStringToFile(f, content.replaceAll(releaseConfig.getCurrentVersion(), releaseConfig.getReleaseVersion()));
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
