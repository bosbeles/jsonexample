package handler;

import io.netty.channel.Channel;

public interface Service<T> {

    void onMessage(T message, Channel channel);
}
