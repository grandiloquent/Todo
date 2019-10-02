package euphoria.psycho.common;

public interface Interfaces {

    public interface Listener<T> {
        void onSuccess(T t);

        void onFailure(String reson);
    }
}
