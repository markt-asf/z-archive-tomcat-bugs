package org.apache.tomcat;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.management.ObjectName;

import org.apache.tomcat.util.modeler.AttributeInfo;
import org.apache.tomcat.util.modeler.FeatureInfo;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.OperationInfo;
import org.apache.tomcat.util.modeler.ParameterInfo;
import org.apache.tomcat.util.net.AprEndpoint;

public class MBeanDump {

    public static void main(String... args) {
        dump(AprEndpoint.class, "Test");
    }


    public static void dump(Class<?> realClass, String type) {
        ManagedBean mbean = new ManagedBean();

        Method methods[] = null;

        Hashtable<String, Method> attMap = new Hashtable<>();
        Hashtable<String, Method> getAttMap = new Hashtable<>();
        Hashtable<String, Method> setAttMap = new Hashtable<>();
        Hashtable<String, Method> invokeAttMap = new Hashtable<>();

        methods = realClass.getMethods();

        initMethods(methods, attMap, getAttMap, setAttMap, invokeAttMap);

        try {

            Enumeration<String> en = attMap.keys();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                AttributeInfo ai = new AttributeInfo();
                ai.setName(name);
                Method gm = getAttMap.get(name);
                if (gm != null) {
                    ai.setGetMethod(gm.getName());
                    Class<?> t = gm.getReturnType();
                    if (t != null)
                        ai.setType(t.getName());
                }
                Method sm = setAttMap.get(name);
                if (sm != null) {
                    Class<?> t = sm.getParameterTypes()[0];
                    if (t != null)
                        ai.setType(t.getName());
                    ai.setSetMethod(sm.getName());
                }
                ai.setDescription("Introspected attribute " + name);
                if (gm == null)
                    ai.setReadable(false);
                if (sm == null)
                    ai.setWriteable(false);
                if (sm != null || gm != null)
                    mbean.addAttribute(ai);
            }

            for (Entry<String, Method> entry : invokeAttMap.entrySet()) {
                String name = entry.getKey();
                Method m = entry.getValue();
                if (m != null) {
                    OperationInfo op = new OperationInfo();
                    op.setName(name);
                    op.setReturnType(m.getReturnType().getName());
                    op.setDescription("Introspected operation " + name);
                    Class<?> parms[] = m.getParameterTypes();
                    for (int i = 0; i < parms.length; i++) {
                        ParameterInfo pi = new ParameterInfo();
                        pi.setType(parms[i].getName());
                        pi.setName("param" + i);
                        pi.setDescription("Introspected parameter param" + i);
                        op.addParameter(pi);
                    }
                    mbean.addOperation(op);
                }
            }

            mbean.setName(type);

            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = System.out;

            out.println("  <mbean         name=\"" + realClass.getSimpleName() + "\"");
            out.println("            className=\"org.apache.catalina.mbeans.ClassNameMBean\"");
            out.println("               domain=\"Catalina\"");
            out.println("                group=\"ThreadPool\"");
            out.println("                 type=\"" + realClass.getName() + "\">");
            out.println();

            AttributeInfo[] ais = mbean.getAttributes();
            Arrays.sort(ais, new AlphaOrder());

            for (AttributeInfo ai : ais) {
                out.println("    <attribute   name=\"" + ai.getName() + "\"");
                out.print("                 type=\"" + ai.getType() + "\"");
                if (!ai.isWriteable()) {
                    out.println();
                    out.print("            writeable=\"false\"");
                }
                if (ai.isIs() || (ai.getGetMethod() != null && ai.getGetMethod().startsWith("is"))) {
                    out.println();
                    out.print("                   is=\"true\"");
                }
                out.println("/>");
                out.println();
            }

            OperationInfo[] ois = mbean.getOperations();
            Arrays.sort(ois, new AlphaOrder());

            for (OperationInfo oi : ois) {
                out.println("    <operation       name=\"" + oi.getName() + "\"");
                out.print("               returnType=\"" + oi.getReturnType() + "\"");
                ParameterInfo[] pis = oi.getSignature();
                if (pis == null || pis.length == 0) {
                    out.println("/>");
                } else {
                    out.println(">");
                    for (ParameterInfo pi : pis) {
                        out.println("      <parameter name=\"" + pi.getName() + "\"");
                        out.println("                 type=\"" + pi.getType() + "\"/>");
                    }
                    out.println("    </operation>");
                }
                out.println();
            }

            out.println("  </mbean>");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void initMethods(Method methods[],
            Hashtable<String, Method> attMap,
            Hashtable<String, Method> getAttMap,
            Hashtable<String, Method> setAttMap,
            Hashtable<String, Method> invokeAttMap) {
        for (int j = 0; j < methods.length; ++j) {
            String name = methods[j].getName();

            if (Modifier.isStatic(methods[j].getModifiers()))
                continue;
            if (!Modifier.isPublic(methods[j].getModifiers())) {
                continue;
            }
            if (methods[j].getDeclaringClass() == Object.class)
                continue;
            Class<?> params[] = methods[j].getParameterTypes();

            if (name.startsWith("get") && params.length == 0) {
                Class<?> ret = methods[j].getReturnType();
                if (!supportedType(ret)) {
                    continue;
                }
                name = unCapitalize(name.substring(3));

                getAttMap.put(name, methods[j]);
// just a marker, we don't use the value
                attMap.put(name, methods[j]);
            } else if (name.startsWith("is") && params.length == 0) {
                Class<?> ret = methods[j].getReturnType();
                if (Boolean.TYPE != ret) {
                    continue;
                }
                name = unCapitalize(name.substring(2));

                getAttMap.put(name, methods[j]);
// just a marker, we don't use the value
                attMap.put(name, methods[j]);

            } else if (name.startsWith("set") && params.length == 1) {
                if (!supportedType(params[0])) {
                    continue;
                }
                name = unCapitalize(name.substring(3));
                setAttMap.put(name, methods[j]);
                attMap.put(name, methods[j]);
            } else {
                if (params.length == 0) {
                    if (specialMethods.get(methods[j].getName()) != null)
                        continue;
                    invokeAttMap.put(name, methods[j]);
                } else {
                    boolean supported = true;
                    for (int i = 0; i < params.length; i++) {
                        if (!supportedType(params[i])) {
                            supported = false;
                            break;
                        }
                    }
                    if (supported)
                        invokeAttMap.put(name, methods[j]);
                }
            }
        }
    }


    private static final Hashtable<String,String> specialMethods =
            new Hashtable<>();
    static {
        specialMethods.put( "preDeregister", "");
        specialMethods.put( "postDeregister", "");
    }

    private static final Class<?>[] supportedTypes  = new Class[] {
        Boolean.class,
        Boolean.TYPE,
        Byte.class,
        Byte.TYPE,
        Character.class,
        Character.TYPE,
        Short.class,
        Short.TYPE,
        Integer.class,
        Integer.TYPE,
        Long.class,
        Long.TYPE,
        Float.class,
        Float.TYPE,
        Double.class,
        Double.TYPE,
        String.class,
        String[].class,
        BigDecimal.class,
        BigInteger.class,
        ObjectName.class,
        Object[].class,
        java.io.File.class,
    };

    /**
     * Check if this class is one of the supported types.
     * If the class is supported, returns true.  Otherwise,
     * returns false.
     * @param ret The class to check
     * @return boolean True if class is supported
     */
    private static boolean supportedType(Class<?> ret) {
        for (int i = 0; i < supportedTypes.length; i++) {
            if (ret == supportedTypes[i]) {
                return true;
            }
        }
        if (isBeanCompatible(ret)) {
            return true;
        }
        return false;
    }

    /**
     * Check if this class conforms to JavaBeans specifications.
     * If the class is conformant, returns true.
     *
     * @param javaType The class to check
     * @return boolean True if the class is compatible.
     */
    private static boolean isBeanCompatible(Class<?> javaType) {
        // Must be a non-primitive and non array
        if (javaType.isArray() || javaType.isPrimitive()) {
            return false;
        }

        // Anything in the java or javax package that
        // does not have a defined mapping is excluded.
        if (javaType.getName().startsWith("java.") ||
            javaType.getName().startsWith("javax.")) {
            return false;
        }

        try {
            javaType.getConstructor(new Class[]{});
        } catch (java.lang.NoSuchMethodException e) {
            return false;
        }

        // Make sure superclass is compatible
        Class<?> superClass = javaType.getSuperclass();
        if (superClass != null &&
            superClass != java.lang.Object.class &&
            superClass != java.lang.Exception.class &&
            superClass != java.lang.Throwable.class) {
            if (!isBeanCompatible(superClass)) {
                return false;
            }
        }
        return true;
    }


    private static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }


    private static class AlphaOrder implements Comparator<FeatureInfo> {

        @Override
        public int compare(FeatureInfo o1, FeatureInfo o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
