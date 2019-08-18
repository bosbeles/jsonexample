package main;

import com.google.gson.Gson;
import handler.Dispatcher;
import handler.ServiceRegistry;
import model.Car;
import model.Cat;
import tcp.endpoint.*;
import tcp.endpoint.test.TestUtil;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Gson gson = TestUtil.createSampleGson();
        ServiceRegistry registry = TestUtil.createSampleServiceRegistry();

        Dispatcher serverDispatcher = new Dispatcher(registry);
        Dispatcher clientDispatcher = new Dispatcher(registry);


        Endpoint server = new ServerEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), 9999);
        Endpoint client = new ClientEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), "localhost", 9999);

        client.onMessage(clientDispatcher::onMessage);
        server.onMessage(serverDispatcher::onMessage);


        new Thread(() -> server.start()).start();
        new Thread(() -> client.start()).start();

        server.onStateChange((o, n) -> {
            if (n == Endpoint.State.CONNECTED) {
                Car car = new Car();
                car.setCompany("Volkswagen");
                car.setModel("Golf");
                server.send(car);
            }
        });

        client.onStateChange((o, n) -> {
            System.out.println(o + " -> " + n);
            if (n == Endpoint.State.CONNECTED) {
                Cat cat = new Cat();
                cat.setName("Minnoş");

                client.send(cat);
            }
        });


        TimeUnit.SECONDS.sleep(10);
        client.stop();
        server.stop();

        System.exit(0);

    }
}
