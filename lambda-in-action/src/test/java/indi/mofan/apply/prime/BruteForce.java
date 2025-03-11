package indi.mofan.apply.prime;


import java.util.stream.IntStream;

/**
 * @author mofan
 * @date 2025/3/11 16:48
 */
public class BruteForce {
    /**
     * 返回从 2 开始的整数流
     */
    public static IntStream numbers() {
        return IntStream.iterate(2, i -> i + 1);
    }

    /**
     * 获取整数流的第一个整数
     */
    static int head(IntStream numbers) {
        return numbers.findFirst().getAsInt();
    }

    /**
     * 跳过当前整数流的第一个元素
     */
    static IntStream tail(IntStream numbers) {
        return numbers.skip(1);
    }

    public static IntStream primesErr(IntStream numbers) {
        int head = head(numbers);
        return IntStream.concat(
                IntStream.of(head),
                primesErr(tail(numbers).filter(n -> n % head != 0))
        );
    }
}
