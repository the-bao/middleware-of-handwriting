package io.github.web;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 10:46
 */
public class EmbeddedTomcatDemo {
    public static void main(String[] args) throws LifecycleException {
        int port = 9090;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath,docBase);

        tomcat.addServlet(contextPath, "helloServlet", new HttpServlet() {
                    @Override
                    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                        resp.setContentType("text/html");
                        resp.getWriter().write("<h1>Hello from Embedded Tomcat!</h1><br>" + req.getRequestURL().toString());
                    }
                });

        context.addServletMappingDecoded("/*","helloServlet");
        tomcat.start();
        System.out.println("tomcat start... port :" + port);
    }
}
