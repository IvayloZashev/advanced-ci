package org.zashev.ci.model;

import javax.persistence.*;

@Entity
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private long startTime;

    private long endTime;

    private String pathToLogFile;

    private Integer executionStatus;

    @Column(name = "task_id")
    private Integer jobId;

    public Execution() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getPathToLogFile() {
        return pathToLogFile;
    }

    public void setPathToLogFile(String pathToLogFile) {
        this.pathToLogFile = pathToLogFile;
    }
}
