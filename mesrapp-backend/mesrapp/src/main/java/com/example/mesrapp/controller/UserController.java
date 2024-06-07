package com.example.mesrapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.mesrapp.model.User;
import com.example.mesrapp.model.dto.AddUserDto;
import com.example.mesrapp.model.dto.UpdateImageUserDto;
import com.example.mesrapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    private static final String UPLOAD_FOLDER = "mesrapp/src/main/resources/static/images/";
    private static final String CHATS_UPLOAD_FOLDER = "mesrapp/src/main/resources/static/chatimages/";


    @PostMapping(path = "/users")
    public @ResponseBody ResponseEntity<String> addNewUser(@RequestBody AddUserDto user) {        
        User savedUser = userService.saveUser(user);
        if (savedUser != null) {
            return ResponseEntity.ok("Saved");
        }
        else {
            return ResponseEntity.badRequest().body("Not Saved");
        }
    }

    @PostMapping(path = "/users/addImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateImage(@RequestParam("user") String userJson, @RequestParam("file") MultipartFile file) {        
        if (StringUtils.isBlank(userJson)) {
            return ResponseEntity.badRequest().body("Gönderilen kullanıcı bilgileri eksik.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UpdateImageUserDto user = objectMapper.readValue(userJson, UpdateImageUserDto.class);
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Gönderilen dosya boş.");
            }
            Path path = Paths.get(UPLOAD_FOLDER + user.getImage());
            Files.write(path, file.getBytes());
            return ResponseEntity.ok("Saved Image");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Dosya yüklenirken bir hata oluştu.");
        }
    }


    @PostMapping(path = "/chats/{messageId}") 
    public @ResponseBody ResponseEntity<String> uploadImage(@PathVariable String messageId, @RequestBody MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Gönderilen dosya boş.");
            }
            Path path = Paths.get(CHATS_UPLOAD_FOLDER + messageId + ".jpg");
            Files.write(path, file.getBytes());
            return ResponseEntity.ok("Saved Image");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Dosya yüklenirken bir hata oluştu.");
        }
    }
    
    @GetMapping(path = "/users")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/users/{spotiId}")
    public @ResponseBody User updateUser(@PathVariable String spotiId, @RequestBody User updatedUser) {
        return userService.updateUser(spotiId, updatedUser);
    }

    @DeleteMapping("/users/{spotiId}")
    public @ResponseBody ResponseEntity<String> deleteUser(@PathVariable String spotiId) {
        return userService.deleteUser(spotiId);
    }

    @GetMapping(path = "/users/filter-by-name")
    public @ResponseBody List<User> getUsersByName(@RequestParam String name) {
        return userService.getUsersByUsername(name);
    }

    @GetMapping(path = "/users/check-spotify/{id}")
    public ResponseEntity<Boolean> checkSpotifyId(@PathVariable String id) {
        boolean exists = userService.isSpotiIdExists(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping(path = "/users/{id}")
    public @ResponseBody ResponseEntity<User> getUserBySpotiId(@PathVariable String id) {
        User user = userService.getUserBySpotiId(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
