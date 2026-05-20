package projecte3.projecte_3.service;

import org.springframework.stereotype.Service;
import projecte3.projecte_3.dto.UserRequestDTO;
import projecte3.projecte_3.dto.UserResponseDTO;
import projecte3.projecte_3.mapper.UserMapper;
import projecte3.projecte_3.model.Role;
import projecte3.projecte_3.model.User;
import projecte3.projecte_3.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findById(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(userMapper::toDto).orElse(null);
    }

    public List<UserResponseDTO> findByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(userMapper::toDto).orElse(null);
    }

    public UserResponseDTO create(UserRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return null;
        }

        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserResponseDTO update(String id, UserRequestDTO request) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return null;
        }

        User user = existing.get();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        if (request.getGrade() != null) {
            user.setAcademicProfile(
                new projecte3.projecte_3.model.AcademicProfile(
                    request.getGrade(),
                    request.getCourse(),
                    request.getObservations(),
                    "ACTIVE"
                )
            );
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public boolean delete(String id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}