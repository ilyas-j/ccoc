package com.stage.coc.dto.response;

import com.stage.coc.enums.TypeUser;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private TypeUser typeUser;
}