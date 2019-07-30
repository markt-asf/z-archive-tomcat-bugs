package org.apache.tomcat.user005;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private static final int ITERATIONS = 100;

    public static void main(String[] args) {
        for (int i = 0; i < ITERATIONS; i++) {
            test();
        }
    }

    private static byte[] RequestBytes =
            ("GET /tomcat-bugs/user005 HTTP/1.1\n" +
                    "Host: 127.0.0.1:8080\n" +
                    "Connection: Upgrade\n" +
                    "Upgrade: websocket\n" +
                    "Origin: http://127.0.0.1:8080\n" +
                    "Sec-WebSocket-Version: 13\n" +
                    "Sec-WebSocket-Key: eHIAjgQrlE+qDd+grUBGpw==\n" +
                    "\n").getBytes();
    private static byte[] Mask = new byte[]{1, 2, 3, 4};

    private static void test() {
        try (Socket socket = new Socket("127.0.0.1", 8080);
             OutputStream out = socket.getOutputStream()) {
            out.write(RequestBytes);
            Thread.sleep(100);
            out.write(0x81); // text frame
            byte[] payload = "東京".getBytes("Shift-JIS");  // {-109, -116, -117, -98 }
            out.write(payload.length | 0x80); // masked
            out.write(Mask);
            for (int i = 0; i < payload.length; i++) {
                out.write(payload[i] ^ Mask[i % 4]);
            }
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}