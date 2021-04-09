package cityu.cs.fyp.service.auth;

import java.util.Map;

import org.json.JSONObject;


public class AuthHandler {

	public static String handleGET(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "getaccounts":
				response = AuthService.getAccounts(response);
				break;
		}
		return response.toString();
	}
	
	public static String handlePOST(String func, Map<String, String> map) {
		JSONObject response = new JSONObject();
		switch(func) {
			case "signin":
				response = AuthService.signIn(response, map.get("email"), map.get("password"), map.get("location"));
				break;
			case "deleteacc":
				AuthService.deleteUser(map.get("uid"), map.get("email"));
				break;
			case "getbalance":
				response = AuthService.getBalance(response, map.get("address"));
				break;
		}
		return response.toString();
	}
}
