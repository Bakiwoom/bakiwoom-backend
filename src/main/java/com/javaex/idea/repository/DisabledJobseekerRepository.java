package com.javaex.idea.repository;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisabledJobseekerRepository extends MongoRepository<DisabledJobseekerDTO, String> {
    // ID 목록에 해당하는 데이터 찾기
    List<DisabledJobseekerDTO> findByIdIn(List<String> ids);
}
