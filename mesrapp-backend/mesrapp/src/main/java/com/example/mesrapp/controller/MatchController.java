package com.example.mesrapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mesrapp.model.Match;
import com.example.mesrapp.service.MatchService;

@RestController
@RequestMapping("/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/add")
    public ResponseEntity<Match> addMatch(@RequestBody Match match) {
        Match savedMatch = matchService.saveMatch(match);
        return ResponseEntity.ok(savedMatch);
    }

    @GetMapping("/get/{spotiId}")
    public List<Match> getMatchingSpotiIds(@PathVariable String spotiId, @RequestParam String type) {
        return matchService.findMatchingSpotiIds(spotiId, type);
    }
/*
    @GetMapping("/get")
    public ResponseEntity<Match> getMatchBySpotiId(@PathVariable String spotiId) {
        return matchService.getMatchBySpotiId(spotiId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    } */
}
