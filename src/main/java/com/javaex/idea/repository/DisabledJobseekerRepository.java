package com.javaex.idea.repository;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisabledJobseekerRepository extends MongoRepository<DisabledJobseekerDTO, String> {
}
