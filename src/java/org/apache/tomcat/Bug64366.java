package org.apache.tomcat;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Bug64366
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/bug64366" })
public class Bug64366 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);
        new Thread(new NotStoppedService(asyncContext)).start();
    }

    private static class NotStoppedService implements Runnable {
        private AsyncContext asyncContext;

        public NotStoppedService(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        private void writeResponseHeaders() {
            HttpServletResponse response = (HttpServletResponse)asyncContext.getResponse();
            response.setStatus(200);
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control","no-cache");
        }

        @Override
        public void run() {
            writeResponseHeaders();
            while (true) {
                // do some keep-alive
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException ie) {}
                try {
                    // write an SSE comment
                    asyncContext.getResponse().getOutputStream().write(":\n\n".getBytes());
                    asyncContext.getResponse().getOutputStream().flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
