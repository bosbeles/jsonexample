package tcp.endpoint;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ServerEndpoint extends BaseEndpoint {

    private boolean onceConnected;

    private double retryCoeff = 2.0;
    private long minRetryTime = 1000;
    private long currentRetryTime = minRetryTime;
    private long maxRetryTime = 30000;
    private long startupTimeout = 60_000;
    private int startupMaxRetry = -1;
    private boolean closeAfterStartupTimeout = false;

    private int disconnectTimeout = 40_000;
    private int disconnectMaxRetry = -1;
    private boolean closeAfterReconnectTimeout = false;

    private Instant startTime;
    private Instant disconnectedTime;


    private final ChannelInitializer<SocketChannel> initializer;
    private EventLoopGroup workerGroup;



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
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(hostname, port));

            serverBootstrap.childHandler(initializer);
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            log.info("Server is open at {}:{}", hostname, port);
            if(startupTimeout > 0) {
                channelFuture.channel().eventLoop().schedule(()-> log.info("Timeout"), startupTimeout, TimeUnit.MILLISECONDS);
            }

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    @Override
    public synchronized void stop() {
        if(workerGroup != null) {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }
}
