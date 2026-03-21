package com.volunteer.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ResetPasswordDTO {
    
    @NotBlank(message = "Username cannot be empty")
    private String username;
    
    /**
     * Verification Answer:
     * For Volunteers: Phone number
     * For Organizers: Organization Name
     */
    @NotBlank(message = "Verification info cannot be empty")
    private String securityAnswer;
    
    @NotBlank(message = "New password cannot be empty")
    private String newPassword;
}
