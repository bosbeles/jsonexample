package tcp.endpoint.test;

import com.google.gson.Gson;
import handler.Dispatcher;
import handler.ServiceRegistry;
import tcp.endpoint.Endpoint;
import tcp.endpoint.Initializer;
import tcp.endpoint.JsonHandlerWithLengthField;
import tcp.endpoint.ServerEndpoint;

public class ServerTest {

    public static void main(String[] args) {
        Gson gson = TestUtil.createSampleGson();
        ServiceRegistry registry = TestUtil.createSampleServiceRegistry();

        Dispatcher dispatcher = new Dispatcher(registry);

        Endpoint server = new ServerEndpoint(new Initializer<>(new JsonHandlerWithLengthField(gson)), 9999);
        server.onMessage(dispatcher::onMessage);


        new Thread(() -> server.start()).start();
    }
}
