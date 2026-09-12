package com.events.planner.service.impl;

import com.events.planner.dto.UserDto;
import com.events.planner.entity.User;
import com.events.planner.mapper.impl.UserDtoEntityMapper;
import com.events.planner.repository.UserRepository;
import com.events.planner.service.UserService;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    
    private static final String USER_NOT_FOUND = "User not found.";

    private final UserRepository userRepository;
    private final UserDtoEntityMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserDtoEntityMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new IllegalStateException("Email already exists.");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    @Override
    public Page<UserDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public UserDto getByEmail(String email){
        return userRepository.findByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    @Override
    public Page<UserDto> getByAdmin(boolean admin, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return userRepository.findByAdmin(admin, pageable).map(userMapper::toDto);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail().trim().toLowerCase());
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getSurname() != null) {
            user.setSurname(dto.getSurname());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setAdmin(dto.isAdmin());

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException(USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateByEmail(String email, UserDto dto) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();

            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    throw new IllegalStateException("Email already exists.");
                }
                user.setEmail(newEmail);
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getSurname() != null) {
            user.setSurname(dto.getSurname());
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
}
