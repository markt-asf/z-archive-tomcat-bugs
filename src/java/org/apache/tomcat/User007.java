package org.apache.tomcat;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/User007")
public class User007 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
    private static final String CONTENT_AS_STRING = "Hello world!";
    private static final byte[] CONTENT_AS_BYTES = CONTENT_AS_STRING.getBytes(StandardCharsets.UTF_8);
    private static final int CONTENT_LENGTH = CONTENT_AS_BYTES.length;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        final Writer out = response.getWriter();
        final String payload = request.getParameter("payload");
        try {
            if (payload != null) {
                response.setContentLength(payload.length());
                out.write(payload);
            } else {
                response.setContentLength(CONTENT_LENGTH);
                out.write(CONTENT_AS_STRING);
            }
            out.flush();
        } finally {
            out.close();
        }
    }
}
