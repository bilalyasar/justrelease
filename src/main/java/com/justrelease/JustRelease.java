package com.justrelease;

import com.justrelease.config.ConfigParser;
import com.justrelease.config.GithubRepo;
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


    public JustRelease(ReleaseConfig releaseConfig,ProjectInfo projectInfo) {
        this.releaseConfig = releaseConfig;
        this.projectInfo = projectInfo;
    }

    public void release() throws Exception {

             ConfigParser configParser = new ConfigParser(releaseConfig.getConfigLocation());
            configParser.parse(releaseConfig);
            // clone main repo
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

    }


    private void commitNextVersion() throws IOException, GitAPIException {
        GithubRepo mainRepo = releaseConfig.getMainRepo();
        Git git = Git.open(new File(releaseConfig.getLocalDirectory() + File.separator + mainRepo.getDirectory()));
        git.add().addFilepattern(".").call();
        git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseConfig.getNextVersion()).call();

        if (!releaseConfig.isDryRun()) {
            git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
            git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().call();
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

}
