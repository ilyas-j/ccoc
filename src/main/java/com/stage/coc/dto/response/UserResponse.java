package com.stage.coc.dto.response;

import com.stage.coc.enums.TypeUser;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String nom;
    private String telephone;
    private TypeUser typeUser;
}