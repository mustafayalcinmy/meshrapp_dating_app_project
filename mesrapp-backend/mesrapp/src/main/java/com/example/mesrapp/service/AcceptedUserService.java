package com.example.mesrapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mesrapp.dao.AcceptedUserRepository;
import com.example.mesrapp.model.AcceptedUser;
import com.example.mesrapp.model.IdMatch;
import com.example.mesrapp.model.Match;

@Service
public class AcceptedUserService {

    private final AcceptedUserRepository acceptedUserRepository;
    private final MatchService matchService;
    private final IdMatchService idMatchService;

    @Autowired
    public AcceptedUserService(AcceptedUserRepository acceptedUserRepository, MatchService matchService, IdMatchService idMatchService) {
        this.acceptedUserRepository = acceptedUserRepository;
        this.matchService = matchService;
        this.idMatchService = idMatchService;
    }

    public AcceptedUser saveAcceptedUser(String spotiId, String acceptedSpotiId) {
        List<IdMatch> userData = idMatchService.getMatchBySpotiId("mustafa5");
        System.out.println(userData);
        Match acceptedUserData = matchService.getMatchBySpotiId(acceptedSpotiId);
        AcceptedUser acceptedUser = new AcceptedUser();
        acceptedUser.setSpotiId(spotiId);
        acceptedUser.setAcceptedSpotiIds(acceptedUserData.getSpotiId());
        acceptedUser.setAcceptedMusicId(acceptedUserData.getMusicId());
        acceptedUser.setAcceptedArtistId(acceptedUserData.getArtistId());
        return acceptedUserRepository.save(acceptedUser);
    }

    public List<AcceptedUser> getAcceptedUsers(String spotiId) {
    return acceptedUserRepository.findBySpotiId(spotiId);
    }
}
