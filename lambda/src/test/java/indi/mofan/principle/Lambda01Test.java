package indi.mofan.principle;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.function.BinaryOperator;

/**
 * @author mofan
 * @date 2024/7/20 14:15
 */
public class Lambda01Test implements WithAssertions {
    @Test
    public void test() {
        BinaryOperator<Integer> lambda = (a, b) -> a + b;
        Method[] methods = Lambda01Test.class.getDeclaredMethods();
        // 方法不止一个
        assertThat(methods).hasSizeGreaterThan(1);
        for (Method method : methods) {
            String name = method.getName();
            // 其中有一个方法的方法名中包含 lambda$testTrySplit$
            if (!name.contains("lambda$testTrySplit$") && !name.contains("testTrySplit")) {
                Assertions.fail();
            }
        }
    }
}
