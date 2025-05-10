package com.javaex.idea.repository;

import com.javaex.idea.dto.WelfareServiceDetailDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareServiceDetailRepository extends MongoRepository<WelfareServiceDetailDTO, String> {
    @Query(value = "{}", fields = "{ 'servId' : 1, '_id': 0 }")
    List<String> findAllServIds();
} 