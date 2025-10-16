package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String username;
    private User.Role role;
    private Long patientId; // null for admin users
    private String message;
    
    public AuthResponse(Long userId, String username, User.Role role, Long patientId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.patientId = patientId;
    }
}
