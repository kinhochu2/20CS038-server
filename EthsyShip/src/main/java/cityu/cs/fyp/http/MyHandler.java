package cityu.cs.fyp.http;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.*;

import cityu.cs.fyp.service.auth.AuthHandler;
import cityu.cs.fyp.service.market.MarketHandler;
import cityu.cs.fyp.service.proof.ProofHandler;
import cityu.cs.fyp.service.tracking.TrackingHandler;

import org.apache.commons.io.IOUtils;

public class MyHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Receive request.");
					String requestUrl = httpExchange.getRequestURI().toString();
					String requestLib = requestUrl.split("\\/")[2];
					String requestFunc = requestUrl.split("\\/")[3];
					System.out.println("method: "+httpExchange.getRequestMethod());
					System.out.println("requestUrl: "+requestUrl);
					System.out.println("requestLib: "+requestLib);
					System.out.println("requestFunc: "+requestFunc);
					
					if("GET".equals(httpExchange.getRequestMethod())) {
						String queryString = httpExchange.getRequestURI().getQuery();
				        Map<String,String> getInfo = formData2Dic(queryString);
				        System.out.println("getInfo: "+getInfo.toString());
				        if(requestFunc.contains("?")) {
				        	requestFunc = requestFunc.split("[?]")[0];
				        	System.out.println("requestFunc after split: "+requestFunc);
				        }
				        String response = "";
				        if(requestLib.equals("auth")){
				        	response = AuthHandler.handleGET(requestFunc, getInfo);
				        }else if(requestLib.equals("market")){
				        	response = MarketHandler.handleGET(requestFunc, getInfo);
				        }else if(requestLib.equals("tracking")){
				        	response = TrackingHandler.handleGET(requestFunc, getInfo);
				        }else if(requestLib.equals("proof")){
				        	response = ProofHandler.handleGET(requestFunc, getInfo);
				        }
				        sendResponse(httpExchange, response);
					}
					if("POST".equals(httpExchange.getRequestMethod())) {
						@SuppressWarnings("deprecation")
						String postString = IOUtils.toString(httpExchange.getRequestBody());
						System.out.println("postString: "+postString);
						Map<String,String> postInfo = formData2Dic(postString);
						
						String response = "";
						if(requestLib.equals("auth")){
				        	response = AuthHandler.handlePOST(requestFunc, postInfo);
				        }else if(requestLib.equals("market")){
				        	response = MarketHandler.handlePOST(requestFunc, postInfo);
				        }else if(requestLib.equals("tracking")){
				        	response = TrackingHandler.handlePOST(requestFunc, postInfo);
				        }else if(requestLib.equals("proof")){
				        	response = ProofHandler.handlePOST(requestFunc, postInfo);
				        }
						sendResponse(httpExchange, response);
					}
					if("OPTIONS".equals(httpExchange.getRequestMethod())) {
						sendResponse(httpExchange,"");
					}
					
				}catch (IOException ie) {
					ie.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
	@SuppressWarnings("restriction")
	private void sendResponse(HttpExchange httpExchange, String responseStr) throws IOException { 
		httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
		httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
		httpExchange.sendResponseHeaders(200, responseStr.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(responseStr.getBytes());
		os.flush();
		os.close();
	}
	
	private Map<String,String> formData2Dic(String formData ) {
        Map<String,String> result = new HashMap<>();
        if(formData== null || formData.trim().length() == 0) {
            return result;
        }
        final String[] items = formData.split("&");
        Arrays.stream(items).forEach(item ->{
            final String[] keyAndVal = item.split("=");
            if(keyAndVal.length == 2) {
                try{
                    final String key = URLDecoder.decode( keyAndVal[0],"utf8");
                    final String val = URLDecoder.decode( keyAndVal[1],"utf8");
                    result.put(key,val);
                }catch (UnsupportedEncodingException e) {}
            }
        });
        return result;
    }
	
	
}
