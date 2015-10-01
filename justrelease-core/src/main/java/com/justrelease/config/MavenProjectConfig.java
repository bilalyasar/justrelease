package com.justrelease.config;

import com.github.zafarkhaja.semver.Version;
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

    @Override
    protected void setNextVersion() {
        String snapshotVersion = this.releaseConfig.getSnapshotVersion();
        String releaseType = this.releaseConfig.getReleaseType();
            if (snapshotVersion != null) {
                this.nextVersion = snapshotVersion;
            } else {
                if (releaseType.equals("patch")) {
                    Version.Builder builder = new Version.Builder(getCurrentVersion());
                    this.releaseVersion = builder.build().getNormalVersion();
                    this.nextVersion = builder.build().incrementPatchVersion().getNormalVersion() + "-SNAPSHOT";
                } else {
                    Version.Builder builder = new Version.Builder(this.releaseVersion);
                    this.nextVersion = builder.build().incrementPatchVersion().getNormalVersion() + "-SNAPSHOT";
                }
            }
    }

}
