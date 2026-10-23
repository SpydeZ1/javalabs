package org.example.repository;

import org.example.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends InMemoryRepository<User> {

    public UserRepository() {
        super(User::getId, User::setId);
    }

    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return stream().filter(u -> username.equals(u.getUsername())).findFirst().orElse(null);
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }
}
