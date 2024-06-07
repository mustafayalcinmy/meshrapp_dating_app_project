package com.example.mesrapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.mesrapp.dao.UserRepository;
import com.example.mesrapp.model.User;
import com.example.mesrapp.model.dto.AddUserDto;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User saveUser(AddUserDto userDto) {
        if (isSpotiEmailExists(userDto.getSpotiEmail())) {
            throw new DataIntegrityViolationException("E-posta already in use");
        }
        User user = new User(userDto.getUsername(), userDto.getSpotiId(), userDto.getSpotiEmail(), userDto.getBio(), userDto.getGender(), userDto.getInstagram(), userDto.getDateTime(), userDto.getLocation());

        return userRepository.save(user);
    }

    @Override
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(String spotiId, User updatedUser) {
        User user = userRepository.findBySpotiId(spotiId);
        if (user != null) {
            if (updatedUser.getUsername() != null) {
                user.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getImage() != null) {
                user.setImage(updatedUser.getImage());
            }
            if (updatedUser.getSpotiId() != null) {
                user.setSpotiId(updatedUser.getSpotiId());
            }
            if (updatedUser.getSpotiEmail() != null) {
                user.setSpotiEmail(updatedUser.getSpotiEmail());
            }
            if (updatedUser.getBio() != null) {
                user.setBio(updatedUser.getBio());
            }
            if (updatedUser.getGender() != null) {
                user.setGender(updatedUser.getGender());
            }
            if (updatedUser.getDateTime() != null) {
                user.setDateTime(updatedUser.getDateTime());
            }
            if (updatedUser.getInstagram() != null) {
                user.setInstagram(updatedUser.getInstagram());
            }
            if (updatedUser.getLocation() != null) {
                user.setLocation(updatedUser.getLocation());
            }
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<String> deleteUser(String spotiId) {
        User user = userRepository.findBySpotiId(spotiId);
        if (user != null) {
            userRepository.deleteBySpotiId(spotiId);
            return ResponseEntity.ok(spotiId + " " + user.getUsername() + " Deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(spotiId + " Not Found");
        }
    }
    

    @Override
    public List<User> getUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    @Override
    public boolean isSpotiEmailExists(String spotiEmail) {
        return userRepository.existsBySpotiEmail(spotiEmail);
    }

    @Override
    public boolean isSpotiIdExists(String spotiId) {
        return userRepository.existsBySpotiId(spotiId);

    }

    @Override
    public User getUserBySpotiId(String id) {
        User user = userRepository.findBySpotiId(id);
        if (user != null) {
            return user;
        }
        return null;
    }
}
