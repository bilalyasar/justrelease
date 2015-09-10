package com.justrelease.git;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;

import java.io.File;
import java.io.IOException;

public class GitOperations {
    private static Git git;

    public static void cloneMainRepo(GithubRepo repo,String localDirectory) throws GitAPIException, IOException {
        Git.cloneRepository()
                .setURI(repo.getRepoUrl())
                .setDirectory(new File(localDirectory))
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

    public static void tagAndCommit(String commitMessage,String tagName) throws IOException, GitAPIException {
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

    public static void initialize(String localDirectory) throws IOException{
         git = Git.open(new File(localDirectory));
    };


}
