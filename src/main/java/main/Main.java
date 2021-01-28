package main;

import com.google.gson.Gson;
import handler.Dispatcher;
import handler.Service;
import handler.ServiceRegistry;
import model.MessageA;
import model.MessageB;
import model.MessageC;
import tcp.endpoint.*;
import tcp.endpoint.test.TestUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Gson gson = TestUtil.createSampleGson();
        ServiceRegistry registry = TestUtil.createSampleServiceRegistry();
        ServiceRegistry clientRegistry = new ServiceRegistry();
        ServiceRegistry serverRegistry = new ServiceRegistry();


        Dispatcher serverDispatcher = new Dispatcher(serverRegistry);
        Dispatcher clientDispatcher = new Dispatcher(clientRegistry);

        Endpoint[] clients = new Endpoint[50];

        Endpoint server = new ServerEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), 9999);
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new ClientEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), "localhost", 9999);
            clients[i].onMessage(clientDispatcher::onMessage);
            clients[i].setName("Client_" + i);
        }

        server.onMessage(serverDispatcher::onMessage);

        Service<MessageA> serviceA = (m, c) -> {
            m.setTime2(Instant.now());
            MessageB messageB = new MessageB();
            messageB.setTime1(m.getTime1());
            messageB.setTime2(m.getTime2());
            c.writeAndFlush(messageB);
        };

        Service<MessageB> serviceB = (m, c) -> {
            m.setTime3(Instant.now());
            //System.out.println("B: " + Duration.between(m.getTime1(), m.getTime3()));
        };

        Service<MessageC> serviceC = (m, c) -> {
            m.setTime3(Instant.now());
            //System.out.println("C latency: " + Duration.between(m.getTime1(), m.getTime3()) + " " + m + " " + c);
        };

        Service<MessageC> serviceCServer = (m, c) -> {
            m.setTime2(Instant.now());
            server.send(m);
            if(m.getSequence()%1000 == 0) {
                System.out.println("C @server: " + m.getSender() + "-" + m.getSequence() + " Latency = " + Duration.between(m.getTime1(), m.getTime2()));
            }
        };


        clientRegistry.register(MessageB.class, serviceB);
        clientRegistry.register(MessageC.class, serviceC);

        serverRegistry.register(MessageA.class, serviceA);
        serverRegistry.register(MessageC.class, serviceCServer);



        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        new Thread(() -> server.start()).start();
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
                    }, 100, 40, TimeUnit.MILLISECONDS);

                    scheduler.scheduleAtFixedRate(() -> {
                        MessageC c = new MessageC();
                        c.setSequence(sequencer.getAndIncrement());
                        c.setSender(client.getName());
                        c.setTime1(Instant.now());
                        client.send(c);
                    }, 100, 40, TimeUnit.MILLISECONDS);
                }
            });
        }


    }
}
