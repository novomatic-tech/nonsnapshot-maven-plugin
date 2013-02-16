/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.nonblocking.maven.nonsnapshot.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Maven project with the Workspace
 * 
 * @author Juergen Kofler
 */
public class WorkspaceArtifact extends MavenArtifact {

    private File pomFile;

    private boolean insertVersionTag;
    private int versionLocation;
        
    private MavenArtifact parent;
    private int parentVersionLocation;

    private List<WorkspaceArtifact> children = new ArrayList<WorkspaceArtifact>();

    private List<WorkspaceArtifactDependency> dependencies = new ArrayList<WorkspaceArtifactDependency>();

    private String baseVersion;
    private String nextRevisionId;

    private boolean dirty;

    public WorkspaceArtifact(File pomFile, String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);
        this.pomFile = pomFile;
    }

    public File getPomFile() {
        return pomFile;
    }

    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public boolean isInsertVersionTag() {
        return insertVersionTag;
    }

    public void setInsertVersionTag(boolean insertVersionTag) {
        this.insertVersionTag = insertVersionTag;
    }

    public int getVersionLocation() {
        return versionLocation;
    }

    public void setVersionLocation(int versionLocation) {
        this.versionLocation = versionLocation;
    }

    public int getParentVersionLocation() {
        return parentVersionLocation;
    }

    public void setParentVersionLocation(int parentVersionLocation) {
        this.parentVersionLocation = parentVersionLocation;
    }

    public MavenArtifact getParent() {
        return parent;
    }

    public void setParent(MavenArtifact parent) {
        this.parent = parent;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getNextRevisionId() {
        return nextRevisionId;
    }

    public void setNextRevisionId(String nextRevisionId) {
        this.nextRevisionId = nextRevisionId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public List<WorkspaceArtifact> getChildren() {
        return children;
    }

    public List<WorkspaceArtifactDependency> getDependencies() {
        return dependencies;
    }

    public String getNewVersion() {
        if (this.baseVersion == null || this.nextRevisionId == null) {
            return null;
        }
        
        return this.baseVersion + "-" + this.nextRevisionId;
    }
    
}
