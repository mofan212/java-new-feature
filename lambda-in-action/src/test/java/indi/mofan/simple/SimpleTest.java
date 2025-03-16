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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
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

    public void process(Consumer<String> consumer) {
        consumer.accept("Lambda In Action");
    }

    public void process(Function<String, String> function) {
        function.apply("Lambda In Action");
    }

    @Test
    public void testTypeInference() {
        // process(str -> System.out.println("Lambda In Action"));

        // 使用 x -> { foo(); } 与 void 兼容
        process(str -> {
            System.out.println("Lambda In Action");
        });

        // 使用 x -> ( foo() ) 与值兼容
        process(str -> (str.toLowerCase()));

        process((Consumer<String>) str -> System.out.println("Lambda In Action"));
    }

    private static <T> void consumerIntFunction(Consumer<int[]> consumer, IntFunction<T> generator) {
    }

    private static <T> void consumerIntFunction(Function<int[], ?> consumer, IntFunction<T> generator) {
    }

    /**
     * <a href="https://stackoverflow.com/questions/29323520/reference-to-method-is-ambiguous-when-using-lambdas-and-generics/">Reference to method is ambiguous when using lambdas and generics</a>
     */
    @Test
    public void testMethodReferenceTypeInference() {

        consumerIntFunction(data -> {
            // 与 void 兼容的块，可以通过表达式识别，无需解析实际类型
            Arrays.sort(data);
        }, int[]::new);

        /*
         * 如果是方法引用或者简化的 Lambda 表达式（没有大括号的）就需要进行类型推导：
         * 1. 是否是一个 void 函数，即 Consumer
         * 2. 是否是一个值返回函数，即 Function
         */
        /* consumerIntFunction(Arrays::sort, int[]::new);
         * consumerIntFunction(data -> Arrays.sort(data), int[]::new);
         */

        /*
         * 问题的关键：解析方法需要知道所需的方法签名，这应该通过目标类型确定，但是目标类型
         * 只有在泛型方法的类型参数已知是才能确定。
         * 从理论上来说，「方法重载解析」和「类型推断」可以同时进行，但实际上是先进行「方法重载解析」，
         * 再进行「类型推断」，因此「类型推断」得到的类型信息不能用于解决「方法重载解析」，最终导致
         * 编译报错。
         */

        /*
         * 根据以下规则，表达式可能与目标类型兼容：（see https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2.1）：
         * 1. 如果满足以下所有条件，那么一个 Lambda 表达式会和函数式接口类型兼容：
         *    - 目标类型函数类型的参数数量与 Lambda 表达式的参数数量相同
         *    - 如果目标类型函数类型返回 void，那么 Lambda 主体要么是一个表达式语句，要么是一个与 void 兼容块
         *    - 如果目标类型的函数类型有返回类型（非 void），那么 Lambda 主体要么是一个表达式，要么是一个值兼容块
         * 2. 如果满足以下条件之一，则方法引用表达式可能与函数式接口类型兼容：当该类型的函数类型参数数量为 n 时，存在
         * 至少一个参数数量为 n 的可能适用于该方法引用表达式的方法，并且以下条件之一成立：
         *   - 方法引用表达式的形式为 `引用类型::[参数类型]`，并且至少存在一个可能适用的方法满足以下条件之一：
         *     1) 方法是静态方法，并且参数数量是 n
         *     2) 方法是非静态方法，并且参数数量是 n - 1
         *   - 方法引用表达式的形式为其他形式时，至少存在一个可能适用的方法为非静态方法。
         */
    }

    private static void run(Consumer<Integer> consumer) {
        System.out.println("consumer");
    }

    private static void run(Function<Integer, Integer> function) {
        System.out.println("function");
    }

    /**
     * <a href="https://stackoverflow.com/questions/23430854/lambda-expression-and-method-overloading-doubts">Lambda expression and method overloading doubts</a>
     */
    @Test
    public void testLambdaExpAndMethodOverloadingDoubts() {
        run(i -> {
        });

        run(i -> 1);
    }

    private static void method1(Predicate<Integer> predicate) {
        System.out.println("Inside Predicate");
    }

    private static void method1(Function<Integer, String> function) {
        System.out.println("Inside Function");
    }

    public static void method2(Consumer<Integer> consumer) {
        System.out.println("Inside Consumer");
    }

    public static void method2(Predicate<Integer> predicate) {
        System.out.println("Inside Predicate");
    }

    /**
     * <a href="https://stackoverflow.com/questions/39294545/java-8-lambda-ambiguous-method-for-functional-interface-target-type">Java 8 lambda ambiguous method for functional interface - Target Type</a>
     */
    @Test
    public void testAmbiguousTargetType() {
        // method1((i) -> "Test");

        List<Integer> lst = new ArrayList<>();

        method2(i -> true);

        // method2(s -> lst.add(s));
    }
}
