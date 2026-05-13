package projecte3.projecte_3.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import projecte3.projecte_3.model.User;

public interface UserRepository extends MongoRepository<User, String>{

}
