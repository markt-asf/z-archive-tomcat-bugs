package org.apache.tomcat;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = "/work001")
public class Work001 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(Thread.currentThread() + " Request received");
        AsyncContext async = req.startAsync();
        try {
            System.out.println("Sleeping for 15 seconds");
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            throw new RuntimeException();
        }
        System.out.println("Async complete");
        async.complete();
    }
}
