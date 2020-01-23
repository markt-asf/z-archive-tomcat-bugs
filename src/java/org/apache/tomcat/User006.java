package org.apache.tomcat;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = "/user006")
public class User006 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        final AsyncContext ac = request.startAsync();
        ac.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                System.out.println("AppAsyncListener onComplete");
            }


            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                System.out.println("AppAsyncListener onTimeout");
                AsyncContext ac = event.getAsyncContext();
                ac.complete();
            }


            @Override
            public void onError(AsyncEvent event) throws IOException {
                System.out.println("AppAsyncListener onError");
            }


            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                System.out.println("AppAsyncListener onStart");
            }
        });
        ac.setTimeout(2000);
        ac.start(new Runnable() {

            @Override
            public void run() {
                try (PrintWriter out = ac.getResponse().getWriter()) {
                    for (int i = 0; i < 10; i++) {
                        try {
                            String text = "Counter :" + i + "\n";
                            out.write(text);
                            out.flush();
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            System.out.println("Thread interrupted");
                            ex.printStackTrace();
                        }
                    }
                    ac.complete();
                } catch (IOException ex) {
                    System.out.println("Thread IOException");
                    ex.printStackTrace();
                }
            }
        });
    }
}
