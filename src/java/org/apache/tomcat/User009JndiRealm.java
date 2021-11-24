package org.apache.tomcat;

import java.security.Principal;

import javax.naming.NamingException;

import org.apache.catalina.realm.JNDIRealm;

public class User009JndiRealm extends JNDIRealm {

    @Override
    public Principal authenticate(JNDIConnection connection, String username, String credentials)
            throws NamingException {

        if (connection.context == null) {

        }
        return super.authenticate(connection, username, credentials);
    }
}
