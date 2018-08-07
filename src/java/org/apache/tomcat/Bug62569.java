package org.apache.tomcat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/bug62569"}, asyncSupported=true)
public class Bug62569 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
                       throws IOException {

       final AsyncContext acontext = request.startAsync();
       final ServletInputStream input = request.getInputStream();

       input.setReadListener(new ReadListener() {
          byte buffer[] = new byte[4*1024];
          StringBuilder sbuilder = new StringBuilder();
          @Override
          public void onDataAvailable() {
             try {
                do {
                   int length = input.read(buffer);
                   if (length > 0) {
                       sbuilder.append(new String(buffer, 0, length));
                   }
                } while(input.isReady());
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
          }
          @Override
          public void onAllDataRead() {
             try {
                PrintWriter pw = acontext.getResponse().getWriter();
                pw.write("...the response...");
                pw.flush();
                acontext.getResponse().flushBuffer();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
             acontext.complete();
          }
          @Override
          public void onError(Throwable t) {
              t.printStackTrace();
          }
       });
    }
}
