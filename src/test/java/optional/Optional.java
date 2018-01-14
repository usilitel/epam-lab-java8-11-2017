package optional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Optional<T> {

    private static final Optional<?> EMPTY = new Optional();
    private T value;

    private Optional() {
    }

    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        return Objects.isNull(value) ? empty() : new Optional<>(value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<T> action) {
        if (isPresent()) {
            action.accept(value);
        }
    }

    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException("Haven't value");
        }
        return value;
    }

    public T orElse(T defaultValue) {
        return isPresent() ? value : defaultValue;
    }

    public T orElseGet(Supplier<T> supplierDefaultValue) {
        return isPresent() ? value : supplierDefaultValue.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exception) throws X {
        if (isPresent()) {
            return value;
        } else {
            throw exception.get();
        }
    }

    public Optional<T> filter(Predicate<T> condition) {
        if (isPresent()) {
            return condition.test(value) ? this : empty();
        } else {
            return empty();
        }
    }

    public <R> Optional<R> map(Function<? super T, ? extends R> remapping) {
        return isPresent() ? new Optional<>(remapping.apply(value)) : empty();
    }

    public <R> Optional<R> flatMap(Function<? super T, Optional<R>> remapping) {
        if (isPresent()) {
            return remapping.apply(value);
        } else {
            return empty();
        }
    }
}









