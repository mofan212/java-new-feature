package indi.mofan.apply;


import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

/**
 * @author mofan
 * @date 2025/3/10 21:55
 */
public class ApplyTest implements WithAssertions {

    static class Factorial {
        static int factorial(int n) {
            if (n == 0) return 1;
            return n * factorial(n - 1);
        }
    }

    @SuppressWarnings("unchecked")
    static class RecursionWithClosure {
        static Function<Integer, Integer>[] recursiveFunction = new Function[10];

        static {
            recursiveFunction[0] = x -> {
                if (x == 0) return 1;
                return x * recursiveFunction[0].apply(x - 1);
            };
        }

        static int factorial(int n) {
            return recursiveFunction[0].apply(n);
        }
    }

    static class RecursionWithHigherOrderFunction {
        @FunctionalInterface
        interface SelfApplicable<SELF, T> {
            T apply(SelfApplicable<SELF, T> self, T t);
        }

        static final SelfApplicable<Integer, Integer> FACTORIAL = (self, n) ->
                n == 0 ? 1 : n * self.apply(self, n - 1);
    }

    @Test
    public void testRecursion() {
        int value = Factorial.factorial(5);
        assertThat(value).isEqualTo(120);

        value = RecursionWithClosure.factorial(5);
        assertThat(value).isEqualTo(120);

        value = RecursionWithHigherOrderFunction.FACTORIAL.apply(RecursionWithHigherOrderFunction.FACTORIAL, 5);
        assertThat(value).isEqualTo(120);
    }
}
