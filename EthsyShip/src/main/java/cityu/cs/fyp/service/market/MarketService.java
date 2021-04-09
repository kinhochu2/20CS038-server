package cityu.cs.fyp.service.market;

import java.math.BigInteger;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.web3j.tuples.generated.Tuple5;

import cityu.cs.fyp.java_ethereum.MarketContractController;
import cityu.cs.fyp.service.tracking.TrackingService;

public class MarketService {
	private static MarketContractController contractCtrl = MarketContractController.getInstance();

	public static JSONObject addItem(JSONObject response, String name, String seller, int price, String location) {
		String gasUsed = "";
		Boolean hasError = false;
		try {
			gasUsed = contractCtrl.addItem(name, seller, price, location);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		response.put("gasUsed", gasUsed);
		return response;
	}
	
	public static JSONObject loadItems(JSONObject response) {
		Tuple5<List<BigInteger>, List<String>, List<String>, List<String>, List<BigInteger>> result = null;
		Boolean hasError = false;
		JSONObject obj = new JSONObject();
		try {
			result = contractCtrl.getItems();

			JSONArray ids = new JSONArray();
			JSONArray names = new JSONArray();
			JSONArray sellers = new JSONArray();
			JSONArray locations = new JSONArray();
			JSONArray prices = new JSONArray();

			for(int i=0;i<contractCtrl.getItemCount();i++) {
				ids.put(result.component1().get(i).toString());
				names.put(result.component2().get(i).toString());
				sellers.put(result.component3().get(i).toString());
				locations.put(result.component4().get(i).toString());
				prices.put(result.component5().get(i).toString());
			}
			obj.put("ids", ids);
			obj.put("names", names);
			obj.put("sellers", sellers);
			obj.put("prices", prices);
			obj.put("locations", locations);
			obj.put("count", contractCtrl.getItemCount());
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		response.put("result", obj);
		return response;
	}
	
	public static JSONObject buyItem(JSONObject response, String buyer, String buyerLocation, int id, int amount) {
		String gasUsed = "";
		Boolean hasError = false;
		try {
			String sellerLocation = contractCtrl.getItemSellerLocation(String.valueOf(id));
			System.out.println("buyItem, sellerLocation: "+sellerLocation);
			int shipmentId = TrackingService.createShipement(String.valueOf(id), sellerLocation, buyerLocation);
			gasUsed = contractCtrl.buyItem(buyer, buyerLocation, id, amount, shipmentId);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		response.put("gasUsed", gasUsed);
		return response;
	}
	
	public static JSONObject getItemsFromBuyer(JSONObject response, String buyer) {
		Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> result = null;
		Boolean hasError = false;
		JSONObject obj = new JSONObject();
		int count = 0;
		try {
			result = contractCtrl.getItemsFromBuyer(buyer);
			count = result.component1().size();
	
			JSONArray ids = new JSONArray();
			JSONArray names = new JSONArray();
			JSONArray prices = new JSONArray();
			JSONArray shipmentIds = new JSONArray();
			JSONArray sellers = new JSONArray();
			JSONArray sellerLocations = new JSONArray();
			JSONArray amounts = new JSONArray();

			for(int i=0;i<count;i++) {
				ids.put(result.component1().get(i).toString());
				names.put(contractCtrl.getItemName(result.component1().get(i).toString()));
				prices.put(contractCtrl.getItemPrice(result.component1().get(i).toString()));
				shipmentIds.put(result.component2().get(i).toString());
				sellers.put(result.component3().get(i).toString());
				sellerLocations.put(result.component4().get(i).toString());
				amounts.put(result.component5().get(i).toString());
			}
			obj.put("ids", ids);
			obj.put("names", names);
			obj.put("shipmentIds", shipmentIds);
			obj.put("sellers", sellers);
			obj.put("prices", prices);
			obj.put("sellerLocations", sellerLocations);
			obj.put("amounts", amounts);
			obj.put("count", count);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.put("hasError", hasError);
		response.put("result", obj);
		return response;
	}
	
	public static JSONObject getItemsFromSeller(JSONObject response, String seller) {
		Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> result = null;
		Boolean hasError = false;
		JSONObject obj = new JSONObject();
		int count = 0;
		try {
			
			result = contractCtrl.getItemsFromSeller(seller);
			count = result.component1().size();
	
			JSONArray ids = new JSONArray();
			JSONArray names = new JSONArray();
			JSONArray prices = new JSONArray();
			JSONArray shipmentIds = new JSONArray();
			JSONArray buyers = new JSONArray();
			JSONArray buyerLocations = new JSONArray();
			JSONArray amounts = new JSONArray();

			for(int i=0;i<count;i++) {
				ids.put(result.component1().get(i).toString());
				names.put(contractCtrl.getItemName(result.component1().get(i).toString()));
				prices.put(contractCtrl.getItemPrice(result.component1().get(i).toString()));
				shipmentIds.put(result.component2().get(i).toString());
				buyers.put(result.component3().get(i).toString());
				buyerLocations.put(result.component4().get(i).toString());
				amounts.put(result.component5().get(i).toString());
			}
			obj.put("ids", ids);
			obj.put("names", names);
			obj.put("shipmentIds", shipmentIds);
			obj.put("buyers", buyers);
			obj.put("prices", prices);
			obj.put("buyerLocations", buyerLocations);
			obj.put("amounts", amounts);
			obj.put("count", count);
			
//			result = contractCtrl.getItemsFromSeller(seller);
//			count = result.component1().size();
//			for(int i=0;i<count;i++) {
//				JSONObject subobj = new JSONObject();
//				subobj.put("ids", result.component1().get(i).toString());
//				subobj.put("names", contractCtrl.getItemName(result.component1().get(i).toString()));
//				subobj.put("prices", contractCtrl.getItemPrice(result.component1().get(i).toString()));
//				subobj.put("shipmentIds", result.component2().get(i).toString());
//				subobj.put("buyers", result.component3().get(i).toString());
//				subobj.put("buyerLocations", result.component4().get(i).toString());
//				subobj.put("amounts", result.component5().get(i).toString());
//				obj.put("items", subobj);
//			}
//			obj.put("count", count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.put("hasError", hasError);
		response.put("result", obj);
		return response;
	}
	
	public static JSONObject getItemPrice(JSONObject response, String id) {
		int result = 0;
		Boolean hasError = false;
		try {
			result = contractCtrl.getItemPrice(id);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		response.put("result", result);
		return response;
	}
	
	public static JSONObject finishShipping(JSONObject response, String seller, String buyer, String shipmentId, String totalAmount, String password) {
		Boolean hasError = false;
		try {
			contractCtrl.finishShipping(seller, buyer, shipmentId, totalAmount, password);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	
	}
	
}
