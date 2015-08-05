/*
 * Copyright (c) 2008-2014, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.justrelease.config;

enum XmlElements {
    JUSTRELEASE("justrelease", false),
    MAINREPO("main-repo", false),
    PROJECTTYPE("project-type", false),
    CURRENTVERSION("current-version", false),
    RELEASEVERSION("release-version", false),
    NEXTVERSION("next-version", false),
    RELEASEDIRECTORY("release-directory", false),
    DEPENDENCYREPO("dependency-repo", true);


    final String name;
    final boolean multipleOccurrence;

    XmlElements(String name, boolean multipleOccurrence) {
        this.name = name;
        this.multipleOccurrence = multipleOccurrence;
    }

    public static boolean canOccurMultipleTimes(String name) {
        for (XmlElements element : values()) {
            if (name.equals(element.name)) {
                return element.multipleOccurrence;
            }
        }
        return false;
    }

    public boolean isEqual(String name) {
        return this.name.equals(name);
    }

}
