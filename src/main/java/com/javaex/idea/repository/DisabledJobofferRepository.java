package com.javaex.idea.repository;

import com.javaex.idea.dto.DisabledJobofferDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisabledJobofferRepository extends MongoRepository<DisabledJobofferDTO, String> {
} 