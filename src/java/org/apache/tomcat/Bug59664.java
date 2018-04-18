/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Bug59664 {

	private static final String TEST_DATA_STRING = "ЯбЭд";
	private static final byte[] TEST_DATA_BYTES;

	static {
		byte[] result = null;
		try {
			result = TEST_DATA_STRING.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Impossible
		}
		TEST_DATA_BYTES = result;
	}

	public static void main(String[] args) throws Exception {
		// Setup SSL
        File keystoreFile = new File("src/resources/ca.jks");
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream fis = new FileInputStream(keystoreFile);
        ks.load(fis, "changeit".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, tmf.getTrustManagers(), null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// The request
        URL url = new URL("https://localhost:8443/tomcat-bugs/bug59664.jsp");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.connect();

        OutputStream os = connection.getOutputStream();
        for (int i = 0; i < TEST_DATA_BYTES.length; i++) {
        	os.write(TEST_DATA_BYTES, i,  1);
        	Thread.sleep(500);
        	os.flush();
        }
        os.close();

        int rc = connection.getResponseCode();
        System.out.println("Return code was [" + rc + "]");

        InputStream is = connection.getInputStream();
        int read = 0;
        int lastRead = 0;
        byte[] buf = new byte[1024];
        while (lastRead > -1) {
        	read += lastRead;
        	lastRead = is.read(buf, read, 1024 - read);
        }
        System.out.println("Response body:");
        System.out.println(new String(buf, 0, read, "UTF-8"));
    }
}
