package io.github.dirtyreadtest;

public interface UserMapper {
    User selectUserById(Long id);
    int updateUser(User user);
}
