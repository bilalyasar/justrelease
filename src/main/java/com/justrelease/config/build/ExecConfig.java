package com.justrelease.config.build;

/**
 * Created by bilal on 14/08/15.
 */
public class ExecConfig {
    String command;
    String directory;
    String githubRepo;

    public ExecConfig(String directory, String command, String githubRepo) {
        this.directory = directory;
        this.command = command;
        this.githubRepo = githubRepo;
    }

    public String getDirectory() {
        return directory;
    }

    public void setRepo(String repo) {
        this.directory = repo;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

}
