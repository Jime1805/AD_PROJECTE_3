package projecte3.projecte_3.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import projecte3.projecte_3.dto.UserRequestDTO;
import projecte3.projecte_3.dto.UserResponseDTO;
import projecte3.projecte_3.model.Role;
import projecte3.projecte_3.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable String id) {
        UserResponseDTO user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> findByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> findByUsername(@PathVariable String username) {
        UserResponseDTO user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@RequestBody UserRequestDTO request) {
        UserResponseDTO created = userService.create(request);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable String id,
                                                   @RequestBody UserRequestDTO request) {
        UserResponseDTO updated = userService.update(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
