package org.apache.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class Bug63205 {

    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore p12 = KeyStore.getInstance("pkcs12");
        File file = new File("test.p12");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        java.nio.file.Files.copy(file.toPath(), os);
        os.flush();
        byte[] bytes = os.toByteArray();
        p12.load(new ByteArrayInputStream(bytes), "motive".toCharArray());
        int split = 10;
        p12.load(createSplitStream(bytes, split), "motive".toCharArray());

    }

    private static SequenceInputStream createSplitStream(byte[] bytes, int split) {
        return new SequenceInputStream(new ByteArrayInputStream(bytes, 0, split), new ByteArrayInputStream(bytes, split, bytes.length - split));
    }
}
