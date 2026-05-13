package projecte3.projecte_3.dto;

import projecte3.projecte_3.model.Role;

public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private Role role;
    private String grade;
    private String course;
    private String observations;

    public UserRequestDTO() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
