package cityu.cs.fyp.service.tracking;

import java.util.Map;

import org.json.JSONObject;


public class TrackingHandler {

	public static String handleGET(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "getshipmentdetails":
				response = TrackingService.getShipmentDetails(response, map.get("shipmentId"));
				break;
			case "getshipmentwaypoints":
				response = TrackingService.getShipmentWaypoints(response, map.get("shipmentId"));
				break;
			case "getdefaultgeofence":
				response = TrackingService.getDefaultGeoFence(response);
				break;
			case "getDistance":
				response = TrackingService.getDistance(response, map.get("start"), map.get("end"));
				break;
		}
		return response.toString();
	}
	
	public static String handlePOST(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "createshipment":
				response = TrackingService.createShipement(response, map.get("itemId"), map.get("sellerLocation")
						, map.get("buyerLocation"), map.get("distance"), map.get("time"));
				break;
			case "addwaypoints":
				response = TrackingService.addWaypointToRoute(response, map.get("shipmentId"), map.get("name1"), map.get("name2"), map.get("name3"), map.get("count"));
				break;
			case "findwaypoints": 
				response = TrackingService.findWaypoints(response, map.get("sellerLocation"), map.get("buyerLocation"));
				break;
			case "getlocation":
				response = TrackingService.getLocation(response, map.get("address"));
				break;
			case "updatelocation":
				response = TrackingService.updateLocation(response, map.get("address"), map.get("lat"), map.get("lng"));
				break;
		}
		return response.toString();
	}
}
