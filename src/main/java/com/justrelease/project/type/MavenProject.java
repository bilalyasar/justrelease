package com.justrelease.project.type;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;

public class MavenProject implements ProjectInfo {

    private boolean isSnapShot;
    private String localDirectory;

    public MavenProject(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getCurrentVersion() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model result = null;
        try {
            String workingDir = System.getProperty("user.dir");
            result = reader.read(new FileInputStream(workingDir +
                    File.separator +
                    localDirectory +
                    File.separator +
                    "pom.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.getVersion().toLowerCase().contains("snapshot")) isSnapShot = true;
        return result.getVersion();
    }

    public boolean isSnapShot() {
        return isSnapShot;
    }

}
