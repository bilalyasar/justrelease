
#JustRelease

[![Join the chat at https://gitter.im/justrelease/justrelease](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/justrelease/justrelease?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/justrelease/justrelease.svg?branch=master)](https://travis-ci.org/justrelease/justrelease)
[![codecov.io](http://codecov.io/github/justrelease/justrelease/coverage.svg?branch=master)](http://codecov.io/github/justrelease/justrelease?branch=master)

JustRelease is a set of release tools for software libraries hosted on github.com. Using JustRelease, you can get rid of release scripts and automate release processes. [All Release Steps](#justrelease-release-steps) will be executed with one simple action.

Justrelease requires no configuration and applies default behavior in each release step. If you want to customize some of the features offered by justrelease, please include `justrelease.yml` file in your repository.

- [Configuring Credentials](#configuring-credentials) 
- [JustRelease CLI](#justrelease-cli)
  - [Installation](#homebrew)
    - [OS X-Homebrew](#homebrew)
    - [All Operating Systems](#all-operating-systems)
  - [Quick Example](#quick-example)
  - [Usage](#usage)
- [JustRelease Maven Plugin](#justrelease-maven-plugin) :new:
  - [Plugin Configuration](#plugin-configuration)
  - [Usage](#usage)
  - [Options](#options)
- [Advanced Configuration](#advanced-configuration)
  - [Version Update](#version-update)
  - [Create Artifacts](#create-artifacts)
  - [Publish](#publish)
  - [Project Variables](#project-variables)
  - [Release Notes Generator](#release-notes-generator)
- [Developers](#developers)

## Configuring Credentials

- Generate [Personal Access Token](https://github.com/settings/tokens)
- Create ~/.github file and set *login* and *oauth* parameters as below
```
login=github_username
oauth=GITHUB_OAUTH_TOKEN
```

##JustRelease CLI

#### Installation

##### Homebrew
- `brew install justrelease/justrelease/justrelease`

##### All Operating Systems
- Download and unzip [latest zip file](https://github.com/justrelease/justrelease/releases)

####Quick Example

- fork [sample repository](https://github.com/justrelease/justrelease-sample-npm)
- update releasenotes.md file and commit the change.
- run `justrelease yourusername/justrelease-sample-npm patch`
- check releases page of your forked repository

####Usage

```
$ justrelease

Thanks for using justrelease 1.0.2!

usage: justrelease <username/repository> <major|minor|patch|X.Y.Z>
 -dryRun                  release without push
 -h,--help                nelp
 -snapshotVersion <arg>   version number that will be updated after the
                          release. maven spesific feature
 -v,--version             Print the version of the application
```


## JustRelease Maven Plugin

### Plugin Configuration

Add your pom.xml following plugin repository.
```
        <pluginRepository>
            <id>repo</id>
            <url>https://github.com/justrelease/maven-repository/raw/master</url>
        </pluginRepository>
```
Define JustRelease Plugin as follows:

```
            <plugin>
                <groupId>com.justrelease</groupId>
                <artifactId>justrelease-maven-plugin</artifactId>
                <version>1.1.3</version>
                <configuration>
                    <github>username/reponame</github>
                </configuration>
            </plugin>
```
### Usage

Based on release type, you can use following maven goals to perform release operation

- mvn justrelease:patch
- mvn justrelease:minor
- mvn justrelease:major

### Options

If you want to enable `dryRun` you need to pass `-DdryRun=true` argument.

## JustRelease Release Steps

Those are steps executed during each release:

- Updating release versions
- creating artifacts such as software library,bundled distributions(zip,tar.gz),creating documentation,generating release notes
- publishing artifacts like creating Github Release page,uploading artifacts to some servers
- finalizing release by committing and pushing changes


## justrelease.yml Configuration

The first step to use justrelease as your release tool is to create `justrelease.yml` file in your github repository. That is optional and if you don't configure `justrelease.yml` in your repository, [default configurations](#default-justrelease-configurations) will be used based on your build system.

This is an example `justrelease.yml` file:

```
version.update:
    - xml
create.artifacts:
        - mvn clean install
publish:
        - github:
            - description:releasenotes.md
            - attachment:target/justrelease-${version}.jar
```

###version.update configuration

You need to give file extensions as a config. If you don't provide this step, we automatically detect your project type
and use default configurations. Currently we are supporting maven and npm type projects.

An example is following ( you need to put this code snippet to your `justrelease.yml` file.

```
version.update:
    - json,js
```

###create.artifacts configuration

In this step, you need to provide commands that create artifacts in your project. If you don't provide this configuration,
we will use default config according to your project. ( i.e.: For maven `mvn clean install` )

Example create artifacts config:

```
create.artifacts:
        - npm install
        - npm test
```

###publish configuration

In this step you need to provide publish configuration. Currently we are supporting `github` publish.

You can give your releasenotes file, and your attachment file.
An example is following:

```
publish:
        - github:
            - description=releasenotes.md
            - attachment=lib/index.js
```

###Project Variables

In the commands, you can use some variables. They are `${latest.tag}` and `${version}`

So, JustRelease automatically change those variables with corresponding values.

This is an example:

` - attachment:target/justrelease-${version}.zip`

###Release Notes Generator

JustRelease can update your Github Release Page according to your change log file..
You can specify change log file as:
```
publish:
        - github:
            - description=releasenotes.md
```
If you don't specify this file, JustRelease automatically collects all commit messages from the latest tag and generates change log file.

Also you can use third party tools for generating change log file.
See this example:
```
create.artifacts:
        - github_changelog_generator  --since-tag v1.1.3
publish:
        - github:
            - description:CHANGELOG.md
```
We are creating `CHANGELOG.md` file at `create.artifacts` step then use that file in `github` step.

Note: Github Changelog Generator is a good open source project you can find here: 

https://github.com/skywinder/github-changelog-generator

##Default JustRelease Configurations

JustRelease is compatible with all github projects if only `justrelease.yml` is provided in the root directory. Otherwise, following build tools are supported with default configurations.

  - [maven](https://github.com/justrelease/justrelease/blob/master/justrelease-core/src/main/resources/default-mvn.yml)
  - [npm](https://github.com/justrelease/justrelease/blob/master/justrelease-core/src/main/resources/default-npm.yml)
  
##Developers

- [Bilal Yasar](https://twitter.com/bilalyasar_)
- [Mesut Celik](https://twitter.com/mesutcelik)









