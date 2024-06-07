package com.example.mesrapp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mesrapp.model.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Match findBySpotiId(String spotiId);
    List<Match> findByMusicId(String musicId);
    List<Match> findByArtistId(String artistId);
    List<Match> findByMusicIdAndSpotiId(String musicId, String spotiId);
    List<Match> findByArtistIdAndSpotiId(String artistId, String spotiId);
    List<Match> findByMusicIdAndSpotiIdNot(String musicId, String spotiId);
    List<Match> findByArtistIdAndSpotiIdNot(String artistId, String spotiId);
    List<Match> findByMusicIdAndArtistIdNot(String musicId, String artistId);
    List<Match> findByMusicIdAndArtistIdEquals(String musicId, String artistId);
}
