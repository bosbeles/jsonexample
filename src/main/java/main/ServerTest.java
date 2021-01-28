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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class ServerTest {


    public static void main(String[] args) throws InterruptedException {
        Gson gson = TestUtil.createSampleGson();
        Supplier<Initializer<SocketChannel>> initializerSupplier = () -> new Initializer<>(new JsonHandlerWithLengthField(gson));
        //Supplier<Initializer<SocketChannel>> initializerSupplier = () -> new Initializer<>(new ObjectHandler());

        ServiceRegistry serverRegistry = new ServiceRegistry();


        Dispatcher serverDispatcher = new Dispatcher(serverRegistry);


        Endpoint server = new ServerEndpoint(initializerSupplier.get(), 9999);


        server.onMessage(serverDispatcher::onMessage);

        Service<MessageA> serviceA = (m, c) -> {
            m.setTime2(Instant.now());
            MessageB messageB = new MessageB();
            messageB.setTime1(m.getTime1());
            messageB.setTime2(m.getTime2());
            c.writeAndFlush(messageB);
        };


        Service<MessageC> serviceCServer = (m, c) -> {
            m.setTime2(Instant.now());
            server.send(m);
            if (m.getSequence() % 1000 == 0) {
                System.out.println("C @server: " + m.getSender() + "-" + m.getSequence() + " Latency = " + Duration.between(m.getTime1(), m.getTime2()));
            }
        };

        serverRegistry.register(MessageA.class, serviceA);
        serverRegistry.register(MessageC.class, serviceCServer);


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        new Thread(() -> server.start()).start();


    }
}
