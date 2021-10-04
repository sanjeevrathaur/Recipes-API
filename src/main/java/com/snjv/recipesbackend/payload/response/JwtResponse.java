package com.snjv.recipesbackend.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

    private String token;

    private String type = "Bearer";

    @JsonIgnore
    private String id;

    @JsonIgnore
    private String username;

    @JsonIgnore
    private List<String> roles;

    public JwtResponse(String accessToken, String id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

}
