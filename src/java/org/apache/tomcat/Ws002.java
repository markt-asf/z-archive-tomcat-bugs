package org.apache.tomcat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.websocket.OnMessage;

public class Ws002 {

    public static void main(String[] args) {
        ConcreteString cs = new ConcreteString();

        cs.onMessage("Hi");

        Class<?> clazz = cs.getClass();
        while (clazz != Object.class) {

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                System.out.println(method);
                System.out.println(method.isBridge());
                System.out.println(method.isSynthetic());
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    System.out.println("Declared: " + annotation);
                }
                annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    System.out.println(annotation);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    public static abstract class TestBase<T> {

        public void onMessage(T msg) {
            System.out.println("TestBase: " + msg);
        }
    }

    public static final class ConcreteString extends TestBase<String> {

        @Override
        @OnMessage
        public void onMessage(String msg) {
            System.out.println("ConcreteString: " + msg);
        }
    }
}
