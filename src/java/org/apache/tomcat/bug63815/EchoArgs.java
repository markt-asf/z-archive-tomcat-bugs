package org.apache.tomcat.bug63815;

import java.lang.management.ManagementFactory;
import java.util.List;

public class EchoArgs {

    public static void main(String[] args) {
        List<String> javaArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        System.out.println("Args - 1 per line");
        for (String arg : javaArgs) {
            System.out.println("  Arg [" + arg + "]");
        }
        System.out.println("Args done");
    }
}
