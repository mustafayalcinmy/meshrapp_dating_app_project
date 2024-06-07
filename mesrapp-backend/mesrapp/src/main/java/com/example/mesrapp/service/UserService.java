package com.example.mesrapp.service;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.example.mesrapp.model.User;
import com.example.mesrapp.model.dto.AddUserDto;

public interface UserService {
    User saveUser(AddUserDto user);
    Iterable<User> getAllUsers();
    User updateUser(String spotiId, User updatedUser);
    ResponseEntity<String> deleteUser(String spotiId);
    public User getUserBySpotiId(String id);
    public boolean isSpotiEmailExists(String spotiEmail);
    boolean isSpotiIdExists(String spotiId);
    public List<User> getUsersByUsername(String username);
}
