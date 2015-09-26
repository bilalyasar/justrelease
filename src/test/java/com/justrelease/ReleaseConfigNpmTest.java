package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.NPMProjectConfig;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.git.GithubRepo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReleaseConfigNpmTest {
    static GithubRepo githubRepo;
    static ReleaseConfig releaseConfig;

    @BeforeClass
    public static void setup() throws Exception {
        githubRepo = mock(GithubRepo.class);
        when(githubRepo.getBranch()).thenReturn("master");
        when(githubRepo.getFolderToExecute()).thenReturn(new File("release/test"));
        when(githubRepo.getLocalDirectory()).thenReturn("release/test");
        when(githubRepo.getRepoUrl()).thenReturn(String.format("https://github.com/justrelease/justrelease-sample-npm"));
        GitOperations.initializeLocalRepository(githubRepo);
        releaseConfig = new ReleaseConfig(githubRepo, true, null, "patch");
    }

    @Test
    public void testProjectType() {
        assertTrue(releaseConfig.getConfig() instanceof NPMProjectConfig);
    }

    @Test
    public void testReleaseVersion() {
        Version.Builder builder = new Version.Builder(releaseConfig.getConfig().getCurrentVersion());
        String releaseVersion = releaseConfig.getConfig().getReleaseVersion();
        assertEquals(releaseVersion, builder.build().incrementPatchVersion().getNormalVersion());
    }

    @Test
    public void testNextVersion() {
        assertEquals(releaseConfig.getConfig().getNextVersion(), releaseConfig.getConfig().getReleaseVersion());
    }

    @Test
    public void testIsDryRun() {
        assertTrue(releaseConfig.isDryRun());
    }

    @Test
    public void testArtifactCommands() {
        assertEquals(releaseConfig.getConfig().getArtifactCommands().get(0), "npm install");
    }


    //also this test checks ${version} replace functionality.
    @Test
    public void testCommitMessageIncludesVersion() {
        assertTrue(releaseConfig.getConfig().getCommitMessage().contains(releaseConfig.getConfig().getReleaseVersion()));
    }

}
