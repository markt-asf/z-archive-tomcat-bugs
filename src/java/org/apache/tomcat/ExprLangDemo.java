package org.apache.tomcat;

import jakarta.el.ELProcessor;

public class ExprLangDemo {

    public static void main(String[] args) {
        ELProcessor elp = new ELProcessor();

        String key = "myKey";
        String value = "myValue";

        elp.setVariable("aaa", "'" + key + "'");
        elp.setVariable("bbb", "'" + value + "'");

        // Map<?,?> result = elp.getValue("{a:b}", Map.class);

        Object result = elp.eval("{ aaa : bbb }.get(aaa)");

        System.out.println(result.getClass());
        System.out.println(result);
    }
}
