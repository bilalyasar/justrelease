package com.justrelease.project.type;


import com.justrelease.config.ReleaseConfig;
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
        String workingDir = System.getProperty("user.dir");
//        String[] cmd = {"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; npm install"};
//        runCommand(cmd);

        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; grunt build"};
        runCommand(cmd);


        cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; rm -rf node_modules"};
        runCommand(cmd);

        cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; npm install"};
        runCommand(cmd);

        cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; npm shrinkwrap"};
        runCommand(cmd);
        
        cmd = new String[]{"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() + "/" + releaseConfig.getMainRepo().getDirectory() + "; grunt release"};
        runCommand(cmd);
    }
}
