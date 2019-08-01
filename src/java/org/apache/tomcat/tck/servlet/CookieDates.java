package org.apache.tomcat.tck.servlet;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class CookieDates {

    private static final String HTTP_CLIENT_3_DATE = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final SimpleDateFormat HTTP_CLIENT_3_SDF = new SimpleDateFormat(HTTP_CLIENT_3_DATE, Locale.US);

    private static final String HTTP_TESTDATE_FAILING = " 2019-07-08 13:21";
    private static final String COOKIE_EXPIRES_FAILING = "Mon, 08-Jul-19 13:21:42 GMT";

    private static final String CUSTOM_FORMAT_ORIGINAL = "yyyy-MM-dd HH:mm";

    public static void main(String[] args) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat(CUSTOM_FORMAT_ORIGINAL);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        System.out.println(sdf.format(sdf.parse(HTTP_TESTDATE_FAILING)));
        System.out.println(sdf.format(HTTP_CLIENT_3_SDF.parse(COOKIE_EXPIRES_FAILING)));
    }

}
