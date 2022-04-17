package pinej.internal;

/**
 * Utility class for calling methods with checked exceptions as if they were unchecked.
 * @param <T> Return type og the method.
 */
@FunctionalInterface
public interface CheckedExceptionHelper<T> {

    static <T> T call(CheckedExceptionHelper<T> callable) {
        try {
            return callable.call();
        } catch (Throwable t) {
            throw new RuntimeException("Function call failed", t);
        }
    }

    T call() throws Throwable;
}

