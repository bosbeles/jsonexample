package tcp.endpoint;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class ServerEndpoint extends BaseEndpoint {


    private final ChannelInitializer<SocketChannel> initializer;

    public ServerEndpoint(ChannelInitializer<SocketChannel> initializer, int port) {
        super(port);
        this.initializer = initializer;
    }

    public ServerEndpoint(ChannelInitializer<SocketChannel> initializer, String hostname, int port) {
        super(hostname, port);
        this.initializer = initializer;

    }

    @Override
    public void start() {
        stop();
        group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(hostname, port));

            serverBootstrap.childHandler(initializer);
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("Server is open at " + hostname + ":" + port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }


}
