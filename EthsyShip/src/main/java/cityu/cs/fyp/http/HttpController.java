package cityu.cs.fyp.http;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;


@SuppressWarnings("restriction")
public class HttpController {
	public static HttpServer server;
	
	public static void createHttpServer() throws IOException, InterruptedException{
		server = HttpServer.create(new InetSocketAddress(8000),0); //Create the server
		server.createContext("/test", new MyHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("Server started at port 8000");
	}
	
	public static void closeHttpServer() {
		server.stop(1); //Close the server
	}
}
