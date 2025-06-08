package com.example.lab.Service;

import com.example.lab.Cache.InMemoryCache;
import com.example.lab.DTO.UserDTO;
import com.example.lab.Exception.ResourceNotFoundException;
import com.example.lab.Repository.UserRepository;
import com.example.lab.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private UserService userService;

    @Mock
    private User mockUser;

    @BeforeEach
    void setup() {
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("mockUsername");
    }

    @Test
    void testCreateUser() {
        UserDTO userDTO = mock(UserDTO.class);
        when(userDTO.getUsername()).thenReturn("newUser");

        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User created = userService.createUser(userDTO);

        assertNotNull(created);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetUserByIdFromCache() {
        when(cache.containsUserKey(1L)).thenReturn(true);
        when(cache.getUser(1L)).thenReturn(mockUser);

        Optional<User> userOpt = userService.getUserById(1L);

        assertTrue(userOpt.isPresent());
        assertEquals("mockUsername", userOpt.get().getUsername());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void testGetUserByIdFromRepository() {
        when(cache.containsUserKey(2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockUser));

        Optional<User> userOpt = userService.getUserById(2L);

        assertTrue(userOpt.isPresent());
        verify(cache).putUser(2L, mockUser);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(cache.containsUserKey(3L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(3L));
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Collections.singletonList(mockUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void testUpdateUserExisting() {
        UserDTO userDTO = mock(UserDTO.class);
        when(userDTO.getUsername()).thenReturn("updatedName");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User updated = userService.updateUser(1L, userDTO);

        assertNotNull(updated);
        verify(cache).putUser(1L, updated);
    }

    @Test
    void testUpdateUserNotExisting() {
        UserDTO userDTO = mock(UserDTO.class);
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        User updated = userService.updateUser(4L, userDTO);

        assertNull(updated);
        verify(cache, never()).putUser(anyLong(), any());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        doNothing().when(cache).removeUser(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        verify(cache).removeUser(1L);
    }
}
