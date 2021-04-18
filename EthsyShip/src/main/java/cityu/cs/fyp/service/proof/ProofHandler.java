package cityu.cs.fyp.service.proof;

import java.util.Map;

import org.json.JSONObject;

import cityu.cs.fyp.service.proof.ProofService;

public class ProofHandler {

	public static String handleGET(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "getrequests":
				response = ProofService.getRequestsByShipmentId(response, map.get("shipmentId"));
				break;
			case "getresponses":
				response = ProofService.getResponseByRequestId(response, map.get("requestId"));
				break;
			case "getrevertreason":
				response= ProofService.getRevertReason(response, map.get("hash"));
				break;
		}
		return response.toString();
	}
	
	public static String handlePOST(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "createrequest":
				response = ProofService.createRequest(response, map.get("shipmentId"), map.get("proverLat")
						, map.get("proverLng"), map.get("proverAddr"), map.get("password"), map);
				break;
			case "addresponse":
				response = ProofService.addResponse(response, map.get("requestId"), map.get("shipmentId")
						, map.get("witnessLat"), map.get("witnessLng"), map.get("witnessAddr"), map.get("timestamp")
						, map.get("password"), map);
				break;
			case "loadrequest":
				response = ProofService.loadRequest(response, map.get("requestId"));
				break;
			case "submitblock":
				response = ProofService.submitBlock(response, map.get("requestId"), map.get("preHx"), map.get("address"), map);
				break;
			case "verifyblocks":
				response = ProofService.verifyBlocks(response, map.get("blockHx"), map.get("totalCount"));
				break;
		}
		return response.toString();
	}
}
