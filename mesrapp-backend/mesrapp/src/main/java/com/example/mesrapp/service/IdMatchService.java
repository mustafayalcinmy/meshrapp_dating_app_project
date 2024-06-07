package com.example.mesrapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mesrapp.dao.IdMatchRepository;
import com.example.mesrapp.model.IdMatch;
import com.example.mesrapp.model.Match;

@Service
public class IdMatchService {

    private final IdMatchRepository idMatchRepository;
    private final MatchService matchService;

    @Autowired
    public IdMatchService(IdMatchRepository idMatchRepository, MatchService matchService) {
        this.idMatchRepository = idMatchRepository;
        this.matchService = matchService;
    }

    public List<IdMatch> getMatchBySpotiId(String spotiId) {
        return idMatchRepository.findBySpotiId(spotiId);
    }

    public List<IdMatch> saveMatches(String spotiId) {
        Set<Match> matchSet = matchService.findMatchingSpotiIds2(spotiId);
        System.out.println(matchSet);
        
        List<IdMatch> savedMatches = new ArrayList<>();
        
        for (Match match : matchSet) {
            Optional<IdMatch> existingMatch = idMatchRepository.findBySpotiIdAndMatchedSpotiIdAndMatchedMusicIdAndMatchedArtistId(
                    spotiId, match.getSpotiId(), match.getMusicId(), match.getArtistId());
            
            if (!existingMatch.isPresent()) {
                IdMatch newMatch = new IdMatch();
                newMatch.setSpotiId(spotiId);
                newMatch.setMatchedSpotiId(match.getSpotiId());
                newMatch.setMatchedMusicId(match.getMusicId());
                newMatch.setMatchedArtistId(match.getArtistId());
                savedMatches.add(idMatchRepository.save(newMatch));
            }
        }
        
        return getMatchBySpotiId(spotiId);
    }

        public void deleteMatch(String spotiId1, String spotiId2) {
        List<IdMatch> matchesToDelete = idMatchRepository.findBySpotiIdAndMatchedSpotiIdOrSpotiIdAndMatchedSpotiId(
                spotiId1, spotiId2, spotiId2, spotiId1);
        idMatchRepository.deleteAll(matchesToDelete);
    }

}
