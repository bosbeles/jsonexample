package tcp.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ClientEndpoint extends BaseEndpoint {

    private final ChannelInitializer<SocketChannel> initializer;


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

    public ClientEndpoint(Initializer<SocketChannel> initializer, int port) {
        this(initializer, "localhost", port);
    }

    public ClientEndpoint(Initializer<SocketChannel> initializer, String hostname, int port) {
        super(hostname, port);
        this.initializer = initializer;
        plugBusinessHandler(initializer);

    }


    @Override
    public synchronized void start() {
        stopStart();
        startTime = Instant.now();
        group = new NioEventLoopGroup();

        doConnect();
    }


    public void doConnect() {
        try {
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(hostname, port));
            clientBootstrap.handler(initializer);


            ChannelFuture channelFuture = clientBootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    peerConnected(future.channel().localAddress());
                }
            });

            channel = channelFuture.channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    stateChanged(State.DISCONNECTED);
                    tryToRetry();
                }
            });

        } catch (Exception e) {
            log.error("Client error", e);
        } finally {
            log.trace("Finally");
        }

    }

    private synchronized void stopFail() {
        log.info("Fail");
        stateChanged(State.STOPPED);
        stop();
    }

    private synchronized void stopStart() {
        log.info("Starting");
        stateChanged(State.STARTING);
        stop();
    }


    private void peerConnected(SocketAddress socketAddress) {
        stateChanged(State.CONNECTED);

        onceConnected = true;
        disconnectedTime = null;
        currentRetryTime = minRetryTime;
        log.info("Client connected to remote {}:{} at local address {}", hostname, port, socketAddress);
    }

    private void tryToRetry() {
        if (onceConnected) {
            if (disconnectedTime == null) {
                disconnectedTime = Instant.now();
                log.info("Reconnecting");
            }
            if (disconnectMaxRetry != 0 || disconnectTimeout != 0) {
                group.schedule(this::retryAfterDisconnect, getNextRetryTimeAfterDisconnect(), TimeUnit.MILLISECONDS);
            }

        } else if (startupMaxRetry != 0 || startupTimeout != 0) {
            group.schedule(this::retryAtStartup, getNextRetryTimeAtStartup(), TimeUnit.MILLISECONDS);
        }
    }

    private long getNextRetryTimeAtStartup() {
        long d = Duration.between(startTime, Instant.now()).toMillis();
        long next = (long) (currentRetryTime * retryCoeff);
        currentRetryTime = Math.min(maxRetryTime, next);
        if (d < startupTimeout) {
            long timeTo = startupTimeout - d;
            currentRetryTime = Math.min(currentRetryTime, timeTo);
        }
        return currentRetryTime;
    }

    private long getNextRetryTimeAfterDisconnect() {
        long d = Duration.between(disconnectedTime, Instant.now()).toMillis();
        long next = (long) (currentRetryTime * retryCoeff);
        currentRetryTime = Math.min(maxRetryTime, next);
        if (d < disconnectTimeout) {
            long timeTo = disconnectTimeout - d;
            currentRetryTime = Math.min(currentRetryTime, timeTo);

        }
        return currentRetryTime;
    }

    private void retryAtStartup() {
        log.info("Retrying...");
        Instant now = Instant.now();
        long d = Duration.between(startTime, now).toMillis();

        if (startupTimeout == -1 || d < startupTimeout) {
            doConnect();
        } else if (startupMaxRetry > 0) {
            startupMaxRetry--;
            doConnect();
        } else if (closeAfterStartupTimeout) {
            stopFail();
        } else {
            // State timeout
            stateChanged(State.TIMEOUT);
            log.info("Timeout");
            doConnect();
        }
    }

    private void retryAfterDisconnect() {
        log.info("Retrying after disconnect...");
        Instant now = Instant.now();
        long d = Duration.between(disconnectedTime, now).toMillis();

        if (disconnectTimeout == -1 || d < disconnectTimeout) {
            doConnect();
        } else if (disconnectMaxRetry > 0) {
            disconnectMaxRetry--;
            doConnect();
        } else if (closeAfterReconnectTimeout) {
            stopFail();
        } else {
            // State timeout
            stateChanged(State.TIMEOUT);
            log.info("Timeout");
            doConnect();
        }
    }


}
