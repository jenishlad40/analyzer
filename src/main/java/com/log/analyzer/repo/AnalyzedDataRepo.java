package com.log.analyzer.repo;

import com.log.analyzer.modal.AnalyzedData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.sql.rowset.CachedRowSet;

@Repository
public interface AnalyzedDataRepo extends CrudRepository<AnalyzedData, Long>{
}
