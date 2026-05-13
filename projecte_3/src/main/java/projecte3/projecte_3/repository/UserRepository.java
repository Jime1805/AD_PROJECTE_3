package projecte3.projecte_3.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projecte3.projecte_3.model.Role;
import projecte3.projecte_3.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByRole(Role role);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
