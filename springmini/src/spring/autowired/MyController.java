package spring.autowired;

import java.lang.annotation.*;

/**
 * 页面交互
 * @author Di kan
 *
 */
//定义注解的作用目标，作用目标为类和接口还有枚举
@Target({ElementType.TYPE})
// 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
//说明该注解将被包含在javadoc中
@Documented
public @interface MyController {
	String value() default "";
}
