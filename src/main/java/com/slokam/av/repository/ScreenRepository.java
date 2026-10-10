package com.slokam.av.repository;

import com.slokam.av.entity.Screen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ScreenRepository extends JpaRepository<Screen, String> {
    List<Screen> findByTheatreId(String theatreId);
}
