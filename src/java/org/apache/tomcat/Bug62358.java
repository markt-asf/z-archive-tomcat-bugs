package org.apache.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Bug62358 {

    public static void main(String[] args) throws IOException {
        // How does JRE behave if an InputStream is requested for a directory?
        String tmpDirName = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirName);
        URL tmpUrl = tmpDir.toURI().toURL();
        InputStream tmpIs = tmpUrl.openStream();
        byte[] data = new byte[8192];
        int read = -1;
        while ((read = tmpIs.read(data)) > 0) {
            System.out.println(read + " bytes read");
            System.out.println(new String(data, 0, read));
        }
    }
}
