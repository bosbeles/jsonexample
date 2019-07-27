package tcp.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ClientEndpoint extends BaseEndpoint {

    private final ChannelInitializer<SocketChannel> initializer;

    public ClientEndpoint(ChannelInitializer<SocketChannel> initializer, int port) {
        super(port);
        this.initializer = initializer;
    }

    public ClientEndpoint(ChannelInitializer<SocketChannel> initializer, String hostname, int port) {
        super(hostname, port);
        this.initializer = initializer;
    }


    @Override
    public void start()  {
        stop();

        group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(hostname, port));
            clientBootstrap.handler(initializer);
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            System.out.println("Client connected to " + hostname + ":" + port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }


}
