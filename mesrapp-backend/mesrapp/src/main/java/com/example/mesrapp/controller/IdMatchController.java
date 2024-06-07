package com.example.mesrapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mesrapp.model.IdMatch;
import com.example.mesrapp.service.IdMatchService;

@RestController
@RequestMapping("/idMatches")
public class IdMatchController {
    @Autowired
    private IdMatchService idMatchService;

    @GetMapping("/get/{spotiId}")
    public List<IdMatch> getMatchingSpotiIds(@PathVariable String spotiId) {
        return idMatchService.saveMatches(spotiId);
    }

    @DeleteMapping("/deleteMatch/{spotiId1}/{spotiId2}")
    public ResponseEntity<String> deleteMatch(@PathVariable String spotiId1, @PathVariable String spotiId2) {
        idMatchService.deleteMatch(spotiId1, spotiId2);
        return ResponseEntity.ok("Match deleted for spotiId1: " + spotiId1 + " and spotiId2: " + spotiId2);
    }
}
