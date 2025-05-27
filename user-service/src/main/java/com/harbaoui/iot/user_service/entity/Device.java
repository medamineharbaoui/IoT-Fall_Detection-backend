package com.harbaoui.iot.user_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "user_id")
    //@ToString.Exclude // Prevent circular reference in logs
    //@EqualsAndHashCode.Exclude
    //@JsonIgnoreProperties("devices") // Ignore user devices when serializing
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("devices") 
    private User user;
}
