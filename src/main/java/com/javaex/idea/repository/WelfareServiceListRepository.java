package com.javaex.idea.repository;

import com.javaex.idea.dto.WelfareServiceListDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareServiceListRepository extends MongoRepository<WelfareServiceListDTO, String> {
    @Query(value = "{}", fields = "{ 'servId' : 1, '_id': 0 }")
    List<String> findAllServIds();
    
    @Query("{ $or: [ " +
           "{ 'servNm': { $regex: ?0, $options: 'i' } }, " +
           "{ 'servDgst': { $regex: ?0, $options: 'i' } }, " +
           "{ 'trgterIndvdlArray': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lifeArray': { $regex: ?0, $options: 'i' } }, " +
           "{ 'intrsThemaArray': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<WelfareServiceListDTO> searchByKeywords(String keyword);
} 