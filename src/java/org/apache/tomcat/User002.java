package org.apache.tomcat;

import java.util.regex.Pattern;

public class User002 {

    public static void main(String[] args) {
        System.out.println(Pattern.compile("127\\.0\\.0\\.1|127\\.0\\.0\\.2").matcher("127.0.0.3").matches());
    }

}
