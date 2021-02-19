package nav.portal.core.exceptionHandling;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExceptionUtil {
    private ExceptionUtil(){};

    public static RuntimeException soften(Exception e) {
        return softenToRuntimeException(e);
    }

    public static <T, R, EX extends Exception> Function<T,R> soften(FunctionWithException<T, R, EX> f) throws EX {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception ex) {
                throw softenToRuntimeException(ex);
            }
        };
    }

    public static <T, EX extends Exception> Consumer<T> softenConsumer(ConsumerWithException<T, EX> f) throws EX {
        return t -> {
            try {
                f.accept(t);
            } catch (Exception ex) {
                throw softenToRuntimeException(ex);
            }
        };
    }

    public static <T, U, EX extends Exception> BiConsumer<T, U> softenConsumer(BiConsumerWithException<T, U, EX> f) throws EX {
        return (t, u) -> {
            try {
                f.accept(t, u);
            } catch (Exception ex) {
                throw softenToRuntimeException(ex);
            }
        };
    }

    private static <T extends Exception> T softenToRuntimeException(Exception e) throws T {
        throw (T)e;
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R, EX extends Exception> {
        R apply(T t) throws EX, MalformedURLException, SQLException;
    }

    @FunctionalInterface
    public interface ConsumerWithException<T, EX extends Exception> {
        void accept(T t) throws EX, MalformedURLException, SQLException;
    }

    @FunctionalInterface
    public interface BiConsumerWithException<T, U, EX extends Exception> {
        void accept(T t, U u) throws EX, SQLException;
    }
}
