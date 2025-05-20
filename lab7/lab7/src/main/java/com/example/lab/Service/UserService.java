package com.example.lab.Service;

import com.example.lab.DTO.UserDTO;
import com.example.lab.Exception.ResourceNotFoundException;
import com.example.lab.model.User;
import com.example.lab.Repository.UserRepository;
import com.example.lab.Cache.InMemoryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InMemoryCache cache;
    private final RequestCounterService requestCounter;

    @Autowired
    public UserService(UserRepository userRepository, InMemoryCache cache, RequestCounterService requestCounter) {
        this.userRepository = userRepository;
        this.cache = cache;
        this.requestCounter = requestCounter;
    }

    public User createUser(UserDTO userDTO) {
        requestCounter.increment();

        User user = new User();
        user.setUsername(userDTO.getUsername());
        return userRepository.save(user);
    }

    public List<User> createUsersBulk(List<UserDTO> userDTOs) {
        requestCounter.increment();

        return userDTOs.stream()
                .map(dto -> {
                    User user = new User();
                    user.setUsername(dto.getUsername());
                    return userRepository.save(user);
                })
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        requestCounter.increment();

        if (cache.containsUserKey(id)) {
            return Optional.of(cache.getUser(id));
        }
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        cache.putUser(id, user.get());
        return user;
    }

    public List<User> getAllUsers() {
        requestCounter.increment();

        return userRepository.findAll();
    }

    public User updateUser(Long id, UserDTO userDTO) {
        requestCounter.increment();

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(userDTO.getUsername());
            User updatedUser = userRepository.save(user);
            cache.putUser(id, updatedUser);
            return updatedUser;
        }
        return null;
    }

    public void deleteUser(Long id) {
        requestCounter.increment();

        userRepository.deleteById(id);
        cache.removeUser(id);
    }
}
