package tcp.endpoint;

import handler.BusinessHandler;
import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import model.Sample;

public class SampleChannelInitializer<T> extends ChannelInitializer<SocketChannel> {

    private final BusinessHandler handler;

    public SampleChannelInitializer(BusinessHandler handler) {
        this.handler = handler;

    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        //pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        //pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("jsonDecoder", new JsonDecoder<Sample>(Sample.class));
        pipeline.addLast("jsonEncoder", new JsonEncoder<Sample>());
        pipeline.addLast("handler", handler);

    }
}