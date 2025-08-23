package io.github;

import io.github.annotation.Param;
import io.github.annotation.Table;

import java.lang.reflect.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/23 16:27
 */
public class MySqlSessionFactory {
    private static final String JDBCURL = "jdbc:mysql://localhost:3306/test";
    private static final String DBUSER = "root";
    private static final String PASSWORD = "mysql@123#";

    @SuppressWarnings("all")
    public <T> T getMapper(Class<T> mapperClass){
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{mapperClass}, new MapperInvocationHandler());
    }

    static class MapperInvocationHandler implements InvocationHandler{
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("select")){
                return invokeSelect(proxy,method,args);
            }
            return null;
        }

        private Object invokeSelect(Object proxy, Method method, Object[] args) {
            String sql = createSelectSql(method);
            try (Connection conn = DriverManager.getConnection(JDBCURL,DBUSER,PASSWORD);
                 PreparedStatement statement = conn.prepareStatement(sql)){

                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer){
                        statement.setInt(i+1,
                                (Integer) arg);
                    }else if (arg instanceof String){
                        statement.setString(i+1,
                                (String) arg);
                    }
                }

                ResultSet rs = statement.executeQuery();
                if (rs.next()){
                    return parseResult(rs,method.getReturnType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // TODO 支持返回多条结果
        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            Field[] declaredField = returnType.getDeclaredFields();
            for (Field field:declaredField){
                Object column = null;
                String name = field.getName();
                if (field.getType() == String.class){
                    column = rs.getString(name);
                }else if (field.getType() == Integer.class){
                    column = rs.getInt(name);
                }
                field.setAccessible(true);
                field.set(result,column);
            }

            return result;
        }

        private String createSelectSql(Method method) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            List<String> selectCols = getSelectCols(method.getReturnType());
            sqlBuilder.append(String.join(",",selectCols));

            sqlBuilder.append(" from ");
            String table = getSelectTableName(method.getReturnType());
            sqlBuilder.append(table);

            sqlBuilder.append(" where ");
            String where = getSelectWhere(method);
            sqlBuilder.append(where);

            return sqlBuilder.toString();
        }

        private String getSelectWhere(Method method) {
            return Arrays.stream(method.getParameters())
                    .map((parameter) -> {
                        Param param = parameter.getAnnotation(Param.class);
                        String column = param.value();
                        return column + " = ?";
                    }).collect(Collectors.joining(" and "));
        }

        private String getSelectTableName(Class<?> returnType) {
            Table table = returnType.getAnnotation(Table.class);
            if (table == null){
                throw new RuntimeException("返回值无法确定查询表");
            }
            return table.tableName();
        }

        private List<String> getSelectCols(Class<?> returnType) {
            // 返回值的属性
            Field[] declaredFields = returnType.getDeclaredFields();
            return Arrays.stream(declaredFields).map(Field::getName).toList();
        }
    }
}
