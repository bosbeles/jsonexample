package tcp.endpoint;

import io.netty.channel.EventLoopGroup;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Endpoint {

    enum State {
        STARTING, CONNECTED, STOPPED, DISCONNECTED, RECONNECTING, TIMEOUT
    }

    void start();

    void stop();

    void send(Object data);

    void onMessage(Consumer<Object> onMessage);

    void onError(Consumer<Throwable> onError);

    void onStateChange(BiConsumer<State, State> onStateChange);

    EventLoopGroup getEventLoop();

}
