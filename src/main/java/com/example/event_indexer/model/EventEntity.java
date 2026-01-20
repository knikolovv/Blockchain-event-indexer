package com.example.event_indexer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private BigInteger amount;
    private String fromAddress;
    private String toAddress;
    private String previousOwner;
    private String newOwner;

    public EventEntity(EventType eventType) {
        this.eventType = eventType;
    }
}
