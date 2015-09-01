package com.justrelease.config;

/**
 * Created by bilal on 30/07/15.
 */
public class GithubRepo {
    String username;
    String repository;
    String repoUrl;
    String branch;
    String descriptionFileName;
    String attachmentFile;

    public GithubRepo(String username, String repository) {
        this(username,repository,"master");
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

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public String getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(String attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public String getDescriptionFileName() {
        return descriptionFileName;
    }

    public void setDescriptionFileName(String descriptionFileName) {
        this.descriptionFileName = descriptionFileName;
    }

    public String getUsername() {
        return username;
    }

    public String getRepository() {
        return repository;
    }

    public String getUniquePath() {
       return getUsername() + "_" + getRepository() + "_" + getBranch();
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s",getUsername(),getRepository());
    }
}