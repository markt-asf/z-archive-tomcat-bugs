package org.apache.tomcat;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/bug63003", asyncSupported=true)
public class Bug63003 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println("Starting...");
        resp.getWriter().flush();
        resp.flushBuffer();

        // Put request in async mode and ignore it
        final AsyncContext ac = req.startAsync();
        ac.addListener(new Bug63003AsyncListener());
        ac.setTimeout(60000);

        ac.start(new Runnable() {

            @Override
            public void run() {
                // Written in such a way that interrupts don't reduce the pause
                long start = System.nanoTime();
                while ((System.nanoTime() - start) < 20L*1000*1000*1000) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                try {
                    resp.getWriter().println("...complete");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ac.complete();
            }
        });
    }


    private static class Bug63003AsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            System.out.println("onComplete");
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            System.out.println("onTimeout");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            System.out.println("onError");
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            System.out.println("onStartAsync");
        }
    }
}
