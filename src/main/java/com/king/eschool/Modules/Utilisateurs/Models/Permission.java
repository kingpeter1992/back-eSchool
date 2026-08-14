package com.king.eschool.Modules.Utilisateurs.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

       @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, unique = true, length = 100)
    private String code;
}