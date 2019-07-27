package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class BusinessHandler<T> extends ChannelInboundHandlerAdapter {


    private ChannelHandlerContext ctx;

    public boolean send(T data) {
        if (ctx != null) {
            ctx.writeAndFlush(data);
        }
        return false;
    }

    public abstract void onMessageReceived(T data);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.ctx = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        onMessageReceived((T) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
