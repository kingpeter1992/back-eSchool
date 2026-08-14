package com.king.eschool.shared.CurrentUser;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.king.eschool.Core.dtoResponse.SchoolInfo;
import com.king.eschool.Core.dtoResponse.UserResponse;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository repository;
    private final SchoolRepository schoolRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return repository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur authentifié introuvable en base de données."));
    }

    public UserResponse getCurrentUserPublic() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH NAME = " + auth.getName());

        User user = getCurrentUser();

return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getSlug)
                        .collect(Collectors.toSet())) // Convertit la liste en Set<String>
                .build();
    }

    public SchoolInfo getCurrentSchool() {
        User user = getCurrentUser();
        
        if (user.getSchoolId() == null) {
            return null; // Cas du Super Admin global
        }

        School school = schoolRepository.findById(user.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Établissement lié à l'utilisateur introuvable."));

        return SchoolInfo.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .email(school.getEmail())
                .phone(school.getPhone())
                .logoUrl(school.getLogoUrl())
                .currency(school.getCurrency())
                .timezone(school.getTimezone())
                .domain(school.getDomain())
                .build();
    }
}