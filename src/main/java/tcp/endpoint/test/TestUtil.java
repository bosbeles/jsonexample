
package tcp.endpoint.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import handler.Service;
import handler.ServiceRegistry;
import model.*;

public class TestUtil {

    public static Gson createSampleGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        RuntimeTypeAdapterFactory<BaseType> factory = RuntimeTypeAdapterFactory
                .of(model.BaseType.class, "type")
                .registerSubtype(Car.class, "car")
                .registerSubtype(Cat.class, "cat")
                .registerSubtype(MessageA.class, "a")
                .registerSubtype(MessageB.class, "b")
                .registerSubtype(MessageC.class, "c");

        gsonBuilder.registerTypeAdapterFactory(factory);
        Gson gson = gsonBuilder.create();

        return gson;
    }

    public static ServiceRegistry createSampleServiceRegistry() {

        ServiceRegistry registry = new ServiceRegistry();
        Service<Cat> catService = (message, channel) -> System.out.println("A cat: " + message);
        Service<Car> carService = (message, channel) -> System.out.println("A car: " + message);
        Service<Car> secondCarService = (message, channel) -> System.out.println("A car (2): " + message);

        registry.register(Car.class, carService);
        registry.register(Cat.class, catService);
        registry.register(Car.class, secondCarService);

        return registry;
    }

}
