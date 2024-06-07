package com.example.mesrapp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mesrapp.dao.MatchRepository;
import com.example.mesrapp.model.Match;


@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public Match getMatchBySpotiId(String spotiId) {
        return matchRepository.findBySpotiId(spotiId);
    }

    public List<Match> findMatchingSpotiIds(String spotiId, String type) {
        Match userMatch = matchRepository.findBySpotiId(spotiId);
        if ("music".equals(type)) {
            List<Match> matches = matchRepository.findByMusicIdAndArtistIdEquals(userMatch.getMusicId(), userMatch.getArtistId());
            return matches;
        }
        else if ("artist".equals(type)) {
            List<Match> matches = matchRepository.findByMusicIdAndArtistIdNot(userMatch.getMusicId(), userMatch.getArtistId());
            return matches;
        }
        else {
            System.out.println(type);
            return null;
        }
    }

    public Set<Match> findMatchingSpotiIds2(String spotiId) {
        Match userMatch = matchRepository.findBySpotiId(spotiId);
        Set<Match> matches = new HashSet<>();

        // Kullanıcının dinlediği müzikler ve sanatçılar
        String musicId = userMatch.getMusicId();
        String artistId = userMatch.getArtistId();

        // Aynı müziklere sahip diğer kullanıcıları bul
        List<Match> musicMatches = matchRepository.findByMusicIdAndSpotiIdNot(musicId, spotiId);
        matches.addAll(musicMatches);

        // Aynı sanatçılara sahip diğer kullanıcıları bul
        List<Match> artistMatches = matchRepository.findByArtistIdAndSpotiIdNot(artistId, spotiId);
        matches.addAll(artistMatches);

        return matches;
    }

    public Match getUserBySpotiId(String spotiId) {
        Match match = matchRepository.findBySpotiId(spotiId);
        if (match != null) {
            return match;
        }
        return null;
    }
}
