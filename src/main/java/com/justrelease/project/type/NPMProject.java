package com.justrelease.project.type;


import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.logging.Logger;

public class NPMProject extends AbstractProjectInfo implements ProjectInfo {

    private final static Logger logger = Logger.getLogger(NPMProject.class.getName());

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
        BuildConfig buildConfig = releaseConfig.getBuildConfig();

        for (ExecConfig execConfig : buildConfig.getExecConfigs()) {
            String[] cmd = createCommand(execConfig);
            runCommand(cmd);
        }
    }

    private String[] createCommand(ExecConfig execConfig) {
        logger.info("cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand());
        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand()};
        logger.info(cmd.toString());
        return cmd;
    }
}
