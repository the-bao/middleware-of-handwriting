package io.github.staticprxoy;

/**
 * @author rty
 * @version 1.0
 * @description: 目标类
 * @date 2025/8/17 17:53
 */
public class UserServiceImpl implements UserService{
    @Override
    public void saveUser(String name) {
        System.out.println("保存用户：" + name);
    }
}
