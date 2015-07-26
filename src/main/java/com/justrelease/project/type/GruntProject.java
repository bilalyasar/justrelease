package com.justrelease.project.type;


import com.justrelease.config.ReleaseConfig;
import org.apache.commons.cli.CommandLine;

public class GruntProject extends AbstractProjectInfo implements ProjectInfo {

    public GruntProject(CommandLine cmd, ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
        this.cmd = cmd;
    }

    public String getCurrentVersion() {
        if (!releaseConfig.getCurrentVersion().equals("")) return releaseConfig.getCurrentVersion();
        return null;
    }

    public void createArtifacts() {
        String workingDir = System.getProperty("user.dir");
        String[] cmd = {"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "; npm install"};
        runCommand(cmd);
        cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "; grunt "};
        runCommand(cmd);
    }
}
