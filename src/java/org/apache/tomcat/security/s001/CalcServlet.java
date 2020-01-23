package org.apache.tomcat.security.s001;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet(urlPatterns = "/calcServlet", asyncSupported = true)
public class CalcServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> OPERATIONS = Arrays.asList("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE");

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    private void process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

            final String operation = req.getParameter("op");
            final String asyncParam = req.getParameter("async");
            final String delayParam = req.getParameter("delay");
            final String timeoutParam = req.getParameter("timeout");
            final String xParam = req.getParameter("x");
            final String yParam = req.getParameter("y");

            if (operation == null || (!OPERATIONS.contains(operation.toUpperCase()))) {
                displayUsage(resp);
                return;
            }

            final int x;
            try {
                x = Integer.parseInt(xParam);
            } catch (final Exception e) {
                displayUsage(resp);
                return;
            }

            final int y;
            try {
                y = Integer.parseInt(yParam);
            } catch (final Exception e) {
                displayUsage(resp);
                return;
            }

            int delay = 0;
            try {
                delay = Integer.parseInt(delayParam);
            } catch (final Exception e) {
                // ignore
            }

            int timeout = -1;
            try {
                timeout = Integer.parseInt(timeoutParam);
            } catch (final Exception e) {
                // ignore
            }

            boolean async = false;
            try {
                async = Boolean.parseBoolean(asyncParam);
            } catch (final Exception e) {
                // ignore
            }

            if (!async) {
                process(operation, x, y, resp);
                return;
            }

            final int threadDelay = delay;
            final AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(timeout);
            asyncContext.start(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(threadDelay);
                    } catch (final InterruptedException e) {
                        // ignore
                    }

                    try {
                        process(operation, x, y, resp);
                    } catch (final Exception e) {

                    } finally {
                        asyncContext.complete();
                    }
                }
            });
    }

    private void process(final String operation, final int x, final int y, final HttpServletResponse resp) throws IOException {
        if ("ADD".equals(operation.toUpperCase())) {
            resp.getWriter().print(x + y);
        } else if ("SUBTRACT".equals(operation.toUpperCase())) {
            resp.getWriter().print(x - y);
        } else if ("MULTIPLY".equals(operation.toUpperCase())) {
            resp.getWriter().print(x * y);
        } else if ("DIVIDE".equals(operation.toUpperCase())) {
            resp.getWriter().print(x / y);
        }
    }

    void displayUsage(final HttpServletResponse resp) throws IOException {
        resp.getWriter().println("Parameters:");
        resp.getWriter().println("\tx: 1st Operand");
        resp.getWriter().println("\ty: 2nd Operand");
        resp.getWriter().println("\top: Operator - add | subtract | multiply | divide");
        resp.getWriter().println("\tasync: Whether to run asynchronously - true | false");
        resp.getWriter().println("\tdelay: Delay for async calls (in ms)");
        resp.getWriter().println("\ttimeout: Timeout for async calls (in ms)\n\n");
        resp.getWriter().println("Example:");
        resp.getWriter().println("\tSync: http://localhost:8080/async-servlet?x=2&y=4&op=multiply");
        resp.getWriter().println("\tAsync (1 second delay): http://localhost:8080/async-servlet?x=2&y=4&op=multiply&async=true&delay=1000");
        resp.getWriter().println("\tAsync Timeout (10 second delay, 1 second timeout): http://localhost:8080/async-servlet?x=2&y=4&op=multiply&async=true&delay=10000&timeout=1000");
    }

//end::main[]
}