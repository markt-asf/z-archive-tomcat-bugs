package org.apache.tomcat;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bug65368")
public class Bug65368 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        InputStream is = req.getInputStream();
        byte[] buf = new byte[8192];
        int read = 0;
        int thisTime = 0;
        while ((thisTime = is.read(buf)) != -1) {
            read += thisTime;
        }

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        resp.getOutputStream().print("Read [" + read + "] bytes from request body");
    }
}
