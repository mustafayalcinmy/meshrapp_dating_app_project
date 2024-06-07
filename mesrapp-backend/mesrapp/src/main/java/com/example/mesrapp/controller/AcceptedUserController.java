package com.example.mesrapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mesrapp.model.AcceptedUser;
import com.example.mesrapp.service.AcceptedUserService;

@RestController
@RequestMapping("/acceptedUsers")
public class AcceptedUserController {

    private final AcceptedUserService acceptedUserService;

    @Autowired
    public AcceptedUserController(AcceptedUserService acceptedUserService) {
        this.acceptedUserService = acceptedUserService;
    }

    @PostMapping("/add")
    public AcceptedUser addAcceptedUser(@RequestParam String spotiId, 
                                        @RequestParam String acceptedSpotiId) {
        return acceptedUserService.saveAcceptedUser(spotiId, acceptedSpotiId);
    }

    @GetMapping("/get/{spotiId}")
    public List<AcceptedUser> getAcceptedUsers(@PathVariable String spotiId) {
        return acceptedUserService.getAcceptedUsers(spotiId);
    }
}
