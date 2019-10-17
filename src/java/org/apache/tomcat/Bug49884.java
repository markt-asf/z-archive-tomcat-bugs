package org.apache.tomcat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = "/bug49884")
public class Bug49884 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static ExecutorService executorService_;

    static {
        executorService_ = Executors.newFixedThreadPool(10);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final AsyncContext context = request.startAsync();
        executorService_.execute(new Worker(context));
    }

    private class Worker implements Runnable {

        private AsyncContext context_;

        public Worker(AsyncContext context) {
            context_ = context;
        }

        @Override
        public void run() {
            try {
                HttpServletResponse response = (HttpServletResponse) context_.getResponse();
                service(response);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                context_.complete();
            }
        }

        protected void service(HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("Hello World");
        }
    }
}