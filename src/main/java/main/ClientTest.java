package main;

import com.google.gson.Gson;
import handler.Dispatcher;
import handler.Service;
import handler.ServiceRegistry;
import io.netty.channel.socket.SocketChannel;
import model.MessageA;
import model.MessageB;
import model.MessageC;
import tcp.endpoint.*;
import tcp.endpoint.test.TestUtil;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ClientTest {


    public static void main(String[] args) throws InterruptedException {
        Gson gson = TestUtil.createSampleGson();
        //Supplier<Initializer<SocketChannel>> initializerSupplier = () -> new Initializer(new JsonHandlerWithLengthField(gson));
        Supplier<Initializer<SocketChannel>> initializerSupplier = () -> new Initializer<>(new ObjectHandler());

        ServiceRegistry clientRegistry = new ServiceRegistry();
        Dispatcher clientDispatcher = new Dispatcher(clientRegistry);

        Endpoint[] clients = new Endpoint[250];

        Endpoint server = new ServerEndpoint(initializerSupplier.get(), 9999);
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new ClientEndpoint(initializerSupplier.get(), "localhost", 9999);
            clients[i].onMessage(clientDispatcher::onMessage);
            clients[i].setName("Client_" + i);
        }


        Service<MessageB> serviceB = (m, c) -> {
            m.setTime3(Instant.now());
            //System.out.println("B: " + Duration.between(m.getTime1(), m.getTime3()));
        };

        Service<MessageC> serviceC = (m, c) -> {
            m.setTime3(Instant.now());
            //System.out.println("C latency: " + Duration.between(m.getTime1(), m.getTime3()) + " " + m + " " + c);
        };


        clientRegistry.register(MessageB.class, serviceB);
        clientRegistry.register(MessageC.class, serviceC);


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        for (int i = 0; i < clients.length; i++) {
            Endpoint client = clients[i];
            AtomicInteger sequencer = new AtomicInteger();
            client.start();
            client.onStateChange((o, n) -> {
                System.out.println(o + " -> " + n);
                if (n == Endpoint.State.CONNECTED) {
                    scheduler.scheduleAtFixedRate(() -> {
                        MessageA a = new MessageA();
                        a.setTime1(Instant.now());
                        client.send(a);
                    }, 100, 100, TimeUnit.MILLISECONDS);

                    scheduler.scheduleAtFixedRate(() -> {
                        MessageC c = new MessageC();
                        c.setSequence(sequencer.getAndIncrement());
                        c.setSender(client.getName());
                        c.setTime1(Instant.now());
                        client.send(c);
                    }, 100, 1000, TimeUnit.MILLISECONDS);
                }
            });
        }


    }
}
