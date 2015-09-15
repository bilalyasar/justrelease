package com.justrelease.config;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.InputStream;

public class MavenProjectConfig extends AbstractProjectConfig {

    private boolean isSnapShot;
    private String currentVersion;

    public MavenProjectConfig(InputStream projectConfigurationIS, InputStream justreleaseConfigIS, ReleaseConfig releaseConfig) throws Exception {
        super(projectConfigurationIS, justreleaseConfigIS, releaseConfig);
    }

    @Override
    public String getCurrentVersion() {
        if (currentVersion != null) {
            return currentVersion;
        }
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model result = null;
        try {
            result = reader.read(projectConfigurationIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.getVersion().toLowerCase().contains("snapshot")) isSnapShot = true;
        currentVersion = result.getVersion();
        return currentVersion;
    }

    public boolean isSnapShot() {
        return isSnapShot;
    }

}
