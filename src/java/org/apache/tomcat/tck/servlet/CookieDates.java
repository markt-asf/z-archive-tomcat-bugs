package org.apache.tomcat.tck.servlet;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class CookieDates {

    private static final String HTTP_TESTDATE_FAILING = " 2019-07-08 13:21";
    private static final String COOKIE_EXPIRES_FAILING = "Mon, 08-Jul-19 13:21:42 GMT";

    private static final String COOKIE_EXPIRES_CORRECTED = "Mon, 08-Jul-2019 13:21:42 GMT";


    private static final String HTTP_CLIENT_3_DATE = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final SimpleDateFormat HTTP_CLIENT_3_SDF = new SimpleDateFormat(HTTP_CLIENT_3_DATE, Locale.US);

    private static final String CUSTOM_DATE = "yyyy-MM-dd HH:mm";
    private static final SimpleDateFormat CUSTOM_SDF = new SimpleDateFormat(CUSTOM_DATE, Locale.US);

    private static final String DEBUG_DATE ="yyyy-MM-dd HH:mm:ss zzz";
    private static final SimpleDateFormat DEBUG_SDF = new SimpleDateFormat(DEBUG_DATE, Locale.US);

    static {
        // Set timezones
        TimeZone GMT = TimeZone.getTimeZone("GMT");

        HTTP_CLIENT_3_SDF.setTimeZone(GMT);
        CUSTOM_SDF.setTimeZone(GMT);
        DEBUG_SDF.setTimeZone(GMT);
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Replicate reported TCK failure:");
        System.out.println("Client parses date in HTTP header as:           " + DEBUG_SDF.format(CUSTOM_SDF.parse(HTTP_TESTDATE_FAILING)));
        System.out.println("Client parses date in Cookie expires header as: " + DEBUG_SDF.format(HTTP_CLIENT_3_SDF.parse(COOKIE_EXPIRES_FAILING)));

        System.out.println("");

        System.out.println("Repeat but with 4 digit year in cookie expires header:");
        System.out.println("Client parses date in HTTP header as:           " + DEBUG_SDF.format(CUSTOM_SDF.parse(HTTP_TESTDATE_FAILING)));
        System.out.println("Client parses date in Cookie expires header as: " + DEBUG_SDF.format(HTTP_CLIENT_3_SDF.parse(COOKIE_EXPIRES_CORRECTED)));

    }

}
