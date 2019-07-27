package tcp.endpoint;

import io.netty.channel.EventLoopGroup;

public abstract class BaseEndpoint implements Endpoint {

    protected final String hostname;
    protected final int port;
    protected EventLoopGroup group;

    public BaseEndpoint(int port) {
        this("localhost", port);
    }

    public BaseEndpoint(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void stop() {
        if (group != null && !group.isShutdown()) {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
