package org.apache.tomcat;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/bug62614" }, asyncSupported = true)
public class Bug62614 extends HttpServlet {

    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final AsyncContext asyncContext = request.startAsync();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/binary");

        final ServletOutputStream output = response.getOutputStream();
        output.setWriteListener(new WriteListener() {

            int i;
            byte[] bytes = new byte[0x10000];

            @Override
            public void onWritePossible() throws IOException {
                i++;
                System.out.println("onWritePossible called " + i + " times");

                if (i > 3) {
                    System.out.println("complete");
                    asyncContext.complete();
                    return;
                }

                int i = 0;
                while (output.isReady()) {
                    i++;
                    output.write(bytes);
                    if (i > 1000) {
                        asyncContext.complete();
                        return;
                    }
                }

                System.out.println("output.isReady() = " + false);
            }


            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
