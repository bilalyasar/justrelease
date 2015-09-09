package com.justrelease.config;

import com.justrelease.config.build.BuildConfig;
import com.justrelease.config.build.ExecConfig;
import com.justrelease.config.build.VersionUpdateConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.justrelease.project.type.AbstractProjectInfo.getTransportConfigCallback;

/**
 * Created by bilal on 25/07/15.
 */
public class ReleaseConfig {
    private String localDirectory;
    private String projectType = "grunt";
    private String currentVersion;
    private String releaseVersion;
    private String nextVersion;
    private String commitMessageTemplate = "released ${version} with :heart: by justrelease";
    private String tagNameTemplate = "v${version}";
    private boolean customConfig;

    private GithubRepo mainRepo;
    private InputStream configFileStream;
    private boolean dryRun;

    BuildConfig buildConfig = new BuildConfig();
    ArrayList<VersionUpdateConfig> versionUpdateConfigs = new ArrayList<VersionUpdateConfig>();

    public ReleaseConfig(GithubRepo githubRepo) {
        this.mainRepo = githubRepo;
        this.localDirectory = "release" + File.separator + githubRepo.getUniquePath();
    }


    public String getLocalDirectory() {
        return localDirectory;
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

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
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

    public void cloneMainRepo() throws GitAPIException, IOException {
        Git.cloneRepository()
                .setURI(mainRepo.getRepoUrl())
                .setDirectory(new File(localDirectory))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setBranch(mainRepo.getBranch())
                .call();

    }

    public void intializeConfig() throws IOException {

        File file = new File(localDirectory + "/justrelease.yml");

        if (file.exists() && !file.isDirectory()) {
            this.configFileStream = new FileInputStream(file);
            customConfig = true;
        } else if (projectType.equals("MAVEN")) {
            this.configFileStream = ReleaseConfig.class.getResourceAsStream("/default-mvn.yml");
        } else {
            this.configFileStream = ReleaseConfig.class.getResourceAsStream("/default-npm.yml");
        }

    }

    public boolean isCustomConfig() {
        return customConfig;
    }

    public InputStream getConfigFileStream() {
        return configFileStream;
    }
}
