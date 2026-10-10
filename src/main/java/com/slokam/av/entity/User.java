package com.slokam.av.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(length = 36)
    public String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 150)
    public String name = "Movie lover";

    @Column(nullable = false, unique = true, length = 20)
    public String mobile;

    @Column(length = 20)
    public String alternativeMobile;

    @Column(unique = true)
    public String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Role role = Role.USER;

    @Column(nullable = false)
    public boolean enabled = true;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    @Column(nullable = false)
    public Instant updatedAt = Instant.now();

    @PreUpdate
    void updateTime() {
        updatedAt = Instant.now();
    }
}
