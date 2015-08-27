package com.justrelease.config;

/**
 * Created by bilal on 30/07/15.
 */
public class GithubRepo {
    String id;
    String repoName;
    String repoUrl;
    String branch = "master";
    String directory;
    String descriptionFileName;

    String attachmentFile;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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

    public GithubRepo(String repoName) {
        this.repoName = repoName;
    }
}