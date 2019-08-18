package handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Dispatcher {

    private final ServiceRegistry serviceRegistry;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Dispatcher(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

    }

    public void onMessage(Object data) {
        System.out.println("Dispatcher: " + data);
        scheduler.submit(() -> serviceRegistry.dispatch(data.getClass(), data));
    }
}
