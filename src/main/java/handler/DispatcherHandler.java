package handler;

import com.google.gson.Gson;
import model.BaseType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DispatcherHandler extends BusinessHandler {

    private final ServiceRegistry serviceRegistry;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public DispatcherHandler(ServiceRegistry serviceRegistry, Gson gson) {
        super(gson);
        this.serviceRegistry = serviceRegistry;

    }

    @Override
    public void onMessageReceived(BaseType data) {
        scheduler.submit(() -> serviceRegistry.dispatch(data.getClass(), data));
    }
}
