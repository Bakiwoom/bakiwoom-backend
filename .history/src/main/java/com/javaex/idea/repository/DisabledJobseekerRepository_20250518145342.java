package com.javaex.idea.repository;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisabledJobseekerRepository extends MongoRepository<DisabledJobseekerDTO, String> {
    // ID 목록에 해당하는 데이터 찾기
    List<DisabledJobseekerDTO> findByIdIn(List<String> ids);
    
    // 페이징 처리를 위한 메서드
    Page<DisabledJobseekerDTO> findAll(Pageable pageable);
    
    // 장애유형으로 검색
    @Query("{'장애유형': { $in: ?0 }}")
    List<DisabledJobseekerDTO> findByDisabilityTypes(List<String> disabilityTypes);
    
    // 검색어를 포함하는 데이터 찾기 (텍스트 검색, 여러 필드에서 검색)
    @Query("{$or: [{'희망직종': {$regex: ?0, $options: 'i'}}, {'희망지역': {$regex: ?0, $options: 'i'}}, {'장애유형': {$regex: ?0, $options: 'i'}}, {'기관분류': {$regex: ?0, $options: 'i'}}]}")
    List<DisabledJobseekerDTO> findBySearchText(String searchText);
    
    // 검색어와 장애유형 모두 조건에 맞는 데이터 찾기
    @Query("{$and: [{$or: [{'희망직종': {$regex: ?0, $options: 'i'}}, {'희망지역': {$regex: ?0, $options: 'i'}}, {'장애유형': {$regex: ?0, $options: 'i'}}, {'기관분류': {$regex: ?0, $options: 'i'}}]}, {'장애유형': { $in: ?1 }}]}")
    List<DisabledJobseekerDTO> findBySearchTextAndDisabilityTypes(String searchText, List<String> disabilityTypes);
}
