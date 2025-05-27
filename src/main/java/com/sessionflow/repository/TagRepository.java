package com.sessionflow.repository;

import com.sessionflow.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根據標籤名稱查詢標籤
     * @param name 標籤名稱
     * @return 標籤實體（如果存在）
     */
    Optional<Tag> findByName(String name);
    
    /**
     * 檢查指定名稱的標籤是否存在
     * @param name 標籤名稱
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 檢查除了指定 ID 外，是否存在相同名稱的標籤
     * @param name 標籤名稱
     * @param id 要排除的標籤 ID
     * @return 是否存在
     */
    boolean existsByNameAndIdNot(String name, Long id);
} 