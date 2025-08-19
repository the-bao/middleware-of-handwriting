package io.github.web;

import com.alibaba.fastjson2.JSONObject;
import io.github.annotation.*;
import io.github.spring.BeanPostProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rty
 * @version 1.0
 * @description: 分发请求
 * @date 2025/8/17 11:20
 */
@Component
public class DispatcherServlet extends HttpServlet implements BeanPostProcessor {

    // 拦截器注册中心
    @Autowired
    InterceptorRegistry interceptorRegistry;

    // uri 和 handler 的映射
    Map<String,WebHandler> handlerMap = new HashMap<>();

    // uri 和 interceptor 的映射
    Map<String,List<Object>> interceptorMap = new HashMap<>();

    // 注册的 adapter
    List<HandlerAdapter> adapterList = new ArrayList<>();

    // 匹配#{...}格式的占位符
    private static final Pattern pattern = Pattern.compile("#\\{([^}]+)\\}");

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        // afterInitializeBean 会在每个bean实例化后都调用一次进行判断是否是controller注解的类
        if (bean.getClass().isAnnotationPresent(Controller.class)){
            // 注册 RequestMapping 标记过的bean
            RequestMapping classRm = bean.getClass().getAnnotation(RequestMapping.class);
            String classurl = classRm != null ? classRm.value() : "";

            Arrays.stream(bean.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                    .forEach(method -> {
                        RequestMapping methodRm = method.getAnnotation(RequestMapping.class);
                        String key = classurl.concat(methodRm.value());
                        WebHandler value = new WebHandler(bean,method);
                        if (handlerMap.put(key,value) != null){
                            throw new RuntimeException("controller 定义重复" + key);
                        }
                    });

            return bean;
        }

        // 注册 interceptor
        // TODO 实现集中管理拦截器注册 InterceptorRegistry
        if (bean.getClass().isAnnotationPresent(Interceptor.class) && bean instanceof HandlerInterceptor){
            Interceptor interceptor = bean.getClass().getAnnotation(Interceptor.class);
            String[] uris = interceptor.value();
            for (String uri:uris) {
                List<Object> value = interceptorMap.getOrDefault(uri,new ArrayList<>());
                value.add(bean);
                interceptorMap.put(uri,value);
            }
        }

        return bean;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebHandler webHandler = findHandler(req);

        if (webHandler == null){
            // 没有对应的handler可以处理 返回error页面
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<h1>Error No Resource Were Found</h1><br>" + req.getRequestURL().toString());
            return;
        }

        // 拿到 HandlerExecutionChain
        HandlerExecutionChain chain = new HandlerExecutionChain(webHandler);
        List<Object> interceptors = findInterceptors(req);
        if (interceptors.size() > 0){
            for (int i = 0; i < interceptors.size(); i++) {
                chain.addInterceptor((HandlerInterceptor) interceptors.get(i));
            }
        }

        // 使用 HandlerExecutionChain 获取到合适的 HandlerAdapter
        // 执行 handle 方法
        // HandlerAdapter adapter = findAdapter(webHandler);

        // TODO 加入applyPostHandle执行时机
        try {
            if (chain.applyPreHandle(req, resp)){
                Method method = webHandler.getMethod();
                Object controller = webHandler.getControllerBean();
                Object[] args = resolveArgs(req,method);
                Object result = method.invoke(controller,args);
                switch (webHandler.getResultType()){
                    case HTML -> {
                        resp.setContentType("text/html;charset=UTF-8");
                        resp.getWriter().write(result.toString());
                    }
                    case JSON -> {
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(JSONObject.toJSONString(result));
                    }
                    case LOCAL -> {
                        ModelAndView mv = (ModelAndView) result;
                        String view = mv.getView();
                        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(view);
                        try (resourceAsStream){
                            String html = new String(resourceAsStream.readAllBytes());
                            html = renderTemplate(html,mv.getContext());
                            resp.setContentType("text/html;charset=UTF-8");
                            resp.getWriter().write(html);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }finally {
            chain.triggerAfterCompletion(req,resp,null);
        }

    }

    private HandlerAdapter findAdapter(WebHandler webHandler) {
        return adapterList.stream()
                .filter(adapter -> adapter.supports(webHandler))
                .findFirst()
                .orElse(null);
    }

    /*
    * 模板解析
    * */
    private String renderTemplate(String html, Map<String, String> context) {
        Matcher matcher = pattern.matcher(html);

        // 使用 StringBuffer 构建结果
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1); // 提取 key（去掉 #{ 和 }）
            String value = context.getOrDefault(key, ""); // 获取对应的 value
            matcher.appendReplacement(result, value); // 替换
        }
        matcher.appendTail(result); // 追加剩余部分

        return result.toString();
    }

    /*
    * 解析方法需要的参数
    * */
    private Object[] resolveArgs(HttpServletRequest req, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = parameters[i];
            String value = req.getParameter(parameter.getAnnotation(Param.class).value());
            Class<?> type = parameter.getType();
            if (String.class.isAssignableFrom(type)){
                args[i] = value;
            }else if (Integer.class.isAssignableFrom(type)){
                args[i] = Integer.parseInt(value);
            }else {
                throw new IllegalArgumentException("暂不支持此类型参数解析");
            }
        }
        return args;
    }

    /*
    * 根据请求找到对应的方法处理(webhandler)
    * */
    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    private List<Object> findInterceptors(HttpServletRequest req){
        return interceptorMap.get(req.getRequestURI());
    }

}
