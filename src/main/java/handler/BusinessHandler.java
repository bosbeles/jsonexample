package handler;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import model.BaseType;

@ChannelHandler.Sharable
public abstract class BusinessHandler extends ChannelInboundHandlerAdapter {

    ChannelGroup allChannels =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Gson gson;


    public BusinessHandler(Gson gson) {
        this.gson = gson;

    }

    public boolean send(BaseType data) {
        if (!allChannels.isEmpty()) {
            allChannels.writeAndFlush(gson.toJson(data, BaseType.class), ChannelMatchers.all(), true);
        }
        return false;
    }

    public abstract void onMessageReceived(BaseType data);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        allChannels.add(ctx.channel());
        super.channelActive(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(msg);
        BaseType baseType = gson.fromJson((String) msg, BaseType.class);
        onMessageReceived(baseType);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
