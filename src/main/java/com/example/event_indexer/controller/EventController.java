package com.example.event_indexer.controller;

import com.example.event_indexer.model.EventEntity;
import com.example.event_indexer.model.EventType;
import com.example.event_indexer.repo.EventRepo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class EventController {
    private final EventRepo eventRepo;

    @GetMapping
    public List<EventEntity> getEvents(@RequestParam(required = false) EventType type) {
        return (type == null) ? eventRepo.findAll() : eventRepo.findByEventType(type);
    }

}
