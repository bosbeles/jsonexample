package handler;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceRegistry {

    private Map<Class<?>, List<Service>> services = new ConcurrentHashMap<>();


    public <T> void register(Class<T> clazz, Service<T> service) {
        services.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>()).add(service);

    }

    public <T> void unregister(Class<T> clazz) {
        services.remove(clazz);
    }


    public <T> void dispatch(Class<T> clazz, Object data, Channel channel) {
        List<Service> services = this.services.get(clazz);
        if (services != null) {
            services.forEach(s -> s.onMessage(data, channel));
        }
    }
}
