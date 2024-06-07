package com.example.mesrapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Entity
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    private String image;
    
    @Column(nullable = false, unique = true)
    private String spotiId;
    
    @Column(unique = true)
    private String spotiEmail;
    
    private String bio;
    @Column(nullable = false)
    private String gender;
    private String instagram;
    private String dateTime;

    @Column(nullable = true)
    private String location;

    public User(String username, String spotiId, String spotiEmail, String bio, String gender, String instagram, String dateTime, String location) {
    this.username = username;
    this.spotiId = spotiId;
    this.spotiEmail = spotiEmail;
    this.bio = bio;
    this.gender = gender;
    this.instagram = instagram;
    this.dateTime = dateTime;
    this.location = location;
    }
}

