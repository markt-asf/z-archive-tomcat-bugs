package org.apache.tomcat;

import java.util.Locale;

public enum TestEnum {
    VALUE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH).toString();
    }
}
