package com.example.mesrapp.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mesrapp.model.User;
import com.example.mesrapp.model.dto.AddUserDto;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAll(Pageable pageable);
    List<User> findByUsernameContainingIgnoreCase(String username);
    boolean existsBySpotiId(String spotiId);
    boolean existsBySpotiEmail(String spotiEmail);
    User findBySpotiId(String spotiId);
    void deleteBySpotiId(String spotiId);
    List<User> findByIdNot(Integer id);
    AddUserDto save(AddUserDto user);
}