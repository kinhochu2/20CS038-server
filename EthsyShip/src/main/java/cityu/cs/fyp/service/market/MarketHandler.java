package cityu.cs.fyp.service.market;

import java.util.Map;

import org.json.JSONObject;


public class MarketHandler {

	public static String handleGET(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "loaditems":
				response = MarketService.loadItems(response);
				break;
			case "loaditemsfrombuyer":
				response = MarketService.getItemsFromBuyer(response, map.get("address"));
				break;
			case "loaditemsfromseller":
				System.out.println("loaditemsfromseller begin");
				response = MarketService.getItemsFromSeller(response, map.get("address"));
				break;
			case "getitemprice":
				response = MarketService.getItemPrice(response, map.get("id"));
				break;
		}
		return response.toString();
	}
	
	public static String handlePOST(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "additem":
				response = MarketService.addItem(response, map.get("name"), map.get("seller")
						, Integer.valueOf(map.get("price")), map.get("location"));
				break;
			case "buyitem":
				response = MarketService.buyItem(response, map.get("buyer"), map.get("buyerLocation")
						, Integer.valueOf(map.get("itemId")), Integer.valueOf(map.get("itemAmount")));
				break;
			case "finishshipping":
				response = MarketService.finishShipping(response, map.get("seller"), map.get("buyer")
						, map.get("shipmentId"), map.get("value"), map.get("password"));
				break;
		}
		return response.toString();
	}
}
