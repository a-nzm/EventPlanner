/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;
import com.events.planner.dto.UserDto;
import com.events.planner.entity.User;
import com.events.planner.mapper.impl.UserDtoEntityMapper;
import com.events.planner.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 *
 * @author MAU
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDtoEntityMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                userMapper,
                passwordEncoder
        );
    }

    @Test
    void create_shouldCreateUser() {
        UserDto dto = createDto();
        User entity = createEntity();
        User saved = createEntity();
        UserDto resultDto = createDto();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        when(userMapper.toEntity(dto))
                .thenReturn(entity);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(entity))
                .thenReturn(saved);

        when(userMapper.toDto(saved))
                .thenReturn(resultDto);

        UserDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(entity);
    }

    @Test
    void create_shouldThrowWhenEmailMissing() {
        UserDto dto = createDto();
        dto.setEmail(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.create(dto)
                );

        assertEquals(
                "Email is required.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenPasswordMissing() {
        UserDto dto = createDto();
        dto.setPassword(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.create(dto)
                );

        assertEquals(
                "Password is required.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenEmailAlreadyExists() {
        UserDto dto = createDto();
        User existing = createEntity();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(existing));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> userService.create(dto)
                );

        assertEquals(
                "Email already exists.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser() {
        User user = createEntity();
        UserDto dto = createDto();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(dto);

        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getById_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> userService.getById(99L)
                );

        assertEquals(
                "User not found.",
                exception.getMessage()
        );
    }

    @Test
    void getByEmail_shouldReturnUser() {
        User user = createEntity();
        UserDto dto = createDto();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(dto);

        UserDto result =
                userService.getByEmail("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void login_shouldReturnUserWhenCredentialsAreCorrect() {
        User user = createEntity();
        UserDto dto = createDto();

        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"))
                .thenReturn(true);

        when(userMapper.toDto(user))
                .thenReturn(dto);

        UserDto result =
                userService.login(
                        "test@test.com",
                        "password123"
                );

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void login_shouldThrowWhenPasswordIsWrong() {
        User user = createEntity();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.login(
                                "test@test.com",
                                "wrongPassword"
                        )
                );

        assertEquals(
                "Invalid email or password.",
                exception.getMessage()
        );
    }

    @Test
    void login_shouldThrowWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.login(
                                "missing@test.com",
                                "password123"
                        )
                );

        assertEquals(
                "Invalid email or password.",
                exception.getMessage()
        );
    }

    @Test
    void delete_shouldDeleteUser() {
        when(userRepository.existsById(1L))
                .thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(99L))
                .thenReturn(false);

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> userService.delete(99L)
                );

        assertEquals(
                "User not found.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .deleteById(anyLong());
    }

    private UserDto createDto() {
        UserDto dto = new UserDto();

        dto.setId(1L);
        dto.setName("Test");
        dto.setSurname("User");
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        dto.setAdmin(false);

        return dto;
    }

    private User createEntity() {
        User user = new User();

        user.setId(1L);
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setAdmin(false);

        return user;
    }
}
