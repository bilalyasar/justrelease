package com.justrelease.git;

import com.jcraft.jsch.Session;
import com.justrelease.config.ReleaseConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GitOperations {
    private static Git git;

    private static void cloneMainRepo(GithubRepo repo) throws GitAPIException, IOException {
        Git.cloneRepository()
                .setURI(repo.getRepoUrl())
                .setDirectory(new File(repo.getLocalDirectory()))
                .setTransportConfigCallback(getTransportConfigCallback())
                .setBranch(repo.getBranch())
                .call();

    }

    public static void pushRepoWithTags() throws GitAPIException, IOException {
        System.out.println("Pushing repository with tags...");
        git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
        git.push().setTransportConfigCallback(getTransportConfigCallback()).setPushTags().call();

    }

    public static void commit(String commitMessage) throws IOException, GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(commitMessage).call();
    }

    public static void tagAndCommit(String commitMessage, String tagName) throws IOException, GitAPIException {
        git.tag().setName(tagName).call();
        commit(commitMessage);
    }


    public static TransportConfigCallback getTransportConfigCallback() {
        final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
            }
        };
        return new TransportConfigCallback() {

            public void configure(Transport transport) {
                if (transport instanceof TransportHttp) return;
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        };
    }

    public static void initializeLocalRepository(GithubRepo mainRepo) throws IOException, GitAPIException {
        String localDirectory = mainRepo.getLocalDirectory();

        //delete and clone the source
        FileUtils.deleteDirectory(new File(localDirectory));
        cloneMainRepo(mainRepo);

        git = Git.open(new File(localDirectory));
    }

    public static void createGithubReleasePage(ReleaseConfig releaseConfig, String latestTag) throws IOException, InterruptedException {
        System.out.println("Connecting to GitHub for uploading artifacts");
        GitHub github = GitHub.connect();
        GHUser user = github.getUser(releaseConfig.getMainRepo().getUsername());

        GHRepository releaseRepository = user.getRepository(releaseConfig.getMainRepo().getRepository());
        GHReleaseBuilder ghReleaseBuilder = new GHReleaseBuilder(releaseRepository, releaseConfig.getConfig().getTagName());
        ghReleaseBuilder.name(releaseConfig.getConfig().getTagName());


        if (releaseConfig.getConfig().getDescription() == null) {
            String command2;
            if (!latestTag.equals("")) {
                command2 = "git log " + latestTag + "..HEAD --oneline --pretty=format:'* %s (%h)'";
            } else {
                command2 = "git log --oneline --pretty=format:'* %s (%h)'";
            }
            Process p2 = Runtime.getRuntime().exec(command2, null, new File(releaseConfig.getMainRepo().getLocalDirectory()));
            p2.waitFor();
            String output = IOUtils.toString(p2.getInputStream());
            ghReleaseBuilder.body(output);
        } else {
            System.out.println("Tag Description File Name: " + releaseConfig.getConfig().getDescription());
            InputStream fis = new FileInputStream(releaseConfig.getMainRepo().getLocalDirectory() +
                    File.separator +
                    releaseConfig.getConfig().getDescription());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line;
            String out = "";
            while ((line = br.readLine()) != null) {
                out += line;
                out += "\n";
            }
            ghReleaseBuilder.body(out);
        }

        GHRelease ghRelease = ghReleaseBuilder.create();
        if (releaseConfig.getConfig().getArtifactCommands() != null)
            ghRelease.uploadAsset(new File((releaseConfig.getMainRepo().getLocalDirectory() +
                    File.separator +
                    releaseConfig.getConfig().getAttachment())), "Project Artifact");
    }
}
