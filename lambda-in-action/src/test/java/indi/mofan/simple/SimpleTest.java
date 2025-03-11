package indi.mofan.simple;

import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author mofan
 * @date 2025/3/2 13:35
 */
public class SimpleTest implements WithAssertions {

    // -------------------- 延迟执行 --------------------

    public void log(int level, String message) {
        if (level == 1) {
            System.out.println(message);
        }
    }

    public void log(int level, Supplier<String> log) {
        if (level == 1) {
            System.out.println(log.get());
        }
    }

    @Test
    public void testLog() {
        // 模拟多种日志信息
        String a = "A";
        String b = "B";
        String c = "C";

        log(2, a + b + c);
        log(1, a + b + c);

        log(2, () -> c + b + a);
        log(1, () -> c + b + a);
    }

    // -------------------- 高阶函数 --------------------

    public int add(int a, int b, IntUnaryOperator f) {
        return f.applyAsInt(a) + f.applyAsInt(b);
    }

    @Test
    public void testHigherOrderFunction() {
        int x = add(-5, 6, Math::abs);
        assertThat(x).isEqualTo(11);
    }

    // -------------------- 复合 Lambda 表达式 --------------------

    enum Color {
        RED, GREEN,
    }

    record Apple(Color color, int weight) {
    }

    @Test
    public void testPredicate() {
        Predicate<Apple> redApple = apple -> Color.RED.equals(apple.color);

        // 不是红色的苹果
        Predicate<Apple> notRedApple = redApple.negate();

        // 红苹果且重量大于 150g
        Predicate<Apple> redAndHeavyApple = redApple.and(apple -> apple.weight > 150);

        // 要么是 150g 以上的红苹果要么是绿苹果
        Predicate<Apple> redAndHeavyAppleOrGreen = redApple.or(apple -> apple.weight > 150)
                .or(apple -> Color.GREEN.equals(apple.color));
    }

    @Test
    public void testFunction() {
        Function<Integer, Integer> f = x -> x + 1;
        Function<Integer, Integer> g = x -> x * 2;

        // g(f(x))
        Function<Integer, Integer> x = f.andThen(g);
        assertThat(x.apply(1)).isEqualTo(4);

        // f(g(x))
        Function<Integer, Integer> y = f.compose(g);
        assertThat(y.apply(1)).isEqualTo(3);
    }

    // -------------------- 包装受检异常 --------------------

    public String processFile() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            // 可能有更多的行为，比如一次读两行？
            return br.readLine();
        }
    }

    @CanIgnoreReturnValue
    public String processFileWithLambda1(Function<BufferedReader, String> function) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            return function.apply(br);
        }
    }

    @FunctionalInterface
    public interface BufferedReaderProcessor {
        String process(BufferedReader br) throws IOException;
    }

    @CanIgnoreReturnValue
    public String processFileWithLambda2(BufferedReaderProcessor processor) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            return processor.process(br);
        }
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public static <T, R> Function<T, R> wrap(ThrowingFunction<T, R, Exception> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Test
    @Disabled
    @SneakyThrows
    public void testProcessFile() {
        processFileWithLambda1(br -> {
            try {
                // 内置函数式接口不允许抛出受检异常
                return br.readLine() + br.readLine();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 自定义函数式接口并声明受检异常
        processFileWithLambda2(br -> br.readLine() + br.readLine());

        // 包装受检异常
        processFileWithLambda1(wrap(br -> br.readLine() + br.readLine()));
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static <T> Supplier<T> wrapSupplier(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }


    public static <T> Supplier<T> sneakySupplier(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return sneakyThrow(e);
            }
        };
    }

    @Test
    public void testSneakyThrow() {
        try {
            String str = wrapSupplier(() -> Files.readString(Path.of("data.txt"))).get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(RuntimeException.class, e);
        }

        try {
            String str = sneakySupplier(() -> Files.readString(Path.of("data.txt"))).get();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(IOException.class, e);
        }
    }

    // -------------------- 柯里化 --------------------
    // 见 indi.mofan.CurryingTest.testTernaryConsumer


    // -------------------- 类型推断 --------------------

    public void run(Runnable runnable) {
        runnable.run();
    }

    public void process(Consumer<String> consumer) {
        consumer.accept("Lambda In Action");
    }

    public void process(Function<String, String> function) {
        function.apply("Lambda In Action");
    }

    @Test
    public void testTypeInference() {
        run(() -> {
            System.out.println("Lambda In Action");
        });

        // 方法调用的返回值为 void 时，可以不使用括号环绕返回值为空的单行方法调用
        run(() -> System.out.println("Lambda In Action"));

        // process(str -> System.out.println("Lambda In Action"));

        process((Consumer<String>) str -> System.out.println("Lambda In Action"));
    }

}
