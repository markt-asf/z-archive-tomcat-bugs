package org.apache.tomcat;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@WebServlet("/*")
//@MultipartConfig
public class Bug62664 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private void showAllParams(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                resp.getWriter().println(key + "=" + value);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        showAllParams(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/parts-first".equals(req.getPathInfo())) {
            resp.getWriter().println("before getParts:");
            showAllParams(req, resp);
            resp.getWriter().println();
        }
        try {
            req.getParts();
        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
        resp.getWriter().println("after getParts:");
        showAllParams(req, resp);
    }
}
