package com.justrelease.project.type;

import com.justrelease.config.ReleaseConfig;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by bilal on 25/07/15.
 */
public class MavenProject extends AbstractProjectInfo implements ProjectInfo {

    public MavenProject(ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
    }

    public String getCurrentVersion() {
        if (releaseConfig.getCurrentVersion() != null) return releaseConfig.getCurrentVersion();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model result = null;
        try {
            String workingDir = System.getProperty("user.dir");
            result = reader.read(new FileInputStream(workingDir +
                    File.separator +
                    releaseConfig.getLocalDirectory() +
                    File.separator +
                    "pom.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseConfig.setCurrentVersion(result.getVersion());
        return result.getVersion();
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
