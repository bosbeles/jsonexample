package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import model.Sample;

import java.io.OutputStream;

public class JsonEncoder<T> extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
            throws Exception {

        ObjectMapper mapper = JacksonMapper.getInstance(); // create once, reuse
		// byte[] body =  mapper.writeValueAsBytes(msg);
        // out.writeBytes(body);
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(out);
        mapper.writeValue((OutputStream) byteBufOutputStream, msg);
    }

}