package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.MavenProjectConfig;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.git.GithubRepo;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReleaseConfigMavenTest {
    static GithubRepo githubRepo;
    static ReleaseConfig releaseConfig;

    @BeforeClass
    public static void setup() throws Exception {
        githubRepo = mock(GithubRepo.class);
        when(githubRepo.getBranch()).thenReturn("master");
        when(githubRepo.getFolderToExecute()).thenReturn(new File("release/test"));
        when(githubRepo.getLocalDirectory()).thenReturn("release/test");
        when(githubRepo.getRepoUrl()).thenReturn(String.format("https://github.com/justrelease/justrelease-sample-maven"));
        GitOperations.initializeLocalRepository(githubRepo);
        releaseConfig = new ReleaseConfig(githubRepo, true, null, "patch");
    }

    @Test
    public void testProjectType() {
        assertTrue(releaseConfig.getConfig() instanceof MavenProjectConfig);
    }

    @Test
    public void testReleaseVersion() {
        Version.Builder builder = new Version.Builder(releaseConfig.getConfig().getCurrentVersion());
        String releaseVersion = releaseConfig.getConfig().getReleaseVersion();
        assertEquals(releaseVersion, builder.build().getNormalVersion());
    }

    @Test
    public void testNextVersion() {
        assertNotNull(releaseConfig.getConfig().getNextVersion());
        Assert.assertTrue(releaseConfig.getConfig().getNextVersion().contains("-SNAPSHOT"));
    }

    @Test
    public void testIsDryRun() {
        assertTrue(releaseConfig.isDryRun());
    }

    @Test
    public void testArtifactCommands() {
        assertEquals(releaseConfig.getConfig().getArtifactCommands().get(0), "mvn clean install");
    }


    //also this test checks ${version} replace functionality.
    @Test
    public void testCommitMessageIncludesVersion() {
        assertTrue(releaseConfig.getConfig().getCommitMessage().contains(releaseConfig.getConfig().getReleaseVersion()));
    }

    @Test
    public void testAttachment() {
        assertEquals(releaseConfig.getConfig().getAttachment(), "target/hello-world-" + releaseConfig.getConfig().getReleaseVersion() + ".jar");
    }

    @Test
    public void testDescriptionFileName() {
        assertEquals(releaseConfig.getConfig().getDescription(), "releasenotes.md");
    }
}
