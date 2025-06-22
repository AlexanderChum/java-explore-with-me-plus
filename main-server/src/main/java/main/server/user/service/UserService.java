package main.server.user.service;

import main.server.user.dto.NewUserDto;
import main.server.user.dto.UpdateUserDto;
import main.server.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    void deleteUserById(Long userId);

    Page<UserDto> getUsers(List<Long> userIds, PageRequest pageable);

    UserDto updateUser(Long userId, UpdateUserDto updateUserDto);
}