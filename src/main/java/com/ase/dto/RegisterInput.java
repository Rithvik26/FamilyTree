package com.ase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RegisterInput {
    private String id;
    private String name;
    private String email;
    private String password;
    private String cpassword;
}
