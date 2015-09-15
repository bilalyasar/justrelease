package com.justrelease.config;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.InputStream;

public class MavenProjectConfig extends AbstractProjectConfig {

    public MavenProjectConfig(InputStream projectConfigurationIS, InputStream justreleaseConfigIS, ReleaseConfig releaseConfig) throws Exception {
        super(projectConfigurationIS, justreleaseConfigIS, releaseConfig);
    }

    @Override
    protected void readCurrentVersion() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model result = null;
        try {
            result = reader.read(projectConfigurationIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.currentVersion = result.getVersion();
    }

}
