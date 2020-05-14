package org.apache.tomcat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ObjectStreams {

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ObjectOutputStream oos1 = new ObjectOutputStream(baos1);
        oos1.writeLong(1000);
        oos1.close();
        dump(baos1);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

        ObjectOutputStream oos2 = new ObjectOutputStream(baos2);

        oos2.writeObject(Long.valueOf(1000));
        oos2.close();
        dump(baos2);

    }

    private static void dump(ByteArrayOutputStream baos) {
        System.out.println("===");
        byte[] bytes = baos.toByteArray();
        for (byte b : bytes) {
            System.out.println(b);
        }
    }
}
