package com.events.planner.service;

import com.events.planner.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {

    UserDto create(UserDto dto);

    UserDto getById(Long id) ;

    Page<UserDto> getAll(int page, int size);

    UserDto getByEmail(String email);

    Page<UserDto> getByAdmin(boolean admin, int page, int size);

    UserDto update(Long id, UserDto dto);

    void delete(Long id) ;

    UserDto login(String email, String password);

    UserDto updateByEmail(String email, UserDto dto);
}
