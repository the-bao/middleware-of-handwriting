package io.github;

import io.github.annotation.Param;

public interface UserMapper {
    User selectById(@Param("id") int id);

    User selectByString(@Param("name") String name);

    User selectByNameAndAge(@Param("name") String name,@Param("age") int age);
}
