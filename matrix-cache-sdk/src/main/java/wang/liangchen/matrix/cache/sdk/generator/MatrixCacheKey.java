package wang.liangchen.matrix.cache.sdk.generator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Liangchen.Wang
 */
public class MatrixCacheKey implements Serializable {
    private final Object[] params;
    // Effectively final, just re-calculated on deserialization
    private transient int hashCode;

    public MatrixCacheKey(Object target, Method method, Object... elements) {
        this.params = new Object[elements.length + 2];
        this.params[0] = target.getClass().getSimpleName();
        this.params[1] = method.getName();
        System.arraycopy(elements, 0, this.params, 2, elements.length);
        this.hashCode = Arrays.deepHashCode(this.params);
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof MatrixCacheKey that && Arrays.deepEquals(this.params, that.params)));
    }

    @Override
    public final int hashCode() {
        // Expose pre-calculated hashCode field
        return this.hashCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.deepToString(this.params);
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Re-calculate hashCode field on deserialization
        this.hashCode = Arrays.deepHashCode(this.params);
    }
}
