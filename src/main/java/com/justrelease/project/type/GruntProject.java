package com.justrelease.project.type;


import com.justrelease.config.GithubRepo;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.apache.commons.cli.CommandLine;

public class GruntProject extends AbstractProjectInfo implements ProjectInfo {

    public GruntProject(CommandLine cmd, ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
        this.cmd = cmd;
    }

    public String getCurrentVersion() {
        return "";

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
        System.out.println("cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + repo.getDirectory() + "; " + execConfig.getCommand());
        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + repo.getDirectory() + "; " + execConfig.getCommand()};
        System.out.println(cmd.toString());
        return cmd;
    }

    private GithubRepo findRepo(ExecConfig execConfig) {
        for (GithubRepo repo : releaseConfig.getDependencyRepos()) {
            if (repo.getRepoName().equals(execConfig.getGithubRepo())) return repo;
        }
        return null;
    }
}
