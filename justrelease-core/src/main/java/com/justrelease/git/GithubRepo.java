package com.justrelease.git;

import java.io.File;

public class GithubRepo {
    private String username;
    private String repository;
    private String repoUrl;
    private String branch;
    private String latestTag;
    private File folderToExecute;

    public GithubRepo(String username, String repository) {
        this(username,repository,"master");
        this.folderToExecute = new File(getLocalDirectory());
    }

    public GithubRepo(String username, String repository, String branch) {
        this.username = username;
        this.repository = repository;
        this.branch = branch;
        this.repoUrl = String.format("git@github.com:%s/%s.git",username,repository);
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public String getUsername() {
        return username;
    }

    public String getRepository() {
        return repository;
    }

    public File getFolderToExecute() {
        return folderToExecute;
    }

    public String getLocalDirectory() {
       return "release/" + getUsername() + "_" + getRepository() + "_" + getBranch();
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s",getUsername(),getRepository());
    }

    public String getLatestTag() {
        return latestTag;
    }

    public void setLatestTag(String latestTag) {
        this.latestTag = latestTag;
    }


}