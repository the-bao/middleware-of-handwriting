package io.github;

import java.sql.*;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/23 16:25
 */
public class Main {
    private static final String JDBCURL = "jdbc:mysql://localhost:3306/test";
    private static final String DBUSER = "root";
    private static final String PASSWORD = "mysql@123#";

    public static void main(String[] args) {
//        System.out.println(jdbcSelectById(1));
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        UserMapper mapper = mySqlSessionFactory.getMapper(UserMapper.class);
        User user = mapper.selectById(1);
        System.out.println(user);
        System.out.println(mapper.selectByString("rty"));
        System.out.println(mapper.selectByNameAndAge("rty",22));
    }

    private static User jdbcSelectById(int id){


        String sql = "SELECT id, name, age FROM user WHERE id =?";
        try (Connection conn = DriverManager.getConnection(JDBCURL,DBUSER,PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)){
            statement.setInt(1,id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setAge(rs.getInt("age"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
