package spring.autowired;

import java.lang.annotation.*;

/**
 * 业务逻辑,注入接口
 * @author di kan
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
	String value() default "";
}
