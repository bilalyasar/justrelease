package com.justrelease;

import com.github.zafarkhaja.semver.Version;
import com.justrelease.config.MavenProjectConfig;
import com.justrelease.config.ReleaseConfig;
import com.justrelease.git.GitOperations;
import com.justrelease.git.GithubRepo;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JustReleaseTest {
    static GithubRepo githubRepo;
    static ReleaseConfig releaseConfig;

    @BeforeClass
    public static void setup() throws Exception {
        githubRepo = new GithubRepo("justrelease", "justrelease-sample-maven");
        GitOperations.initializeLocalRepository(githubRepo);
        releaseConfig = new ReleaseConfig(githubRepo, true, null, "patch");
    }

    @Test
    public void testProjectType() {
        assertTrue(releaseConfig.getConfig() instanceof MavenProjectConfig);
    }

    @Test
    public void testReleaseVersion() {
        Version.Builder builder = new Version.Builder(releaseConfig.getCurrentVersion());
        String releaseVersion = releaseConfig.getReleaseVersion();
        assertEquals(releaseVersion, builder.build().incrementPatchVersion().getNormalVersion());
    }

    @Test
    public void testNextVersion() {
        Version.Builder builder = new Version.Builder(releaseConfig.getCurrentVersion());
        String nextVersion = releaseConfig.getNextVersion();
        assertEquals(nextVersion, builder.build().incrementPatchVersion().getNormalVersion() + "-SNAPSHOT");
    }

    @Test
    public void testIsDryRun() {
        assertTrue(releaseConfig.isDryRun());

    }

}
