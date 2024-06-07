package com.example.mesrapp.model.dto;

import jakarta.persistence.Column;
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
public class UpdateImageUserDto {
    private String image;
    @Column(nullable = false, unique = true)
    private String spotiId;
}
