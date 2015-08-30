package com.justrelease.config;

import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bilal on 25/07/15.
 */
public class ReleaseConfig {
    String localDirectory;
    String configLocation;
    String projectType = "grunt";
    String currentVersion;
    String releaseVersion;
    String nextVersion;
    String commitMessage = "Just Release";
    String tagName = "";

    GithubRepo mainRepo;
    boolean dryRun;

    BuildConfig buildConfig = new BuildConfig();
    ArrayList<VersionUpdateConfig> versionUpdateConfigs = new ArrayList<VersionUpdateConfig>();

    public ReleaseConfig(GithubRepo githubRepo) {
        this.mainRepo = githubRepo;
        this.localDirectory = "release" + File.separator + githubRepo.getUniquePath();
        this.configLocation = "https://raw.githubusercontent.com/" + githubRepo.getUsername()
                + "/" + githubRepo.getRepository() + "/master/justrelease.yml";
    }


    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public GithubRepo getMainRepo() {
        return mainRepo;
    }

    public void setMainRepo(GithubRepo mainRepo) {
        this.mainRepo = mainRepo;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getNextVersion() {
        return nextVersion;
    }

    public void setNextVersion(String nextVersion) {
        this.nextVersion = nextVersion;
    }


    public void addExecConfig(ExecConfig execConfig) {
        buildConfig.addExecConfig(execConfig);
    }

    public BuildConfig getBuildConfig() {
        return buildConfig;
    }

    public void setBuildConfig(BuildConfig buildConfig) {
        this.buildConfig = buildConfig;
    }


    public ArrayList<VersionUpdateConfig> getVersionUpdateConfigs() {
        return versionUpdateConfigs;
    }

    public void setVersionUpdateConfigs(ArrayList<VersionUpdateConfig> versionUpdateConfigs) {
        this.versionUpdateConfigs = versionUpdateConfigs;
    }

    public void addVersionUpdateConfig(VersionUpdateConfig versionUpdateConfig) {
        versionUpdateConfigs.add(versionUpdateConfig);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getTagName() {
        if (tagName != "")
            return tagName;
        setTagName("v" + getReleaseVersion());
        return "v" + getReleaseVersion();
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

}
