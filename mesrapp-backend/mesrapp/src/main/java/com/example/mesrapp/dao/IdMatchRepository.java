package com.example.mesrapp.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mesrapp.model.IdMatch;

@Repository
public interface IdMatchRepository extends JpaRepository<IdMatch, Long> {
    List<IdMatch> findBySpotiId(String spotiId);
    List<IdMatch> findBySpotiIdAndMatchedSpotiIdOrSpotiIdAndMatchedSpotiId(String spotiId1, String matchedSpotiId1, String spotiId2, String matchedSpotiId2);
    Optional<IdMatch> findBySpotiIdAndMatchedSpotiIdAndMatchedMusicIdAndMatchedArtistId(
        String spotiId, String acceptedSpotiId, String acceptedMusicId, String acceptedArtistId);
}
