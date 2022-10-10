package indi.mofan;

import indi.mofan.lambda.TeConsumer;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author mofan
 * @date 2022/10/10 22:17
 */
public class TeConsumerTest {
    @Test
    public void testTeConsumer() {
        TeConsumer<Double, Float, Integer> consumer = (t, u, r) -> {
            System.out.println(t + ";" + u + ";" + r);
        };
        // 1.0;2.0;3
        consumer.accept(1.0, 2f, 3);

        Function<Double, Function<Float, Consumer<Integer>>> f = t -> u -> r -> {
            System.out.println(t + ";" + u + ";" + r);
        };
        // 1.0;2.0;3
        f.apply(1.0).apply(2f).accept(3);

        Function<Double, BiConsumer<Float, Integer>> fun = t -> (u, r) -> {
            System.out.println(t + ";" + u + ";" + r);
        };
        // 1.0;2.0;3
        fun.apply(1.0).accept(2f, 3);
    }
}
