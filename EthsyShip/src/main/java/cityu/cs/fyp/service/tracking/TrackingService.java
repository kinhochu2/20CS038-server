package cityu.cs.fyp.service.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import cityu.cs.fyp.firebase.FirestoreProvider;
import cityu.cs.fyp.http.HttpRequestClient;
import cityu.cs.fyp.java_ethereum.LocationContractController;
import cityu.cs.fyp.util.DistanceUtil;

public class TrackingService {

	private static final String MapQuestKey = "2sdNBLXdUDfxllrLzrxrNuuO9l1DGtt0";
	private static LocationContractController contractCtrl = LocationContractController.getInstance();
	
	private static class Location {
		String name;
		double lat;
		double lng;
		double dist;
		
		public Location(String name, double lat, double lng, double dist) {
			this.name = name;
			this.lat = lat;
			this.lng = lng;
			this.dist = dist;
		}
	}

	private static FirestoreProvider firestoreProvider = FirestoreProvider.getInstance();
	
	public static JSONObject getDefaultGeoFence(JSONObject response) {
		List<QueryDocumentSnapshot> list = firestoreProvider.getData("Geofence");
		for(QueryDocumentSnapshot doc: list) {
			JSONArray array = new JSONArray();
			array.put(doc.getId());
			array.put(doc.get("lat"));
			array.put(doc.get("lng"));
			response.put("locations", array);
		}
		return response;
	}
	
	public static JSONObject getShipmentDetails(JSONObject response, String id) {
		Tuple3<String, String, String> result = null;
		Boolean hasError = false;
		JSONObject obj = new JSONObject();
		try {
			result = contractCtrl.getShipmentDetails(id);
			response.put("itemId", result.component1());
			response.put("sellerLocation", result.component2());
			response.put("buyerLocation", result.component3());
			JSONObject waypoints = TrackingService.getShipmentWaypoints(response, id);
			response.put("waypointSet", waypoints.get("hasWaypointSet"));
			
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject createShipement(JSONObject response, String itemId, String sName
			, String eName, String distance, String time) {
		String gasUsed = "";
		Boolean hasError = false;
		try {
			gasUsed = contractCtrl.createShipment(itemId, sName, eName, distance, time);
			int shipmentId = contractCtrl.getShipmentId();
			System.out.println("createShipement, shipmentId: "+shipmentId);
			response.put("shipmentId", shipmentId);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("gasUsed", gasUsed);
		response.put("hasError", hasError);
		return response;
	}
	
	public static int createShipement(String itemId, String sellerLocation, String buyerLocation) {
		int id = -1;
		try {
			contractCtrl.createShipment(itemId, sellerLocation, buyerLocation, "empty", "empty");
			id = contractCtrl.getShipmentId();
			System.out.println("createShipement, shipmentId: "+id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public static JSONObject addWaypointToRoute(JSONObject response, String id, String name1, String name2, String name3, String count) {
		Boolean hasError = false;
		try {
			if(Integer.valueOf(count)==3) {
				contractCtrl.addWaypointToRoute(id, name1, false);
				contractCtrl.addWaypointToRoute(id, name2, false);
				contractCtrl.addWaypointToRoute(id, name3, true);
			}else if(Integer.valueOf(count)==2) {
				contractCtrl.addWaypointToRoute(id, name1, false);
				contractCtrl.addWaypointToRoute(id, name2, true);
			}else if(Integer.valueOf(count)==1) {
				contractCtrl.addWaypointToRoute(id, name1, true);
			}else if(Integer.valueOf(count)==0) {
				contractCtrl.setWaypointToRouteFinished(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject getShipmentWaypoints(JSONObject response, String id) {
		Boolean hasError = false;
		Tuple2<List<String>, Boolean> waypoints = null;
		try {
			waypoints = contractCtrl.getShipmentWaypoints(id);
			response.put("hasWaypointSet", waypoints.component2());
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		for(int i=0;i<waypoints.component1().size();i++) {
			if(!waypoints.component1().get(i).equals("") && !waypoints.component1().get(i).equals(" "))
				response.append("waypointName", waypoints.component1().get(i));
		}
		return response;
	}
	
	public static JSONObject findWaypoints(JSONObject response, String sellerLocation, String buyerLocation) {
		double slat = 0.0, slng = 0.0, elat =0.0, elng = 0.0;
    	try {
	    	HttpRequestClient obj = new HttpRequestClient();
	    	Map<Object, Object> data = new HashMap<>();
	    	data.put("location", sellerLocation);
			JSONObject result = obj.sendPost("http://open.mapquestapi.com/geocoding/v1/address?key="+MapQuestKey, data);
			JSONObject latLng = result.getJSONArray("results").getJSONObject(0).getJSONArray("locations")
					.getJSONObject(0).getJSONObject("latLng");
			slat = latLng.getDouble("lat");
			slng = latLng.getDouble("lng");
	    	data.clear();
	    	data.put("location", buyerLocation);
	    	result = obj.sendPost("http://open.mapquestapi.com/geocoding/v1/address?key="+MapQuestKey, data);
	    	latLng = result.getJSONArray("results").getJSONObject(0).getJSONArray("locations").getJSONObject(0).getJSONObject("latLng");
	    	elat = latLng.getDouble("lat");
			elng = latLng.getDouble("lng");
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
		double refDist = DistanceUtil.distance(slat, slng, elat, elng, 'K'); // distance from starting point to destination
		System.out.println("refDist: "+refDist);
		List<QueryDocumentSnapshot> list = firestoreProvider.getData("Geofence"); // retrieve document reference from db
		List<Location> topWaypoints = new ArrayList<Location>();
		
		Comparator<Location> comp = (Location a, Location b) -> { // comparator for sorting waypoints
		    return b.dist > a.dist ? -1 : (b.dist < a.dist) ? 1 : 0;
		};
		Comparator<Location> compX = (Location a, Location b) -> { // comparator for sorting waypoint's latitudes
		    return b.lat < a.lat ? -1 : (b.lat > a.lat) ? 1 : 0;
		};
		for(QueryDocumentSnapshot doc: list) {
			double dist1 = DistanceUtil.distance(doc.getDouble("lat"), doc.getDouble("lng"), 
					Double.valueOf(slat), Double.valueOf(slng), 'K'); // distance from waypoint to starting point
			double dist2 = DistanceUtil.distance(doc.getDouble("lat"), doc.getDouble("lng"), 
					Double.valueOf(elat), Double.valueOf(elng), 'K'); // distance from waypoint to destination
			if(((dist1+dist2) - refDist) <= 2.0 && (dist1+dist2) - refDist > 0.0) {
				topWaypoints.add(new Location(doc.getId(), doc.getDouble("lat"), doc.getDouble("lng"), dist1+dist2));
				if(topWaypoints.size() == 4) {
					topWaypoints.sort(comp); // sort the array list
					topWaypoints.remove(topWaypoints.size()-1); // remove the last waypoint from the list
				}
			}
		}
		topWaypoints.sort(compX);
		double totalDist = 0.0;
		double tlat = slat; double tlng = slng;
		for(Location l: topWaypoints) {
			JSONObject obj = new JSONObject();
			obj.put("name", l.name);
			obj.put("lat", l.lat);
			obj.put("lng", l.lng);
			response.append("waypoints", obj);
			totalDist += DistanceUtil.distance(tlat, tlng, l.lat, l.lng, 'K');
			tlat = l.lat; tlng = l.lng;
		}
		totalDist += DistanceUtil.distance(tlat, tlng, elat, elng, 'K');
		totalDist *= 1000;
		response.put("count", topWaypoints.size());
		response.put("distance", totalDist);
		System.out.println("findWaypoints result: "+response.toString());
		return response;
	}
	
	public static JSONObject getDistance(JSONObject response, String start, String end) {
		double slat = 0.0, slng = 0.0, elat =0.0, elng = 0.0;
    	try {
	    	HttpRequestClient obj = new HttpRequestClient();
	    	Map<Object, Object> data = new HashMap<>();
	    	data.put("location", start);
			JSONObject result = obj.sendPost("http://open.mapquestapi.com/geocoding/v1/address?key="+MapQuestKey, data);
			JSONObject latLng = result.getJSONArray("results").getJSONObject(0).getJSONArray("locations").getJSONObject(0).getJSONObject("latLng");
			slat = latLng.getDouble("lat");
			slng = latLng.getDouble("lng");
	    	data.clear();
	    	data.put("location", end);
	    	result = obj.sendPost("http://open.mapquestapi.com/geocoding/v1/address?key="+MapQuestKey, data);
	    	latLng = result.getJSONArray("results").getJSONObject(0).getJSONArray("locations").getJSONObject(0).getJSONObject("latLng");
	    	elat = latLng.getDouble("lat");
			elng = latLng.getDouble("lng");
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	response.put("slat", slat);
    	response.put("slng", slng);
    	response.put("elat", elat);
    	response.put("elng", elng);
    	response.put("distance", DistanceUtil.distance(slat, slng, elat, elng, 'K')*1000);
    	return response;
	}

	public static JSONObject getLocation(JSONObject response, String address) {
		Boolean hasError = false;
		try {
			CollectionReference locations = firestoreProvider.getCollection("Location");
			Query query = locations.whereEqualTo("address", address);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				response.put("lat", doc.getString("lat"));
				response.put("lng", doc.getString("lng"));
			}
		} catch (InterruptedException | ExecutionException e) {
			hasError = true;
			e.printStackTrace();
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject updateLocation(JSONObject response, String address, String lat, String lng) {
		Boolean hasError = false;
		try {
			DocumentReference docRef = firestoreProvider.getRef("Location", address);
			Map<String, Object> data = new HashMap<>();
			data.put("address", address);
			data.put("lat", lat);
			data.put("lng", lng);
			firestoreProvider.setData(data, docRef);
		} catch (Exception e) {
			hasError = true;
			e.printStackTrace();
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject setShipmentRoute(JSONObject response, String shipmentId, String seller, String buyer, String sellerLocation, String buyerLocation,
			String waypoint1, String waypoint2, String waypoint3) {
		String[] waypoints = {waypoint1, waypoint2, waypoint3};
		firestoreProvider.setShipmentRoute(shipmentId, seller, buyer, sellerLocation, buyerLocation, waypoints);
		return response;
	}
	
}
