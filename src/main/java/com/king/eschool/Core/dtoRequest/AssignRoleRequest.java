package com.king.eschool.Core.dtoRequest;


import java.util.List;

import com.king.eschool.Modules.Utilisateurs.Models.Role;

import lombok.Data;

@Data
public class AssignRoleRequest {
    private List<Role> roles;
}
