package tcp.endpoint;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

import java.util.List;
import java.util.function.Supplier;

public class Initializer<C extends Channel> extends ChannelInitializer<C> {

    private final Supplier<List<ChannelHandler>> handlerFactory;
    private ChannelHandler businessHandler;

    public Initializer(Supplier<List<ChannelHandler>> handlerFactory) {
        this.handlerFactory = handlerFactory;
    }


    @Override
    protected void initChannel(C ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (handlerFactory != null) {
            List<ChannelHandler> channelHandlers = handlerFactory.get();
            if (channelHandlers != null) {
                for (ChannelHandler handler : channelHandlers) {
                    pipeline.addLast(handler);
                }
            }
        }

        if (businessHandler != null) {
            pipeline.addLast(businessHandler);
        }
    }

    public void setBusinessHandler(ChannelHandler businessHandler) {
        this.businessHandler = businessHandler;
    }
}
