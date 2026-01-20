package com.example.event_indexer.repo;

import com.example.event_indexer.model.EventEntity;
import com.example.event_indexer.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByEventType(EventType type);
}
