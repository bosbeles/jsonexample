package handler;

import io.netty.channel.Channel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Dispatcher {

    private final ServiceRegistry serviceRegistry;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Dispatcher(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

    }

    public void onMessage(Object data, Channel channel) {
        //System.out.println("Dispatcher: " + data + " ch: " + channel);
        scheduler.submit(() -> serviceRegistry.dispatch(data.getClass(), data, channel));
    }
}
