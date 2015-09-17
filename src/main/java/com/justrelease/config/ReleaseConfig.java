package com.justrelease.config;

import com.justrelease.git.GithubRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class ReleaseConfig {
    private GithubRepo mainRepo;
    private boolean dryRun;
    private String snapshotVersion;
    private String releaseType;
    private AbstractProjectConfig config;


    public ReleaseConfig(GithubRepo githubRepo, boolean dryRun, String snapshotVersion, String releaseType) throws Exception {
        this.mainRepo = githubRepo;
        this.dryRun = dryRun;
        this.snapshotVersion = snapshotVersion;
        this.releaseType = releaseType;
        this.config = createProjectInfo(mainRepo);
    }

    public GithubRepo getMainRepo() {
        return mainRepo;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public AbstractProjectConfig getConfig() {
        return config;
    }

    public String getSnapshotVersion() {
        return snapshotVersion;
    }

    public String getReleaseType() {
        return releaseType;
    }



    private AbstractProjectConfig createProjectInfo(GithubRepo mainrepo) throws Exception {

        File justreleaseConfigFile = new File(getMainRepo().getLocalDirectory() + "/justrelease.yml");

        InputStream projectConfigurationIS;
        InputStream justreleaseConfigIS;

        //npm support
        File packageJsonFile = new File(mainrepo.getLocalDirectory() + "/package.json");

        if (packageJsonFile.exists() && !packageJsonFile.isDirectory()) {
            projectConfigurationIS = new FileInputStream(packageJsonFile);

            if (justreleaseConfigFile.exists() && !justreleaseConfigFile.isDirectory()) {
                justreleaseConfigIS = new FileInputStream(justreleaseConfigFile);
                return new NPMProjectConfig(projectConfigurationIS, justreleaseConfigIS, this);
            } else {
                justreleaseConfigIS = ReleaseConfig.class.getResourceAsStream("/default-npm.yml");
                return new NPMProjectConfig(projectConfigurationIS, justreleaseConfigIS, this);
            }
        }

        // maven support
        File pomXMLFile = new File(mainrepo.getLocalDirectory() + "/pom.xml");

        if (pomXMLFile.exists() && !pomXMLFile.isDirectory()) {
            projectConfigurationIS = new FileInputStream(pomXMLFile);

            if (justreleaseConfigFile.exists() && !justreleaseConfigFile.isDirectory()) {
                justreleaseConfigIS = new FileInputStream(justreleaseConfigFile);
                return new MavenProjectConfig(projectConfigurationIS, justreleaseConfigIS, this);
            } else {
                justreleaseConfigIS = ReleaseConfig.class.getResourceAsStream("/default-mvn.yml");
                return new MavenProjectConfig(projectConfigurationIS, justreleaseConfigIS, this);
            }
        }

        throw new RuntimeException("Unsupported Project Type");

    }

}
