package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.InputStream;
import java.util.List;

public class JsonDecoder<T> extends ByteToMessageDecoder {

    private final Class<T> clazz;
    private final ObjectMapper mapper;

    public JsonDecoder(Class<T> clazz) {
        this(clazz, JacksonMapper.getInstance());

    }

    public JsonDecoder(Class<T> clazz, ObjectMapper mapper) {
        this.clazz = clazz;
        this.mapper = mapper;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                          List<Object> out) throws Exception {
        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(in);
        T value = mapper.readValue((InputStream) byteBufInputStream, clazz);
        out.add(value);

    }

}