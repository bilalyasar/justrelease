package com.justrelease.project.type;

import com.justrelease.config.ReleaseConfig;
import org.apache.commons.cli.CommandLine;

/**
 * Created by bilal on 25/07/15.
 */
public class MavenProject extends AbstractProjectInfo implements ProjectInfo {

    public MavenProject(CommandLine cmd, ReleaseConfig releaseConfig) {
        this.releaseConfig = releaseConfig;
        this.cmd = cmd;
    }

    public String getCurrentVersion() {
        return "";
//        if (!releaseConfig.getCurrentVersion().equals("")) return releaseConfig.getCurrentVersion();
//        MavenXpp3Reader reader = new MavenXpp3Reader();
//        Model result = null;
//        try {
//            String workingDir = System.getProperty("user.dir");
//            result = reader.read(new FileInputStream(workingDir +
//                    File.separator +
//                    releaseConfig.getLocalDirectory() +
//                    File.separator +
//                    releaseConfig.getMainRepo().getDirectory() +
//                    File.separator +
//                    "pom.xml"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        releaseConfig.setCurrentVersion(result.getVersion());
//        return result.getVersion();
    }

    public void createArtifacts() {
//        String workingDir = System.getProperty("user.dir");
//        String[] cmd = {"/bin/sh", "-c", "cd " + workingDir + "/" + releaseConfig.getLocalDirectory() +
//                File.separator +
//                releaseConfig.getMainRepo().getDirectory() +"; mvn clean install -DskipTests"};
//        runCommand(cmd);
    }
}
