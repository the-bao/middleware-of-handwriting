package io.github.myproxy;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/20 18:41
 */
public class Compiler {
    public static void compile(File javaFile){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // 获取文件管理器
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,null,null)){

//            File outputDir = new File("proxy/target/classes");
//            outputDir.mkdirs();
//
//            // 设置文件管理器的输出位置
//            fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
//                    Arrays.asList(outputDir));

            // 获取要编译的文件对象
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(List.of(javaFile));

            // 设置编译选选项
            List<String> options = Arrays.asList("-d","./proxy/target/classes");

            // 创建编译任务
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    null,
                    options,
                    null,
                    compilationUnits);

            // 执行编译
            boolean success = task.call();
            if (success){
                System.out.println("编译成功");
            }else {
                System.out.println("编译失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
