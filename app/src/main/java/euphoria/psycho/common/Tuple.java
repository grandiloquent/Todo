package euphoria.psycho.common;
import android.util.Pair;
import java.util.Objects;
public class Tuple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;
    public Tuple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode())
                ^ (third == null ? 0 : third.hashCode());
    }
    public static <A, B, C> Tuple<A, B, C> create(A a, B b, C c) {
        return new Tuple(a, b, c);
    }
}