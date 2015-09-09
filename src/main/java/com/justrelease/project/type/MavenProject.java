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

    private boolean isSnapShot;

    public MavenProject(ReleaseConfig releaseConfig) {
        releaseConfig.setProjectType("MAVEN");
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
        if (result.getVersion().toLowerCase().contains("snapshot")) isSnapShot = true;
        releaseConfig.setCurrentVersion(result.getVersion());
        return result.getVersion();
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
        String[] cmd = new String[]{"/bin/sh", "-c", "cd " + releaseConfig.getLocalDirectory() + "; " + execConfig.getCommand()};
        System.out.println(cmd.toString());
        return cmd;
    }

    public boolean isSnapShot() {
        return isSnapShot;
    }

    public void setSnapShot(boolean isSnapShot) {
        this.isSnapShot = isSnapShot;
    }
}
