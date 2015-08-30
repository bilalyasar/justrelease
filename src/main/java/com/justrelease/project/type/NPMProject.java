package com.justrelease.project.type;


import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class NPMProject extends AbstractProjectInfo implements ProjectInfo {

    public NPMProject(ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
    }

    public String getCurrentVersion() {
        String workingDir = System.getProperty("user.dir") + "/" + releaseConfig.getLocalDirectory() + "/";
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(releaseConfig.getLocalDirectory()+ "/package.json"));
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
        System.out.println("cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand());
        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand()};
        System.out.println(cmd.toString());
        return cmd;
    }
}
