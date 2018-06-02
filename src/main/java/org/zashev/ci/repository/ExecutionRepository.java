package org.zashev.ci.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.zashev.ci.model.Execution;

import java.util.List;

public interface ExecutionRepository extends PagingAndSortingRepository<Execution, Integer> {

    Execution save(Execution execution);

    @Override
    List<Execution> findAll();
}
