package org.apache.tomcat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.el.ExpressionFactoryImpl;
import org.apache.el.lang.FunctionMapperImpl;
import org.apache.el.lang.VariableMapperImpl;

public class User003ELPerformance {

    public static void main(final String[] args) {

        final ExpressionFactory expressionFactory = new ExpressionFactoryImpl();

        final CompositeELResolver elResolver = new CompositeELResolver();
        //elResolver.add(new CoerceToResolver());
        elResolver.add(new ArrayELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new BeanELResolver());
        elResolver.add(new MapELResolver());


        final VariableMapper variableMapper = new VariableMapperImpl();
        variableMapper.setVariable("countries", expressionFactory.createValueExpression(new HashSet<>(Arrays.asList("AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH", "II", "JJ", "KK", "LL", "MM")), Set.class));
        variableMapper.setVariable("NN", expressionFactory.createValueExpression("NN", String.class));
        variableMapper.setVariable("ZZ", expressionFactory.createValueExpression("ZZ", String.class));
        variableMapper.setVariable("I1", expressionFactory.createValueExpression(239235, Long.class));
        variableMapper.setVariable("I2", expressionFactory.createValueExpression(569071142, Long.class));
        variableMapper.setVariable("I3", expressionFactory.createValueExpression(-189245, Long.class));
        variableMapper.setVariable("D1", expressionFactory.createValueExpression(129835.12512, Double.class));
        variableMapper.setVariable("D3", expressionFactory.createValueExpression(98982223.598731412, Double.class));
        variableMapper.setVariable("BT", expressionFactory.createValueExpression(Boolean.TRUE, Boolean.class));
        variableMapper.setVariable("BF", expressionFactory.createValueExpression(Boolean.FALSE, Boolean.class));

        final String[] expressions = {
                "${I2 - I3 + D3 - D1}",
                "${NN == '0' && ZZ == 'ZZ'}",
                "${BT != BF}",
        };



        final ELContext elContext = new ELContext() {
            @Override
            public ELResolver getELResolver() {
                return elResolver;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return new FunctionMapperImpl();
            }

            @Override
            public VariableMapper getVariableMapper() {
                return variableMapper;
            }


            /*
            @Override
            public Object convertToType(final Object obj, final Class<?> type) {
                // Uncomment this using apache-el:8+ to reduce slowdown
//                if (type == null
//                        || Object.class.equals(type)
//                        || (obj != null && type.isAssignableFrom(obj.getClass()))
//                        || obj == null
//                ) {
//                    return obj;
//                    // alternative:
//                    // return ELSupport.coerceToType(null, obj, type);
//                }

                return super.convertToType(obj, type);
            }
            */
        };

        final ValueExpression[] ves = new ValueExpression[expressions.length];
        for (int j = 0; j < expressions.length; j++) {
            ves[j] = expressionFactory.createValueExpression(elContext, expressions[j], Object.class);
        }

        final int iterations = 100*1000;
        long elapsed = -System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < expressions.length; j++) {
                final Object result = ves[j].getValue(elContext);
                // alternative benchmarks:
                // Object result = elContext.convertToType("Foo", String.class);
                if (i % 10000 == 0) {
                    System.out.println(result);
                }
            }
        }

        elapsed += System.currentTimeMillis();

        final int total = iterations * expressions.length;
        System.out.println(total + " expressions in " + elapsed + " ms");
    }
/*
    private static class CoerceToResolver extends ELResolver {

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            return null;
        }

        @Override
        public Class<?> getType(ELContext context, Object base, Object property) {
            return null;
        }

        @Override
        public void setValue(ELContext context, Object base, Object property, Object value) {
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base, Object property) {
            return false;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            return null;
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext context, Object base) {
            return null;
        }

        @Override
        public Object convertToType(ELContext context, Object obj, Class<?> type) {
            return ELSupport.coerceToType(null, obj, type);
        }
    }
*/
}
