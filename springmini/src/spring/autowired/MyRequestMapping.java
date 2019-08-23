package spring.autowired;

import java.lang.annotation.*;

/**
 * 请求url
 * @author Tom
 *
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
	String value() default "";
}
