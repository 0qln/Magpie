package Engine;

import java.lang.annotation.*;

// The `BuilderRequired` annotation should only be used on non-primitve
// data types.
// Primitve types get assigned a default value anyways, thus making the 
// `BuilderRequired` annotation useless.
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface BuilderRequired { }
