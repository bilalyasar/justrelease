package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.NPMProjectConfig;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.git.GithubRepo;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReleaseConfigNpmTest {
    static GithubRepo githubRepo;
    static ReleaseConfig releaseConfig;

    @BeforeClass
    public static void setup() throws Exception {
        githubRepo = new GithubRepo("justrelease", "justrelease-sample-npm");
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
        String releaseVersion = releaseConfig.getReleaseVersion();
        assertEquals(releaseVersion, builder.build().incrementPatchVersion().getNormalVersion());
    }

    @Test
    public void testNextVersion() {
        assertNull(releaseConfig.getNextVersion());
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
        assertTrue(releaseConfig.getConfig().getCommitMessage().contains(releaseConfig.getReleaseVersion()));
    }

}
