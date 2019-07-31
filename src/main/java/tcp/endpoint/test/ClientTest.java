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
import tcp.endpoint.ClientEndpoint;
import tcp.endpoint.Endpoint;
import tcp.endpoint.LengthBaseChannelInitializer;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
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


        BusinessHandler clientHandler = new DispatcherHandler(registry, gson);

        Endpoint client = new ClientEndpoint(new LengthBaseChannelInitializer(clientHandler), "localhost", 9999);

        new Thread(client::start).start();

        Thread.currentThread().join();

    }
}
