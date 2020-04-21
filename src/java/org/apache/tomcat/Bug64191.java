package org.apache.tomcat;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Bug64191 {

    private static final String testResource = "org/apache/tomcat/bug-64191.properties";

    public static void main(String... args) throws Exception {

        URL url = Bug64191.class.getClassLoader().getResource(testResource);
        if (url == null) {
            System.out.println("FAIL");
            return;
        }
        InputStream is = url.openConnection().getInputStream();
        Properties p = new Properties();
        p.load(is);

        System.out.println(p.get("test"));
    }
}
