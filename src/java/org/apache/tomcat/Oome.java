package org.apache.tomcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/oome")
public class Oome extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<Integer,byte[]> map = new HashMap<>();

        for (int i = 0; i < 1024; i++) {
            map.put(Integer.valueOf(i), new byte[1024*1024]);
        }
    }
}
