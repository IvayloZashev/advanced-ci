package org.zashev.ci.model;

import org.eclipse.jgit.lib.ObjectId;

public class GitBranch {
    private String name;
    private ObjectId branchId;

    public GitBranch() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getBranchId() {
        return branchId;
    }

    public void setBranchId(ObjectId branchId) {
        this.branchId = branchId;
    }
}
