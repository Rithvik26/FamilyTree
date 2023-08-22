package com.ase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
/*
Data Transfer Object(dto) Design Pattern is a frequently used design pattern. It is basically used to pass data with multiple attributes in one shot from client to server, to avoid multiple calls to a remote server.
 */
@Data
@NoArgsConstructor
public class Result {
    String status;
    Object result;
}
