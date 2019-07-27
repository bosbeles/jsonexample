package main;

import handler.BusinessHandler;
import model.Sample;
import tcp.endpoint.ClientEndpoint;
import tcp.endpoint.Endpoint;
import tcp.endpoint.SampleChannelInitializer;
import tcp.endpoint.ServerEndpoint;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        BusinessHandler<Sample> serverHandler = new BusinessHandler<Sample>() {
            @Override
            public void onMessageReceived(Sample data) {
                System.out.println("Server received: " + data);
            }
        };

        BusinessHandler<Sample> clientHandler = new BusinessHandler<Sample>() {
            @Override
            public void onMessageReceived(Sample data) {
                System.out.println("Client received: " + data);
            }
        };

        Endpoint server = new ServerEndpoint(new SampleChannelInitializer<>(serverHandler), 9999);
        Endpoint client = new ClientEndpoint(new SampleChannelInitializer<>(clientHandler), "localhost", 9999);


        new Thread(() -> server.start()).start();
        TimeUnit.SECONDS.sleep(5);
        new Thread(()-> client.start()).start();
        TimeUnit.SECONDS.sleep(1);

        Sample s = new Sample();
        s.setId(13);
        s.setName("ABCöçşiğüıIĞÜŞİÖÇXYZ");

        serverHandler.send(s);

        s = new Sample();
        s.setId(14);
        s.setName("ABCöçşiğüıIĞÜŞİÖÇXYZ");
        s.setCourses(Arrays.asList("CS 101", "CS 102"));

        clientHandler.send(s);




    }
}
