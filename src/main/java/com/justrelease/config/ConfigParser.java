package com.justrelease.config;

import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bilal on 26/07/15.
 */
public class ConfigParser {
    ReleaseConfig releaseConfig;
    String configLocation;
    InputStream in;

    public ConfigParser(String configLocation) {
        this.configLocation = configLocation;
    }

    void loadFromWorkingDirectory() {

        try {
            in = new URL(configLocation).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void parse(ReleaseConfig releaseConfig) throws Exception {
        this.releaseConfig = releaseConfig;
        loadFromWorkingDirectory();
        if (in != null) parseAndBuildConfig();
    }

    private void parseAndBuildConfig() throws Exception {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(in);
        handleConfig(map);
    }

    private void handleConfig(Map root) {
        handleRepositories(root);
        handleBuild(root);
        handleVersionUpdate(root);
        handleTaggingRepos(root);

    }

    private void handleTaggingRepos(Map root) {
        if (root.get("publish") == null) return;
        ArrayList<LinkedHashMap> arrayList = ((ArrayList) root.get("publish"));
        for (LinkedHashMap entry : arrayList) {
            String key = (String) entry.keySet().iterator().next();
            ArrayList<String> commands = (ArrayList) ((LinkedHashMap) ((ArrayList) entry.get(key)).get(0)).get("github");
            for (String command : commands) {
                if (command.startsWith("description"))
                    findRepo(findRepoFromId(key)).setDescriptionFileName(command.split("=")[1]);
                if (command.startsWith("attachment"))
                    findRepo(findRepoFromId(key)).setAttachmentFile(command.split("=")[1]);
            }
        }
    }

    private void handleVersionUpdate(Map root) {
        String repo, regex;
        ArrayList<String> versionUpdates = (ArrayList) root.get("version.update");
        if (versionUpdates == null) return;
        for (String versionUpdate : versionUpdates) {
            repo = findRepoFromId(versionUpdate.split("=")[0]);
            regex = versionUpdate.split("=")[1];
            VersionUpdateConfig versionUpdateConfig = new VersionUpdateConfig(regex, repo);
            releaseConfig.addVersionUpdateConfig(versionUpdateConfig);
        }
    }


    private void handleBuild(Map root) {
        String repo, directory = "";
        ArrayList<Map> artifacts = (ArrayList) root.get("create.artifacts");
        if (artifacts == null) return;
        for (Map<String, ArrayList<String>> artifact : artifacts) {
            for (String command : artifact.values().iterator().next()) {
                repo = findRepoFromId(artifact.keySet().iterator().next());
                ExecConfig execConfig = new ExecConfig(directory, command, repo);
                releaseConfig.addExecConfig(execConfig);
            }
        }

    }

    private GithubRepo findRepo(String repoName) {
        for (GithubRepo repo : releaseConfig.getDependencyRepos()) {
            if (repo.getRepoName().equals(repoName)) return repo;
        }
        return null;
    }

    private String findRepoFromId(String next) {
        for (GithubRepo githubRepo : releaseConfig.getDependencyRepos()) {
            if (githubRepo.getId().equals(next)) return githubRepo.getRepoName();
        }
        return null;
    }

    private void handleRepositories(Map root) {
        for (String repo : (ArrayList<String>) root.get("repositories")) {
            ArrayList<GithubRepo> list = releaseConfig.getDependencyRepos();
            String dependencyRepo = repo.split("=")[1].split("#")[0];
            GithubRepo githubRepo = new GithubRepo(dependencyRepo);
            githubRepo.setDirectory((repo.split("=")[1].split("#")[2]));
            githubRepo.setId(repo.split("=")[0]);
            list.add(githubRepo);
            releaseConfig.setDependencyRepos(list);
        }
    }
}
