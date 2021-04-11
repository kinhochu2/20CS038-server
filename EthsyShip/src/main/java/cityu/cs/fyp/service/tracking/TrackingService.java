package cityu.cs.fyp.service.tracking;

import java.io.IOException;
import java.math.BigInteger;
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
import org.web3j.tuples.generated.Tuple5;

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
		double distToStart;
		
		public Location(String name, double lat, double lng, double dist, double distToStart) {
			this.name = name;
			this.lat = lat;
			this.lng = lng;
			this.dist = dist;
			this.distToStart = distToStart;
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
		Tuple5<String, String, String, BigInteger, String> result = null;
		Boolean hasError = false;
		JSONObject obj = new JSONObject();
		try {
			result = contractCtrl.getShipmentDetails(id);
			response.put("sellerLocation", result.component1());
			response.put("buyerLocation", result.component2());
			response.put("distance", result.component3());
			response.put("time", result.component4());
			response.put("eta", result.component5());
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
			, String eName) {
		String gasUsed = "";
		Boolean hasError = false;
		try {
			gasUsed = contractCtrl.createShipment(itemId, sName, eName);
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
			contractCtrl.createShipment(itemId, sellerLocation, buyerLocation);
			id = contractCtrl.getShipmentId();
			System.out.println("createShipement, shipmentId: "+id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public static JSONObject addWaypointToRoute(JSONObject response, String id, String name1, String name2, String name3, String count, String start, String end, String mph) {
		Boolean hasError = false;
		double distance = 0.0;
		double time = 0.0;
		String eta = "";
		List<String> list = new ArrayList<>();
		try {
			if(Integer.valueOf(count)==3) {
				contractCtrl.addWaypointToRoute(id, name1, false);
				contractCtrl.addWaypointToRoute(id, name2, false);
				contractCtrl.addWaypointToRoute(id, name3, true);
				list.add(start);list.add(name1); list.add(name2); list.add(name3);list.add(end);
			}else if(Integer.valueOf(count)==2) {
				contractCtrl.addWaypointToRoute(id, name1, false);
				contractCtrl.addWaypointToRoute(id, name2, true);
				list.add(start);list.add(name1); list.add(name2);list.add(end);
			}else if(Integer.valueOf(count)==1) {
				contractCtrl.addWaypointToRoute(id, name1, true);
				list.add(start);list.add(name1);list.add(end);
			}else if(Integer.valueOf(count)==0) {
				contractCtrl.setWaypointToRouteFinished(id);
				list.add(start);list.add(end);
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		JSONObject slatLng = geocoding(start);
		double slat = slatLng.getDouble("lat");
		double slng = slatLng.getDouble("lng");
		for(int i=1;i<Integer.valueOf(count)+2;i++) {
			JSONObject elatLng = geocoding(list.get(i));
			double elat = elatLng.getDouble("lat");
			double elng = elatLng.getDouble("lng");
			distance = distance += DistanceUtil.distance(slat, slng, elat, elng, 'K');
			slat = elat; slng = elng;
		}
		time = DistanceUtil.time(distance, Double.valueOf(mph), 'M');
		eta = DistanceUtil.addTime(time).toString();
		
		try {
			contractCtrl.setTimeAndDistance(id, time, String.valueOf(distance), eta);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		response.put("distance", distance);
		response.put("time", time);
		response.put("eta", eta);
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
	
	public static JSONObject geocoding(String location) {
		HttpRequestClient obj = new HttpRequestClient();
    	Map<Object, Object> data = new HashMap<>();
    	data.put("location", location);
    	JSONObject result = new JSONObject();
		try {
			result = obj.sendPost("http://open.mapquestapi.com/geocoding/v1/address?key="+MapQuestKey, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject latLng = result.getJSONArray("results").getJSONObject(0).getJSONArray("locations")
				.getJSONObject(0).getJSONObject("latLng");
		return latLng;
	}
	
	public static JSONObject findWaypoints(JSONObject response, String sellerLocation, String buyerLocation) {
		double slat = 0.0, slng = 0.0, elat =0.0, elng = 0.0;
    	JSONObject sLatLng = geocoding(sellerLocation);
    	JSONObject eLatLng = geocoding(buyerLocation);
    	slat = sLatLng.getDouble("lat");
		slng = sLatLng.getDouble("lng");
		elat = eLatLng.getDouble("lat");
		elng = eLatLng.getDouble("lng");
		double refDist = DistanceUtil.distance(slat, slng, elat, elng, 'K'); // distance from starting point to destination
		System.out.println("refDist: "+refDist);
		List<QueryDocumentSnapshot> list = firestoreProvider.getData("Geofence"); // retrieve document reference from db
		List<Location> topWaypoints = new ArrayList<Location>();
		
		Comparator<Location> comp = (Location a, Location b) -> { // comparator for sorting waypoints
		    return b.dist > a.dist ? -1 : (b.dist < a.dist) ? 1 : 0;
		};
		Comparator<Location> compOrders = (Location a, Location b) -> { // comparator for sorting waypoint according to the distance to starting point
		    return b.distToStart < a.distToStart ? -1 : (b.distToStart > a.distToStart) ? 1 : 0;
		};
		for(QueryDocumentSnapshot doc: list) {
			double dist1 = DistanceUtil.distance(doc.getDouble("lat"), doc.getDouble("lng"), 
					Double.valueOf(slat), Double.valueOf(slng), 'K'); // distance from waypoint to starting point
			double dist2 = DistanceUtil.distance(doc.getDouble("lat"), doc.getDouble("lng"), 
					Double.valueOf(elat), Double.valueOf(elng), 'K'); // distance from waypoint to destination
			if(((dist1+dist2) - refDist) <= 2.0 && (dist1+dist2) - refDist > 0.0) {
				topWaypoints.add(new Location(doc.getId(), doc.getDouble("lat"), doc.getDouble("lng"), dist1+dist2, dist1));
				if(topWaypoints.size() == 4) {
					topWaypoints.sort(comp); // sort the array list
					topWaypoints.remove(topWaypoints.size()-1); // remove the last waypoint from the list
				}
			}
		}
		topWaypoints.sort(compOrders);
		for(Location l: topWaypoints) {
			JSONObject obj = new JSONObject();
			obj.put("name", l.name);
			obj.put("lat", l.lat);
			obj.put("lng", l.lng);
			response.append("waypoints", obj);
		}
		response.put("count", topWaypoints.size());
		System.out.println("findWaypoints result: "+response.toString());
		return response;
	}
	
	public static JSONObject getDistance(JSONObject response, String start, String end) {
		double slat = 0.0, slng = 0.0, elat =0.0, elng = 0.0;
    	JSONObject sLatLng = geocoding(start);
    	JSONObject eLatLng = geocoding(end);
    	slat = sLatLng.getDouble("lat");
		slng = sLatLng.getDouble("lng");
		elat = eLatLng.getDouble("lat");
		elng = eLatLng.getDouble("lng");
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
