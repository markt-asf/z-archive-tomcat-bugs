package org.apache.tomcat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

import javax.net.SocketFactory;

public class Bug57485Client {

    public static void main(String[] args) throws IOException {
        Socket s = SocketFactory.getDefault().createSocket("192.168.23.40", 80);
        s.setSoTimeout(30000);

        OutputStream os = s.getOutputStream();

        Writer w = new PrintWriter(os);
        w.write("POST /tomcat-bugs/Bug57485 HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Connection: keep-alive\r\n"
                + "Content-Type: application/x-www-form-urlencoded; charset=UTF-8\r\n"
                + "Transfer-Encoding: chunked\r\n"
                + "\r\n"
                + "5\r\n"
                + "k=125\r\n"
                + "0\r\n"
                + "\r\n");
        w.flush();
        s.close();
    }

}
