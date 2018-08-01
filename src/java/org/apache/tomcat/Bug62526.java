package org.apache.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;


/*
 * Bug requires a key store with a null/empty password. This code tries to
 * create such a key store from the standard test key store.
 */
public class Bug62526 {

    //private static final String KEYSTORE_PATH =
    //        "/home/mark/repos/asf-public/tomcat/trunk/output/build/conf/localhost-rsa.p12";

    private static final String KEYSTORE_PATH =
            "/home/mark/Downloads/kse-532/keystore.p12";

    public static void main(String... args) throws Exception {
        File f = new File(KEYSTORE_PATH);

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        InputStream is = new FileInputStream(f);
        //keystore.load(is, null);
        keystore.load(is, "".toCharArray());
        is.close();

        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println(alias);
            Certificate cert = keystore.getCertificate(alias);
            System.out.println(cert);
            Key key = keystore.getKey(alias, "keypass".toCharArray());
            System.out.println(key);
        }
        /*
        String alias = "tomcat";

        System.out.println(alias);
        Certificate cert = keystore.getCertificate(alias);
        System.out.println(cert);
        Key key = keystore.getKey(alias, "changeit".toCharArray());
        System.out.println(key);
         */

        /*
        f.delete();
        //keystore.deleteEntry(alias);
        //keystore.setKeyEntry("tomcat", key, "changeit".toCharArray(), null);

        FileOutputStream os = new FileOutputStream(f);
        keystore.store(os, "".toCharArray());
        */
    }
}
