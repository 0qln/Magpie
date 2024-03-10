package Misc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public abstract class Builder<T> {

    // TODO: singleton mechanism, such that this can only be set on program startup
    public static boolean unsafe = false;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface NotRequired {
    }

    // The `BuilderRequired` annotation should only be used on non-primitve
    // data types.
    // Primitve types get assigned a default value anyways, thus making the
    // `BuilderRequired` annotation useless.
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    protected static @interface Required {
    }

    public static class FieldNotSetException extends RuntimeException {
    }

    // Check if required fields are set
    protected <TChild extends Builder<?>> boolean ok(TChild childInstance) {
        try {
            for (Field field : childInstance.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Required.class)
                        && field.get(childInstance) == null)
                    return false;
            }
            return true;
        } catch (IllegalAccessException e) {
            // This shouldn't happen, as we have forced the field to be accesible.
            System.out.println("Parent builder failed to access field of child.");
            return false;
        }
    }

    public final T build() throws FieldNotSetException {
        return build(!unsafe);
    }

    /**
     * @param checkFields Check if required fields are set. (Reccomendation: [ Release build: Off | Development build: On ])
     * @return The instance that the builder prodeced
     */
    public final T build(boolean checkFields) throws FieldNotSetException {
        if (!checkFields)
            return _buildT();

        if (!ok(this)) 
            throw new FieldNotSetException();

        return _buildT();
    }

    protected abstract T _buildT();
}
