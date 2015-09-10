package com.justrelease.config.build;

import com.justrelease.git.GithubRepo;

/**
 * Created by bilal on 14/08/15.
 */
public class ExecConfig {
    private String command;
    private String directory;
    private GithubRepo githubRepo;

    public ExecConfig(String directory, String command, GithubRepo githubRepo) {
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

    public GithubRepo getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(GithubRepo githubRepo) {
        this.githubRepo = githubRepo;
    }

}
