package tcp.endpoint.test;

import com.google.gson.Gson;
import handler.Dispatcher;
import handler.ServiceRegistry;
import tcp.endpoint.ClientEndpoint;
import tcp.endpoint.Endpoint;
import tcp.endpoint.Initializer;
import tcp.endpoint.JsonHandlerWithLengthField;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException {

        Gson gson = TestUtil.createSampleGson();
        ServiceRegistry registry = TestUtil.createSampleServiceRegistry();

        Dispatcher dispatcher = new Dispatcher(registry);
        Endpoint client = new ClientEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), "localhost", 9999);
        client.onMessage(dispatcher::onMessage);


        new Thread(client::start).start();
        

        Thread.currentThread().join();

    }
}
