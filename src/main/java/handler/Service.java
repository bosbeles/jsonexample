package handler;

public interface Service<T> {

    void onMessage(T message);
}
