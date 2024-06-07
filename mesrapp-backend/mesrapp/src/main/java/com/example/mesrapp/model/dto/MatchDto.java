package com.example.mesrapp.model.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String spotiId;
    private String musicId;
    private String artistId;
}
