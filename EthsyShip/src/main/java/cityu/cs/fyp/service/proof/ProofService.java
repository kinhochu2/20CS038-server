package cityu.cs.fyp.service.proof;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;

import cityu.cs.fyp.java_ethereum.ProofOfLocationController;
import cityu.cs.fyp.java_ethereum.Web3Provider;
import cityu.cs.fyp.util.DistanceUtil;
import cityu.cs.fyp.util.SignUtil;

public class ProofService {

	private static ProofOfLocationController contractCtrl = ProofOfLocationController.getInstance();
	
	public static JSONObject createRequest(JSONObject response, String shipmentId, String proverLat
			, String proverLng, String proverAddr, String password, Map<String, String> map) {
		String blockHx = "";
		Boolean hasError = false;
		String timestamp = Calendar.getInstance().getTime().toString();
		String preHx = "0";
		try {
			int signedHx = Web3Provider.getInstance().signMessage(proverAddr, password, map);
			System.out.println("signedHx: "+signedHx);
			System.out.println("shipmentId: "+shipmentId);
			System.out.println("proverLat: "+proverLat);
			System.out.println("proverLng: "+proverLng);
			System.out.println("proverAddr: "+proverAddr);
			System.out.println("timestamp: "+timestamp);
			blockHx = contractCtrl.createRequest(shipmentId, proverLat, proverLng, proverAddr, preHx, timestamp, signedHx);
			if(verifyRequest(String.valueOf(contractCtrl.getId()))) {
				response.put("blockHx", blockHx);
				response.put("requestId", contractCtrl.getId());
			}else {
				hasError = false;
				System.out.println("failed in verifyRequest");
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject addResponse(JSONObject response, String requestId, String shipmentId
			, String witnessLat, String witnessLng, String witnessAddr, String timestamp, String password
			, Map<String, String> map) {
		Boolean hasError = false;
		timestamp = Calendar.getInstance().getTime().toString();
		try {
			int signedHx = Web3Provider.getInstance().signMessage(witnessAddr, password, map);
			contractCtrl.addResponse(requestId, shipmentId, witnessLat, witnessLng, witnessAddr, timestamp, signedHx);
			if(!verifyResponse(requestId, witnessAddr)) {
				hasError = false;
				System.out.println("failed in verifyResponse");
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject loadRequest(JSONObject response, String requestId) {
		Tuple5<BigInteger, String, String, String, String> result = null;
		Boolean hasError = false;
		try {
			result = contractCtrl.loadRequestDetails(requestId);
			response.put("shipmentId", result.component1().toString());
			response.put("proverLat", result.component2());
			response.put("proverLng", result.component3());
			response.put("proverAddr", result.component4());
			response.put("timestamp", result.component5());
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject submitBlock(JSONObject response, String requestId, String preHx) {
		Boolean hasError = false;
		try {
			int hxCode = contractCtrl.submitBlock(requestId, preHx);
			response.put("hxCode", hxCode);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	private static boolean verifyRequest(String requestId) {
		boolean result = true;
		try {
			JSONObject details = new JSONObject();
			details = ProofService.loadRequest(details, requestId);
			Tuple2<String, BigInteger> requestHx = contractCtrl.getRequestSignedHx(requestId);
			if(requestHx.component1() == null || requestHx.component2() == null) {
				result = false;
			}
			double proverLat = details.getDouble("proverLat");
			double proverLng = details.getDouble("proverLng");
			if(DistanceUtil.distance(proverLat, proverLng, 22.302711, 114.177216, 'K') > 500.0) {
				result = false;
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	private static boolean verifyResponse(String requestId, String witnessAddr) {
		boolean result = true;
		try {
			Tuple4<BigInteger, String, String, String> details = contractCtrl.loadResponseDetails(requestId, witnessAddr);
			Tuple2<String, BigInteger> responseHx = contractCtrl.getResponseSignedHx(requestId, witnessAddr);
			if(responseHx.component1() == null || responseHx.component2() == null) {
				result = false;
			}
			if(responseHx.component1() != witnessAddr)
				result = false;
			double witnessLat = Double.valueOf(details.component2());
			double witnessLng = Double.valueOf(details.component3());
			if(DistanceUtil.distance(witnessLat, witnessLng, 22.302711, 114.177216, 'K') > 500.0) {
				result = false;
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	public static JSONObject verifyBlocks(JSONObject response, String blockHx, String totalCount) {
		Boolean hasError = false;
		try {
			if(!contractCtrl.verifyBlocks(blockHx, Integer.valueOf(totalCount))) {
				hasError = true;
				System.out.println("failed in verifyBlocks");
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject getRequestsByShipmentId(JSONObject response, String shipmentId) {
		Boolean hasError = false;
		Tuple4<List<BigInteger>, List<String>, List<String>, List<String>> result = null;
		JSONObject obj = new JSONObject();
		int count = 0;
		try {
			result = contractCtrl.getRequestsByShipmentId(shipmentId);
			count = result.component1().size();
			JSONArray requestIds = new JSONArray();
			JSONArray sellerLats = new JSONArray();
			JSONArray sellerLngs = new JSONArray();
			JSONArray timestamps = new JSONArray();
			
			for(int i=0;i<count;i++) {
				requestIds.put(result.component1().get(i).toString());
				sellerLats.put(result.component2().get(i).toString());
				sellerLngs.put(result.component3().get(i).toString());
				timestamps.put(result.component4().get(i).toString());
			}
			obj.put("requestIds", requestIds);
			obj.put("sellerLats", sellerLats);
			obj.put("sellerLngs", sellerLngs);
			obj.put("timestamps", timestamps);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("count", count);
		response.put("result", obj);
		response.put("hasError", hasError);
		return response;
	}
	
	public static JSONObject getResponseByRequestId(JSONObject response, String requestId) {
		Boolean hasError = false;
		Tuple4<List<String>, List<String>, List<String>, List<String>> result = null;
		JSONObject obj = new JSONObject();
		int count = 0;
		try {
			result = contractCtrl.getResponseByRequestId(requestId);
			count = result.component1().size();
			JSONArray witnessAddrs = new JSONArray();
			JSONArray witnessLats = new JSONArray();
			JSONArray witnessLngs = new JSONArray();
			JSONArray timestamps = new JSONArray();
			
			for(int i=0;i<count;i++) {
				witnessAddrs.put(result.component1().get(i).toString());
				witnessLats.put(result.component2().get(i).toString());
				witnessLngs.put(result.component3().get(i).toString());
				timestamps.put(result.component4().get(i).toString());
			}
			obj.put("witnessAddrs", witnessAddrs);
			obj.put("witnessLats", witnessLats);
			obj.put("sellerLngs", witnessLngs);
			obj.put("timestamps", timestamps);
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
		}
		response.put("count", count);
		response.put("result", obj);
		response.put("hasError", hasError);
		return response;
	}
}
