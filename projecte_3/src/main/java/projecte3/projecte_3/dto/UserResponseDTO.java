package projecte3.projecte_3.dto;

import projecte3.projecte_3.model.Role;
import java.time.LocalDateTime;

public class UserResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Role role;
    private LocalDateTime dataCreated;
    private AcademicProfileDTO academicProfile;

    public UserResponseDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDataCreated() { return dataCreated; }
    public void setDataCreated(LocalDateTime dataCreated) { this.dataCreated = dataCreated; }

    public AcademicProfileDTO getAcademicProfile() { return academicProfile; }
    public void setAcademicProfile(AcademicProfileDTO academicProfile) { this.academicProfile = academicProfile; }
}
