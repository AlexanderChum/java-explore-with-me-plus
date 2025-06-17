package main.server.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.exception.DuplicatedDataException;
import main.server.exception.NotFoundException;
import main.server.user.UserMapper;
import main.server.user.UserRepository;
import main.server.user.dto.NewUserDto;
import main.server.user.dto.UpdateUserDto;
import main.server.user.dto.UserDto;
import main.server.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserDto newUserDto) {
        validateEmailExist(newUserDto.getEmail());
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(newUserDto)));
    }

    @Override
    public void deleteUserById(Long userId) {
        validateUserExist(userId);
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserDto> getUsers(List<Long> userIds, PageRequest pageable) {
        if (userIds == null || userIds.isEmpty()) {
            return userMapper.toUserDtoPage(userRepository.findAll(pageable).getContent(), pageable);
        } else {
            return userMapper.toUserDtoPage(userRepository.findByIdIn(userIds, pageable).getContent(), pageable);
        }
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUserDto updateUserDto) {
        User user = validateUserExist(userId);
        validateEmailExist(updateUserDto.getEmail(), userId);
        updateUserFields(user, updateUserDto);
        return (userMapper.toUserDto(userRepository.save(user)));
    }

    private void validateEmailExist(String email, Long currentUserId) {
        Optional<User> alreadyExistUser = userRepository.findByEmail(email);
        if (alreadyExistUser.isPresent() && !alreadyExistUser.get().getId().equals(currentUserId)) {
            throw new DuplicatedDataException(String.format("Email - %s уже используется", email));
        }
    }

    private void validateEmailExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicatedDataException(String.format("Email - %s уже используется", email));
        }
    }

    private User validateUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", userId)));
    }

    private void updateUserFields(User user, UpdateUserDto updateUserDto) {
        if (updateUserDto.hasEmail()) {
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.hasName()) {
            user.setName(updateUserDto.getName());
        }
    }
}
