package com.ase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FPUpdatePInput {
    private String email;
    private String emailtoken;
    private String password;
    private String cpassword;

}
