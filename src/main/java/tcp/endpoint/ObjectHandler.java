package tcp.endpoint;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectHandler implements Supplier<List<ChannelHandler>> {


    public ObjectHandler() {

    }

    @Override
    public List<ChannelHandler> get() {
        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new ObjectEncoder());
        handlers.add(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        //handlers.add(new LoggingHandler(LogLevel.ERROR));

        return handlers;
    }
}