package com.example.demo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;


@Repository
public interface TaskManagerRepository extends JpaRepository<TaskManager, Long> {

    List<TaskManager> findByDueDate(LocalDate tomorrow);

    @Query(value = """
            SELECT * FROM task_manager
            WHERE embedding IS NOT NULL
            AND id != :excludeId
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT 3
            """, nativeQuery = true)
    List<TaskManager> findSimilarTasks(@Param("embedding") String embedding,
            @Param("excludeId") Long excludeId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE task_manager SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

}
