package com.justrelease.config.build;

/**
 * Created by bilal on 14/08/15.
 */
public class VersionUpdateConfig {
    String regex;
    String githubRepo;

    public VersionUpdateConfig(String regex, String githubRepo) {
        this.regex = regex;
        this.githubRepo = githubRepo;
    }


    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

}
