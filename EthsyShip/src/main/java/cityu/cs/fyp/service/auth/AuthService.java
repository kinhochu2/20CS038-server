package cityu.cs.fyp.service.auth;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;

import cityu.cs.fyp.firebase.FireAuthProvider;
import cityu.cs.fyp.firebase.FirestoreProvider;
import cityu.cs.fyp.java_ethereum.Web3Provider;

public class AuthService {

	private static Web3Provider web3Provider = Web3Provider.getInstance();
	private static FireAuthProvider fireAuthProvider = FireAuthProvider.getInstance();
	private static FirestoreProvider firestoreProvider = FirestoreProvider.getInstance();
	
	public static JSONObject signIn(JSONObject response, String email, String password, String location) {
		String uid = "", address = "";
		try {
			uid = fireAuthProvider.getUserByEmail(email);
			address = firestoreProvider.getAddress(email);
			location = firestoreProvider.getLocation(email);
			System.out.println("User has been found, uid: "+uid);
		} catch (FirebaseAuthException e) {
			AuthErrorCode errCode = e.getAuthErrorCode();
			if(errCode.equals(AuthErrorCode.USER_NOT_FOUND)) { //No record of the user
				System.out.println("getUserByEmail, AuthErrorCode.USER_NOT_FOUND");
				uid = createUser(email, password);
				JSONObject newWallet = web3Provider.createWallet(password);
				address = newWallet.getString("address");
				String fileName = newWallet.getString("fileName");
				firestoreProvider.logNewAccount(email, uid, address, location, fileName);
			}else {
				System.out.println("getUserByEmail: "+errCode.toString());
			}
		}
		
		response.put("email", email);
		response.put("uid", uid);
		response.put("address", address);
		response.put("location", location);
		return response;
	}
	
	private static String createUser(String email, String password) {
		String pid = "";
		try {
			pid = fireAuthProvider.createUserWithEmail(email, password);
			System.out.println("createUser, pid: "+pid);
		} catch (FirebaseAuthException e) {
			AuthErrorCode errCode = e.getAuthErrorCode();
			System.out.println("createUser: "+errCode.toString());
		}
		return pid;
	}
	
	public static void deleteUser(String uid, String email) {
		try {
			fireAuthProvider.deleteUser(uid);
			firestoreProvider.deleteAccount(email);
		} catch (FirebaseAuthException e) {
			AuthErrorCode errCode = e.getAuthErrorCode();
			System.out.println("deleteUser: "+errCode.toString());
		}
	}
	
	public static JSONObject getBalance(JSONObject response, String address) {
		response.put("address", address);
		if(address=="undefined") {
			response.put("balance", "0");
		}else
			response.put("balance", web3Provider.getBalance(address));
		return response;
	}
	
	public static JSONObject getAccounts(JSONObject response) {
		List<String> accounts = null;
		try {
			accounts = web3Provider.getAccounts();
		} catch (IOException e) {
			e.printStackTrace();
		}
		response.put("accounts", accounts.toString());
		return response;
	}
	
}
