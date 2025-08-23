package io.github.dirtyreadtest;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/23 23:11
 */
public class MyBatisFirstLevelCacheDirtyReadTest {
    @Test
    public void demonstrateDirtyRead() throws Exception {
        // 1. 获取SqlSessionFactory (假设你已经配置好了)
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        Long id = 1L;

        // 2. 打开第一个SqlSession（操作A）
        try (SqlSession sqlSessionA = sqlSessionFactory.openSession()) {
            UserMapper mapperA = sqlSessionA.getMapper(UserMapper.class);

            System.out.println("========= 操作A：第一次查询ID为1的用户 =========");
            // 第一次查询，会发送SQL到数据库，并将结果存入一级缓存
            User userFirstQuery = mapperA.selectUserById(id);
            System.out.println("第一次查询结果: " + userFirstQuery);

            // 3. 打开第二个SqlSession并执行更新（操作B）
            try (SqlSession sqlSessionB = sqlSessionFactory.openSession()) {
                UserMapper mapperB = sqlSessionB.getMapper(UserMapper.class);
                System.out.println("\n========= 操作B：更新ID为1的用户 =========");

                // 创建一个新对象进行更新，比如把年龄+1
                User userToUpdate = new User();
                userToUpdate.setId(id);
                userToUpdate.setName(userFirstQuery.getName());
                userToUpdate.setAge(userFirstQuery.getAge() + 1); // 年龄增加

                int rowsAffected = mapperB.updateUser(userToUpdate);
                sqlSessionB.commit(); // 【关键】提交事务，让更新生效
                System.out.println("更新了 " + rowsAffected + " 行数据");
                System.out.println("更新后的数据应为: " + userToUpdate);
            } // sqlSessionB 自动关闭

            System.out.println("\n========= 操作A：第二次查询ID为1的用户 =========");
            // 4. 第一个SqlSession再次查询同一数据
            // 【关键点】此时会发生什么？
            // 由于一级缓存存在，MyBatis不会发送SQL，而是直接返回缓存中的旧对象
            User userSecondQuery = mapperA.selectUserById(id);
            System.out.println("第二次查询结果: " + userSecondQuery);

            // 5. 验证两次查询结果是否是同一个对象（缓存）以及数据是否过时
            System.out.println("\n========= 验证 =========");
            System.out.println("两次查询是否是同一个对象实例: " + (userFirstQuery == userSecondQuery)); // 应该是true，证明取自缓存
            System.out.println("数据是否一致（是否出现脏读）: " + (userFirstQuery.getAge().equals(userSecondQuery.getAge()))); // 应该是true，但数据库的值已经变了

            // 6. 清空sqlSessionA的缓存，再次查询获取最新数据
            System.out.println("\n========= 操作A：清空缓存后再次查询 =========");
            sqlSessionA.clearCache(); // 【关键】手动清空一级缓存
            User userAfterClearCache = mapperA.selectUserById(id);
            System.out.println("清空缓存后查询结果: " + userAfterClearCache); // 这时应该是最新的数据

        } // sqlSessionA 自动关闭
    }
}
