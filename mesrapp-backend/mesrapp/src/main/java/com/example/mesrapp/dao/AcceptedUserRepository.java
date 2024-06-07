package com.example.mesrapp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mesrapp.model.AcceptedUser;

public interface AcceptedUserRepository extends JpaRepository<AcceptedUser, String> {
    List<AcceptedUser> findBySpotiId(String spotiId);
}
