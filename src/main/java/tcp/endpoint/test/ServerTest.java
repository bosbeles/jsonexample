package tcp.endpoint.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import handler.BusinessHandler;
import handler.DispatcherHandler;
import handler.Service;
import handler.ServiceRegistry;
import model.BaseType;
import model.Car;
import model.Cat;
import tcp.endpoint.Endpoint;
import tcp.endpoint.LengthBaseChannelInitializer;
import tcp.endpoint.ServerEndpoint;

public class ServerTest {

    public static void main(String[] args) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        RuntimeTypeAdapterFactory<BaseType> factory = RuntimeTypeAdapterFactory
                .of(model.BaseType.class, "type")
                .registerSubtype(Car.class, "car")
                .registerSubtype(Cat.class, "cat");
        gsonBuilder.registerTypeAdapterFactory(factory);
        Gson gson = gsonBuilder.create();


        ServiceRegistry registry = new ServiceRegistry();
        Service<Cat> catService = message -> System.out.println("A cat: " + message);
        Service<Car> carService = message -> System.out.println("A car: " + message);
        Service<Car> secondCarService = message -> System.out.println("A car (2): " + message);

        registry.register(Car.class, carService);
        registry.register(Cat.class, catService);
        registry.register(Car.class, secondCarService);


        BusinessHandler serverHandler = new DispatcherHandler(registry, gson);

        Endpoint server = new ServerEndpoint(new LengthBaseChannelInitializer(serverHandler), 9999);


        new Thread(() -> server.start()).start();
    }
}
