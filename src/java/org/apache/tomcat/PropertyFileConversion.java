package org.apache.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Used to assist in conversion of property files from \\unnnn encoding to
 * UTF-8.
 */
public class PropertyFileConversion {

    public static void main(String[] args) throws IOException {
        // Read a property file and dump it to the console.
        File f = new File("/home/mark/repos/asf-public/tomcat/trunk/java/org/apache/catalina/manager/LocalStrings_ru.properties");
        FileInputStream fis = new FileInputStream(f);

        Properties props = new Properties();
        props.load(fis);

        for (Map.Entry<?,?> entry : props.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }
}
