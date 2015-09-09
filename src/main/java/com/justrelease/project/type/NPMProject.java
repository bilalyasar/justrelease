package com.justrelease.project.type;


import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class NPMProject extends AbstractProjectInfo implements ProjectInfo {


    public NPMProject(ReleaseConfig releaseConfig) {
        releaseConfig.setProjectType("NPM");
        this.releaseConfig = releaseConfig;
    }

    public String getCurrentVersion() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(releaseConfig.getLocalDirectory() + "/package.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;

        String version = (String) jsonObject.get("version");
        return version;

    }

    public void createArtifacts() {
        System.out.println("Create Artifacts:");
        BuildConfig buildConfig = releaseConfig.getBuildConfig();
        for (ExecConfig execConfig : buildConfig.getExecConfigs()) {
            String[] command = createCommand(execConfig);
            runCommand(command);
        }
    }

    private String[] createCommand(ExecConfig execConfig) {
        System.out.println("cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand());
        String[] command = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand()};
        System.out.println(command.toString());
        return command;
    }
}
