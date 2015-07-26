package com.justrelease.config;

import java.util.ArrayList;

/**
 * Created by bilal on 25/07/15.
 */
public class ReleaseConfig {
    String localDirectory = "release";
    String mainRepo = "justrelease/justrelease";
    String githubName = "";
    String githubPassword = "";
    ArrayList<String> dependencyRepos = new ArrayList<String>();
    String projectType = "maven";
    String currentVersion = "";
    String releaseVersion = "";
    String nextVersion= "";

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

    public String getMainRepo() {
        return mainRepo;
    }

    public void setMainRepo(String mainRepo) {
        this.mainRepo = mainRepo;
    }

    public ArrayList<String> getDependencyRepos() {
        return dependencyRepos;
    }

    public void setDependencyRepos(ArrayList<String> dependencyRepos) {
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


}
