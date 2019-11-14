package org.apache.tomcat;

import java.util.Map;

public class PropertyDump {

    public static void main(String[] args) {
        for (Map.Entry<Object,Object> entry : System.getProperties().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}
