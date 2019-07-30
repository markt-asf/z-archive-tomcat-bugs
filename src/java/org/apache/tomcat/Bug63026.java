package org.apache.tomcat;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class Bug63026 {

    public static void main(String[] args) throws InvalidNameException {
        LdapName name = new LdapName("CN=bug63026\\ \\ ,CN=Users,DC=DEV,DC=LOCAL");
        System.out.println(name.toString());
        System.out.println(name.getRdns());
        LdapName name2 = new LdapName("CN=bug63026 \\ ,CN=Users,DC=DEV,DC=LOCAL");
        System.out.println(name2.equals(name));
        System.out.println(name2.getRdns());

        for (Rdn rdn : name.getRdns()) {
            System.out.println(rdn.getValue() + "|" + Rdn.unescapeValue(rdn.getValue().toString()) + "|" + Rdn.escapeValue(Rdn.unescapeValue(rdn.getValue().toString())));
        }


        System.out.println(Rdn.escapeValue("foo bar  "));

        Rdn.unescapeValue("\\20");
    }
}
