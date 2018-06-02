package org.zashev.ci.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name= "job_id")
    private Integer id;

    @Column(name="name", unique=true)
    private String name;

    private String description;

    private String githubUrl;

    private String baseBranch;

    private String projectRootDirectory;

    private String buildCommand;

    @Transient
    private long lastBuild;

    @OneToMany
    @JoinColumn(name = "task_id", referencedColumnName = "job_id")
    private List<Execution> executionList;

    public Job() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }

    public String getProjectRootDirectory() {
        return projectRootDirectory;
    }

    public void setProjectRootDirectory(String projectRootDirectory) {
        this.projectRootDirectory = projectRootDirectory;
    }

    public List<Execution> getExecutionList() {
        return executionList;
    }

    public void setExecutionList(List<Execution> executionList) {
        this.executionList = executionList;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public long getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(long lastBuild) {
        this.lastBuild = lastBuild;
    }
}
