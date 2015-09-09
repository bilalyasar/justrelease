package com.justrelease.config.build;

import com.justrelease.config.GithubRepo;

/**
 * Created by bilal on 14/08/15.
 */
public class VersionUpdateConfig {
    private String regex;
    private GithubRepo githubRepo;

    public VersionUpdateConfig(String regex, GithubRepo githubRepo) {
        this.regex = regex;
        this.githubRepo = githubRepo;
    }


    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public GithubRepo getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(GithubRepo githubRepo) {
        this.githubRepo = githubRepo;
    }

}
