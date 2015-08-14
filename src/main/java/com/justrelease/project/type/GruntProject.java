package com.justrelease.project.type;


import com.justrelease.config.GithubRepo;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class GruntProject extends AbstractProjectInfo implements ProjectInfo {

    public GruntProject(CommandLine cmd, ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
        this.cmd = cmd;
    }

    public String getCurrentVersion() {
        if (!releaseConfig.getCurrentVersion().equals("")) return releaseConfig.getCurrentVersion();
        String workingDir = System.getProperty("user.dir") + "/" + releaseConfig.getLocalDirectory() + "/";
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(workingDir + releaseConfig.getMainRepo().getDirectory() + "/package.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = (JSONObject) obj;

        String version = (String) jsonObject.get("version");
        return version;
    }

    public void createArtifacts() {
        BuildConfig buildConfig = releaseConfig.getBuildConfig();

        for (ExecConfig execConfig : buildConfig.getExecConfigs()) {
            String[] cmd = createCommand(execConfig);
            runCommand(cmd);
        }
    }

    private String[] createCommand(ExecConfig execConfig) {
        String workingDir = System.getProperty("user.dir");
        GithubRepo repo = findRepo(execConfig);
        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + repo.getDirectory() + "; " + execConfig.getCommand()};
        System.out.println(cmd.toString());
        return cmd;
    }

    private GithubRepo findRepo(ExecConfig execConfig) {
        if (execConfig.getGithubRepo().equals(releaseConfig.getMainRepo().getRepoName()))
            return releaseConfig.getMainRepo();
        for (GithubRepo repo : releaseConfig.getDependencyRepos()) {
            if (repo.getRepoName().equals(execConfig.getGithubRepo())) return repo;
        }
        return null;
    }
}
