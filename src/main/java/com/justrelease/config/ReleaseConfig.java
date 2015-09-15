package com.justrelease.config;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.git.GithubRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class ReleaseConfig {
    private String releaseVersion;
    private String nextVersion;

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
        initializeVersions();
        config.parse();
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public GithubRepo getMainRepo() {
        return mainRepo;
    }

    public String getNextVersion() {
        return nextVersion;
    }

    public void setNextVersion(String nextVersion) {
        this.nextVersion = nextVersion;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public AbstractProjectConfig getConfig() {
        return config;
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

    private void initializeVersions() {
        Version.Builder builder = new Version.Builder(getConfig().getCurrentVersion());


        if (releaseType.equals("major")) {
            setReleaseVersion(builder.build().incrementMajorVersion().getNormalVersion());
        } else if (releaseType.equals("minor")) {
            setReleaseVersion(builder.build().incrementMinorVersion().getNormalVersion());
        } else if (releaseType.equals("patch")) {
            setReleaseVersion(builder.build().incrementPatchVersion().getNormalVersion());
        } else {
            //TODO - check if format of release type match X.Y.Z
            setReleaseVersion(releaseType);
        }

        if (config instanceof MavenProjectConfig) {
            if (snapshotVersion != null) {
                setNextVersion(snapshotVersion);
            } else {
                builder = new Version.Builder(getReleaseVersion());
                if (releaseType.equals("patch"))
                    setNextVersion(builder.build().getNormalVersion() + "-SNAPSHOT");
                else setNextVersion(builder.build().incrementPatchVersion().getNormalVersion() + "-SNAPSHOT");
            }
        }
    }
}
