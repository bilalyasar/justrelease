package com.justrelease.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractProjectConfig {

    protected InputStream projectConfigurationIS;
    protected InputStream justreleaseConfigIS;

    private String commitMessageTemplate = "released ${version} with :heart: using `justrelease`";
    private String tagNameTemplate = "v${version}";
    private String attachment;
    private String description;
    private List<String> versionUpdatePatterns;
    private List<String> artifactCommands;

    private ReleaseConfig releaseConfig;

    private Yaml yaml = new Yaml();

    public AbstractProjectConfig(InputStream projectConfigurationIS, InputStream justreleaseConfigIS,ReleaseConfig releaseConfig) throws Exception {
        this.projectConfigurationIS = projectConfigurationIS;
        this.justreleaseConfigIS = justreleaseConfigIS;
        this.releaseConfig = releaseConfig;
    }

    public void parse() throws Exception {
        Map root = (Map) yaml.load(justreleaseConfigIS);
        handleBuild(root);
        handleVersionUpdate(root);
        handleTaggingRepos(root);
        handleScm(root);

    }

    private void handleScm(Map root) {
        if (root.get("scm") == null) return;
        ArrayList<String> arrayList = ((ArrayList) root.get("scm"));
        for (String entry : arrayList) {
            String key = entry.split(":")[0];
            String value = entry.split(":")[1];
            if (key.equals("commit")) {
                this.commitMessageTemplate = value;
            }
            if (key.equals("tag")) {
                this.tagNameTemplate = value;
            }
        }
    }

    private void handleTaggingRepos(Map root) {
        if (root.get("publish") == null) return;
        ArrayList<LinkedHashMap> arrayList = ((ArrayList) root.get("publish"));
        for (LinkedHashMap entry : arrayList) {
            String key = (String) entry.keySet().iterator().next();
            if ("npm".equals(key)) {
                // TODO - add npm publish support
            } else if ("github".equals(key)) {
                ArrayList<String> commands = (ArrayList<String>) entry.get(key);
                for (String command : commands) {
                    if (command.startsWith("description"))
                        this.description = command.split(":")[1].replaceAll("\\$\\{version\\}", releaseConfig.getReleaseVersion());
                    if (command.startsWith("attachment"))
                        this.attachment = command.split(":")[1].replaceAll("\\$\\{version\\}", releaseConfig.getReleaseVersion());
                }

            }
        }
    }

    private void handleVersionUpdate(Map root) {
        this.versionUpdatePatterns = (List) root.get("version.update");
    }


    private void handleBuild(Map root) {
        this.artifactCommands = (List) root.get("create.artifacts");
    }

    public String getCommitMessage() {
        return commitMessageTemplate.replaceAll("\\$\\{version\\}", releaseConfig.getReleaseVersion());
    }

    public String getTagName() {
        return tagNameTemplate.replaceAll("\\$\\{version\\}", releaseConfig.getReleaseVersion());
    }

    public String getAttachment() {
        return attachment;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getVersionUpdatePatterns() {
        return versionUpdatePatterns;
    }

    public List<String> getArtifactCommands() {
        return artifactCommands;
    }

    public abstract String getCurrentVersion();
}
