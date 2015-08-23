package com.justrelease.config;

import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;

import java.util.ArrayList;

/**
 * Created by bilal on 25/07/15.
 */
public class ReleaseConfig {
    String localDirectory = "release";
    String githubName = "";
    String githubPassword = "";
    public String taggingRepos;
    ArrayList<GithubRepo> dependencyRepos = new ArrayList<GithubRepo>();


    ArrayList<VersionUpdateConfig> versionUpdateConfigs = new ArrayList<VersionUpdateConfig>();
    String projectType = "grunt";
    String currentVersion = "";
    String releaseVersion = "";
    String nextVersion = "";
    BuildConfig buildConfig = new BuildConfig();

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getGithubName() {
        return githubName;
    }

    public void setGithubName(String githubName) {
        this.githubName = githubName;
    }

    public String getGithubPassword() {
        return githubPassword;
    }

    public void setGithubPassword(String githubPassword) {
        this.githubPassword = githubPassword;
    }


    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }


    public ArrayList<GithubRepo> getDependencyRepos() {
        return dependencyRepos;
    }

    public void setDependencyRepos(ArrayList<GithubRepo> dependencyRepos) {
        this.dependencyRepos = dependencyRepos;
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
}
