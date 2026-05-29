package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    Page<Track> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
