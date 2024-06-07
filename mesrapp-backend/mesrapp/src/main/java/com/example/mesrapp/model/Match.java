package com.example.mesrapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @Column(nullable = false, unique = true)
    private String spotiId;

    @Column(nullable = false)
    private String musicId;
    
    @Column(nullable = false)
    private String artistId;
}
