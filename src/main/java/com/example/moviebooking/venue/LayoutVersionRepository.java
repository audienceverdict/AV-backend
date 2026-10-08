package com.example.moviebooking.venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LayoutVersionRepository extends JpaRepository<LayoutVersion,String> { List<LayoutVersion> findByScreenIdOrderByVersionNumberDesc(String screenId); Optional<LayoutVersion> findByScreenIdAndVersionNumber(String screenId,int version); Optional<LayoutVersion> findFirstByScreenIdAndStatusOrderByVersionNumberDesc(String screenId, LayoutVersionStatus status); }
