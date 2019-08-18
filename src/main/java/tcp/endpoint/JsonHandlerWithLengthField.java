package tcp.endpoint;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import model.BaseType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JsonHandlerWithLengthField implements Supplier<List<ChannelHandler>> {

    private static final int LENGTH_FIELD_LENGTH = 4;
    private final int maxFrameSize;
    private final Gson gson;


    public JsonHandlerWithLengthField(Gson gson) {
        this(gson, 1024 * 1024); // 1MB
    }

    public JsonHandlerWithLengthField(Gson gson, int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        this.gson = gson;
    }


    @Override
    public List<ChannelHandler> get() {
        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new LengthFieldBasedFrameDecoder(maxFrameSize, 0, LENGTH_FIELD_LENGTH, 0, LENGTH_FIELD_LENGTH));
        handlers.add(new LengthFieldPrepender(LENGTH_FIELD_LENGTH));
        handlers.add(new LoggingHandler(LogLevel.INFO));
        handlers.add(new StringDecoder(CharsetUtil.UTF_8));
        handlers.add(new StringEncoder(CharsetUtil.UTF_8));
        handlers.add(new MessageToMessageCodec<String, BaseType>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, BaseType msg, List<Object> out) {
                out.add(gson.toJson(msg, BaseType.class));
            }

            @Override
            protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
                out.add(gson.fromJson(msg, BaseType.class));
            }
        });

        return handlers;
    }
}