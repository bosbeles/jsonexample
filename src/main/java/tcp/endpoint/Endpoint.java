package tcp.endpoint;

import io.netty.channel.Channel;
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

    void send(Object data, Channel channel);

    void onMessage(BiConsumer<Object, Channel> onMessage);

    void onError(Consumer<Throwable> onError);

    void onStateChange(BiConsumer<State, State> onStateChange);

    String getName();

    void setName(String name);

    EventLoopGroup getEventLoop();

}
