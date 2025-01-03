package dgs.inputproxy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class SparseUpdateProxy {
    private final static Logger LOGGER = LoggerFactory.getLogger(SparseUpdateProxy.class);

    public static boolean isSet(String fieldName, @This Object impl) {
        try {
            Field fieldsSet = impl.getClass().getDeclaredField("fieldsSet");
            fieldsSet.setAccessible(true);
            @SuppressWarnings("unchecked") Set<String> fields = (Set<String>) fieldsSet.get(impl);
            return fields.contains(fieldName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BindingPriority(100)
    public static void intercept(@Advice.Argument(0) Object value, @SuperCall Callable<?> call, @Origin Method m, @This Object impl) {
        LOGGER.info("Invoked {} method with: {}", m.getName(), value);
        try {
            var fieldsSet = impl.getClass().getDeclaredField("fieldsSet");
            fieldsSet.setAccessible(true);
            try {
                @SuppressWarnings("unchecked") Set<String> fields = (Set<String>) fieldsSet.get(impl);
                if (fields == null) {
                    fields = new HashSet<>();
                    fields.add(getFieldNameFromSetter(m));
                    fieldsSet.set(impl, fields);
                } else {
                    fields.add(getFieldNameFromSetter(m));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info(String.valueOf(fieldsSet));

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }


        try {
            call.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String getFieldNameFromSetter(Method m) {
        var removeSet = m.getName().substring(3);
        var firstLetter = removeSet.substring(0, 1);
        return firstLetter.toLowerCase() + removeSet.substring(1);
    }
}
