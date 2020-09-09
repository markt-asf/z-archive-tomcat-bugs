package org.apache.tomcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bug64710")
public class Bug64710 extends HttpServlet {

    private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = request.getReader()) {
            for (String line; (line = br.readLine()) != null;) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        try (Writer writer = response.getWriter()) {
            writer.write(sb.toString());
            writer.flush();
        }

    }


	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
	    // Generally poor practice but good enough for this test
	    doGet(request, response);
    }
}
