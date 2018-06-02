package org.zashev.ci.repository;

import org.zashev.ci.model.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JobRepository extends CrudRepository<Job, Integer> {

    Job save(Job job);

    List<Job> findAll();

    @Override
    Job findOne(Integer jobId);

    @Transactional
    @Modifying
    @Query("update Job  set buildCommand = :buildCommand, description = :description, baseBranch = :baseBranch where id = :jobId")
    int updateJob(@Param ("buildCommand") String buildCommand,
                  @Param ("description") String description, @Param ("jobId") Integer jobId,
                  @Param("baseBranch") String baseBranch);

    @Query("select j.name from Job j where j.id = :id")
    String getJobName(@Param("id") Integer id);
}
