package org.apache.tomcat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "Bug65684", urlPatterns = "/bug65684")
public class Bug65684 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String s;
        try {
            s = CompletableFuture.supplyAsync(() -> {
                LogManager lm = LogManager.getLogManager();
                return lm.getProperty("anything");
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter printWriter = new PrintWriter(response.getOutputStream());
            e.printStackTrace(printWriter);
            printWriter.close();
            return;
        }

        response.getOutputStream().println("Success: " + s);
    }
}