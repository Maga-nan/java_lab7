package com.example.lab.Service;

import com.example.lab.DTO.UserDTO;
import com.example.lab.Model.User;
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
    private final RequestCounterService requestCounterService;

    @Autowired
    public UserService(UserRepository userRepository, InMemoryCache cache, RequestCounterService requestCounterService) {
        this.userRepository = userRepository;
        this.cache = cache;
        this.requestCounterService = requestCounterService;
    }

    public User createUser(UserDTO userDTO) {
        requestCounterService.increment();
        User user = new User();
        user.setUsername(userDTO.getUsername());
        User savedUser = userRepository.save(user);
        cache.putUser(savedUser.getId(), savedUser);
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        requestCounterService.increment();
        if (cache.containsUserKey(id)) {
            return Optional.of(cache.getUser(id));
        }

        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> cache.putUser(u.getId(), u));
        return user;
    }

    public List<User> createUsersBulk(List<UserDTO> userDTOs) {
        requestCounterService.increment();
        List<User> users = userDTOs.stream()
                .map(dto -> {
                    User user = new User();
                    user.setUsername(dto.getUsername());
                    return user;
                })
                .collect(Collectors.toList());

        List<User> savedUsers = userRepository.saveAll(users);

        savedUsers.forEach(user -> cache.putUser(user.getId(), user));

        return savedUsers;
    }

    public List<User> getAllUsers() {
        requestCounterService.increment();
        return userRepository.findAll().stream()
                .peek(user -> cache.putUser(user.getId(), user))
                .collect(Collectors.toList());
    }

    public User updateUser(Long id, UserDTO userDTO) {
        requestCounterService.increment();
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(userDTO.getUsername());
            User updatedUser = userRepository.save(user);
            cache.putUser(updatedUser.getId(), updatedUser);
            cache.removeUserConversions(id);
            return updatedUser;
        }
        return null;
    }

    public void deleteUser(Long id) {
        requestCounterService.increment();
        userRepository.deleteById(id);
        cache.removeUser(id);
        cache.removeUserConversions(id);
    }
}
