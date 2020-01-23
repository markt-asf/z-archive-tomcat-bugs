package org.apache.tomcat;

import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class Bug64021SCI implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        System.out.println("Bug64021SCI started");
        System.out.println(ctx.getAttribute("jakarta.websocket.server.ServerContainer"));
    }
}
