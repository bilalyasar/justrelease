package com.justrelease.config;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import com.justrelease.git.GithubRepo;
import com.justrelease.project.type.MavenProject;
import com.justrelease.project.type.NPMProject;
import com.justrelease.project.type.ProjectInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;


public class ReleaseConfig {
    private String releaseVersion;
    private String nextVersion;
    private String commitMessageTemplate = "released ${version} with :heart: by justrelease";
    private String tagNameTemplate = "v${version}";

    private GithubRepo mainRepo;
    private InputStream configFileStream;
    private boolean dryRun;
    private String snapshotVersion;
    private String releaseType;
    private ProjectInfo projectInfo;

    BuildConfig buildConfig = new BuildConfig();
    ArrayList<VersionUpdateConfig> versionUpdateConfigs = new ArrayList<VersionUpdateConfig>();


    public ReleaseConfig(GithubRepo githubRepo, boolean dryRun, String snapshotVersion,String releaseType) throws Exception {
        this.mainRepo = githubRepo;
        this.dryRun = dryRun;
        this.snapshotVersion = snapshotVersion;
        this.releaseType = releaseType;
        this.projectInfo = createProjectInfo(githubRepo);
        initializeVersions();
        initializeConfig();
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

    public String getCurrentVersion() {
        return projectInfo.getCurrentVersion();
    }

    public String getNextVersion() {
        return nextVersion;
    }

    public void setNextVersion(String nextVersion) {
        this.nextVersion = nextVersion;
    }


    public void addExecConfig(ExecConfig execConfig) {
        buildConfig.addExecConfig(execConfig);
    }

    public BuildConfig getBuildConfig() {
        return buildConfig;
    }

    public void setBuildConfig(BuildConfig buildConfig) {
        this.buildConfig = buildConfig;
    }


    public ArrayList<VersionUpdateConfig> getVersionUpdateConfigs() {
        return versionUpdateConfigs;
    }

    public void setVersionUpdateConfigs(ArrayList<VersionUpdateConfig> versionUpdateConfigs) {
        this.versionUpdateConfigs = versionUpdateConfigs;
    }

    public void addVersionUpdateConfig(VersionUpdateConfig versionUpdateConfig) {
        versionUpdateConfigs.add(versionUpdateConfig);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getCommitMessage() {
        return commitMessageTemplate.replaceAll("\\$\\{version\\}", releaseVersion);
    }

    public String getTagName() {
        return tagNameTemplate.replaceAll("\\$\\{version\\}", releaseVersion);
    }

    public void setCommitMessageTemplate(String commitMessageTemplate) {
        this.commitMessageTemplate = commitMessageTemplate;
    }

    public void setTagNameTemplate(String tagNameTemplate) {
        this.tagNameTemplate = tagNameTemplate;
    }


    private void initializeConfig() throws Exception {

        File file = new File(getMainRepo().getLocalDirectory() + "/justrelease.yml");

        if (file.exists() && !file.isDirectory()) {
            this.configFileStream = new FileInputStream(file);
        } else if (projectInfo instanceof MavenProject) {
            this.configFileStream = ReleaseConfig.class.getResourceAsStream("/default-mvn.yml");
        } else if (projectInfo instanceof NPMProject) {
            this.configFileStream = ReleaseConfig.class.getResourceAsStream("/default-npm.yml");
        } else {
            throw new RuntimeException("We could not detect your configuration. " +
                    "please provide justrelease.yml in your project home directory. ");
        }

        ConfigParser configParser = new ConfigParser(this);
        configParser.parse();
    }

    private ProjectInfo createProjectInfo(GithubRepo mainrepo) {
        File file = new File(mainrepo.getLocalDirectory() + "/package.json");
        if (file.exists()) return new NPMProject(mainrepo.getLocalDirectory());
        return new MavenProject(mainrepo.getLocalDirectory());

    }


    public InputStream getConfigFileStream() {
        return configFileStream;
    }

    private  void initializeVersions() {
        Version.Builder builder = new Version.Builder(getCurrentVersion());


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

        if (projectInfo instanceof MavenProject) {

            if (snapshotVersion != null) {
                setNextVersion(snapshotVersion);
            } else {
                if (((MavenProject) projectInfo).isSnapShot()) {
                    setNextVersion(getReleaseVersion() + "-SNAPSHOT");
                    setReleaseVersion(builder.build().getNormalVersion());
                }
            }
        }
    }
}
