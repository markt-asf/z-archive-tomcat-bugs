package org.apache.tomcat;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class TLSInfo {

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        String[] protocols = sslContext.getSupportedSSLParameters().getProtocols();
        for (String protocol : protocols) {
            System.out.println(protocol);
        }
        System.out.println("===");
        protocols = sslContext.getDefaultSSLParameters().getProtocols();
        for (String protocol : protocols) {
            System.out.println(protocol);
        }
    }
}
