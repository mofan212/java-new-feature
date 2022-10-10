package indi.mofan.lambda;

/**
 * Te means ternary
 *
 * @author mofan
 * @date 2022/10/10 22:15
 */
@FunctionalInterface
public interface TeConsumer<T, U, R> {

    void accept(T t, U u, R r);
}
