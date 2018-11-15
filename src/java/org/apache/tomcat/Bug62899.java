package org.apache.tomcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;

public class Bug62899 {

    public static class AsyncRead extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            final AsyncContext asyncContext = request.startAsync();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/binary");

            final ServletInputStream input = request.getInputStream();

            input.setReadListener(
                    new ReadListener() {
                        byte[] bytes = new byte[4096];
                        int bytesRead;

                        @Override
                        public void onDataAvailable() throws IOException {
                            while (input.isReady()) {
                                int len = input.read(bytes);
                                //System.err.println("Servlet read [" + len + "] bytes");
                                if (len != -1) {
                                    bytesRead += len;
                                    //if (bytesRead >= 262140) {
                                        //System.err.println("bytesRead = " + bytesRead); // 262140
                                        //System.err.println(
                                        //        "bytes: " + Arrays.copyOf(bytes, len).hashCode());
                                    //}
                                }
                            }
                        }

                        @Override
                        public void onAllDataRead() throws IOException {
                            System.out.println("bytes read: " + bytesRead);
                            asyncContext.complete();
                        }

                        @Override
                        public void onError(Throwable t) {
                            t.printStackTrace();
                        }
                    });
        }
    }


    public static void main(String[] args) throws Exception {

        System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        System.setProperty("java.util.logging.config.file", new File("conf/logging.properties").toString());

        for (int i = 0; i < 1000; i++) {
            doTest();
        }
    }


    private static void doTest() throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(0);
        Context ctx = tomcat.addContext("/asyncread", new File("build/tmp").getAbsolutePath());
        Tomcat.addServlet(ctx, "asyncread", new AsyncRead()).setAsyncSupported(true);
        ctx.addServletMappingDecoded("/*", "asyncread");
        tomcat.getConnector().addUpgradeProtocol(new Http2Protocol());

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            tomcat.stop();
            throw e;
        }
        int port = tomcat.getConnector().getLocalPort();

        byte[] bytes = new byte[271828];
        new Random().nextBytes(bytes);
        try (FileOutputStream file = new FileOutputStream("newfile")) {
            file.write(bytes);
        }

        long startTime = System.nanoTime();
        Process proc = Runtime.getRuntime()
                .exec(
                        "nghttp -v -t 10 --header=:method:POST --header=Content-Type:application/binary -d newfile"
                                + " http://localhost:"
                                + port
                                + "/asyncread");

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        while ((s = stdErr.readLine()) != null) {
            System.out.println(s);
        }

        tomcat.stop();
        if (System.nanoTime() - startTime > TimeUnit.SECONDS.toNanos(9)) {
            throw new RuntimeException("time out");
        }
    }
}