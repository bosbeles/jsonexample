package tcp.endpoint;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Log4j2
public abstract class BaseEndpoint implements Endpoint {

    protected final String hostname;
    protected final int port;
    protected EventLoopGroup group;
    protected volatile BiConsumer<Object, Channel> onMessage;
    protected volatile Channel channel;
    protected volatile Consumer<Throwable> onError;
    protected volatile BiConsumer<State, State> onStateChange;
    protected volatile State state;
    protected ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    protected String name = "";


    private Object stateLock = new Object();


    public BaseEndpoint(int port) {
        this("localhost", port);
    }

    public BaseEndpoint(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void send(Object message) {
        channelGroup.writeAndFlush(message);
    }

    @Override
    public void send(Object message, Channel ch) {
        if (ch != null) {
            ch.writeAndFlush(message);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void onMessage(BiConsumer<Object, Channel> onMessage) {
        this.onMessage = onMessage;
    }


    @Override
    public void onError(Consumer<Throwable> onError) {
        this.onError = onError;
    }


    @Override
    public void onStateChange(BiConsumer<State, State> onStateChange) {
        this.onStateChange = onStateChange;
    }

    @Override
    public EventLoopGroup getEventLoop() {
        return group;
    }

    protected void stateChanged(State newState) {
        synchronized (stateLock) {
            State oldState = this.state;
            this.state = newState;
            if (onStateChange != null) {
                onStateChange.accept(oldState, newState);
            }
        }

    }

    protected void plugBusinessHandler(Initializer<SocketChannel> initializer) {
        initializer.setBusinessHandler(new MySimpleChannelInboundHandler());
    }

    public synchronized void stop() {
        if (group != null && !group.isShutdown()) {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @ChannelHandler.Sharable
    private class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            if (onMessage != null) {
                onMessage.accept(msg, ctx.channel());
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            channelGroup.add(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            channelGroup.remove(ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (onError != null) {
                onError.accept(cause);
            } else {
                log.error("Exception caught in endpoint.", cause);
            }
            ctx.close();
        }
    }
}
