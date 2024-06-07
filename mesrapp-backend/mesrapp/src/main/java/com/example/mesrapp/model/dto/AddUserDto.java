package com.example.mesrapp.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AddUserDto {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String spotiId;

    @Column(nullable = false, unique = true)
    private String spotiEmail;

    private String bio;

    @Column(nullable = false)
    private String gender;

    private String instagram;

    private String dateTime;

    @Column(nullable = true)
    private String location;
}