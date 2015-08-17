package com.justrelease.config;

import com.justrelease.config.build.ExecConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.justrelease.config.XmlElements.BUILD;
import static com.justrelease.config.XmlElements.CURRENTVERSION;
import static com.justrelease.config.XmlElements.DEPENDENCYREPO;
import static com.justrelease.config.XmlElements.MAINREPO;
import static com.justrelease.config.XmlElements.PROJECTTYPE;
import static com.justrelease.config.XmlElements.RELEASEDIRECTORY;
import static com.justrelease.config.XmlElements.RELEASEVERSION;

/**
 * Created by bilal on 26/07/15.
 */
public class ConfigParser {
    ReleaseConfig releaseConfig;
    String configLocation;
    InputStream in;

    public ConfigParser(String configLocation) {
        this.configLocation = configLocation;
    }

    void loadFromWorkingDirectory() {
        File file = new File(configLocation);
        if (!file.exists()) {
            return;
        }
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
        }
    }

    public void parse(ReleaseConfig releaseConfig) throws Exception {
        this.releaseConfig = releaseConfig;
        loadFromWorkingDirectory();
        if (in != null) parseAndBuildConfig();
    }

    private void parseAndBuildConfig() throws Exception {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc;
//        Yaml yaml = new Yaml();
//        yaml.load(in);
        doc = builder.parse(in);
        Element root = doc.getDocumentElement();
        handleConfig(root);
    }

    private void handleConfig(Element root) {
        for (Node node : new IterableNodeList(root.getChildNodes())) {
            final String nodeName = cleanNodeName(node.getNodeName());
            handleXmlNode(node, nodeName);
        }
    }

    private void handleXmlNode(Node node, String nodeName) {
        if (MAINREPO.isEqual(nodeName)) handleMainRepo(node);
        else if (DEPENDENCYREPO.isEqual(nodeName)) handleDependencyRepo(node);
        else if (CURRENTVERSION.isEqual(nodeName)) handleCurrentVersion(node);
        else if (RELEASEVERSION.isEqual(nodeName)) handleReleaseVersion(node);
        else if (CURRENTVERSION.isEqual(nodeName)) handleNextVersion(node);
        else if (RELEASEDIRECTORY.isEqual(nodeName)) handleReleaseDirectory(node);
        else if (BUILD.isEqual(nodeName)) handleBuild(node);
        else if (PROJECTTYPE.isEqual(nodeName)) handleProjectType(node);
    }

    private void handleBuild(Node node) {
        String command, repo, directory;
        for (Node child : new IterableNodeList(node.getChildNodes())) {
            final String nodeName = cleanNodeName(child.getNodeName());
            if (nodeName.equals("exec")) {
                repo = cleanNodeName(getAttribute(child, "repo"));
                command = cleanNodeName(getAttribute(child, "command"));
                directory = cleanNodeName(getAttribute(child, "directory"));
                ExecConfig execConfig = new ExecConfig(directory, command, repo);
                releaseConfig.addExecConfig(execConfig);
            }
        }
    }

    private void handleReleaseDirectory(Node node) {
        releaseConfig.setLocalDirectory(cleanNodeName(getTextContent(node).trim()));
    }

    private void handleProjectType(Node node) {
        releaseConfig.setProjectType(cleanNodeName(getTextContent(node).trim()));
    }

    private void handleNextVersion(Node node) {
        releaseConfig.setNextVersion(cleanNodeName(getTextContent(node).trim()));
    }

    private void handleReleaseVersion(Node node) {
        releaseConfig.setReleaseVersion(cleanNodeName(getTextContent(node).trim()));
    }

    private void handleCurrentVersion(Node node) {
        releaseConfig.setCurrentVersion(cleanNodeName(getTextContent(node).trim()));
    }

    private void handleDependencyRepo(Node node) {
        ArrayList<GithubRepo> list = releaseConfig.getDependencyRepos();
        String dependencyRepo = getAttribute(node, "repo-name");
        GithubRepo githubRepo = new GithubRepo(dependencyRepo);
        if (getAttribute(node, "directory") == null) {
            githubRepo.setDirectory(cleanNodeName(dependencyRepo.split("/")[1]));
        } else githubRepo.setDirectory(cleanNodeName(getAttribute(node, "directory")));
        list.add(githubRepo);
        releaseConfig.setDependencyRepos(list);

    }

    private void handleMainRepo(Node node) {
        String mainRepo = getAttribute(node, "repo-name");
        releaseConfig.setMainRepo(mainRepo);
        releaseConfig.getMainRepo().setDirectory(mainRepo.split("/")[1]);
    }


    public static class IterableNodeList implements Iterable<Node> {

        private final NodeList parent;
        private final int maximum;
        private final short nodeType;

        public IterableNodeList(final Node node) {
            this(node.getChildNodes());
        }

        public IterableNodeList(final NodeList list) {
            this(list, (short) 0);
        }

        public IterableNodeList(final Node node, short nodeType) {
            this(node.getChildNodes(), nodeType);
        }

        public IterableNodeList(final NodeList parent, short nodeType) {
            this.parent = parent;
            this.nodeType = nodeType;
            this.maximum = parent.getLength();
        }

        public Iterator<Node> iterator() {
            return new Iterator<Node>() {
                private int index;
                private Node next;

                public boolean hasNext() {
                    next = null;
                    for (; index < maximum; index++) {
                        final Node item = parent.item(index);
                        if (nodeType == 0 || item.getNodeType() == nodeType) {
                            next = item;
                            return true;
                        }
                    }
                    return false;
                }

                public Node next() {
                    if (hasNext()) {
                        index++;
                        return next;
                    }
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static String cleanNodeName(final String nodeName) {
        String name = nodeName;
        if (name != null) {
            name = nodeName.replaceAll("\\w+:", "").toLowerCase();
        }
        return name;
    }

    protected String getTextContent(final Node node) {
        if (node != null) {
            final String text;
            text = node.getTextContent();
            return text != null ? text.trim() : "";
        }
        return "";
    }

    protected String getAttribute(org.w3c.dom.Node node, String attName) {
        final Node attNode = node.getAttributes().getNamedItem(attName);
        if (attNode == null) {
            return null;
        }
        return getTextContent(attNode);
    }
}
