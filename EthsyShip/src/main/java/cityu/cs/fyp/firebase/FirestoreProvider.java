package cityu.cs.fyp.firebase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;


public class FirestoreProvider {

	private Firestore db;
	private static FirestoreProvider provider = null;
	
	private FirestoreProvider() {
		if(FirebaseAppInitializer.AppHasInitialized())
			db = FirestoreClient.getFirestore(FirebaseAppInitializer.getFirebaseApp());
	}
	
	public static FirestoreProvider getInstance() {
		if(provider == null) {
			provider = new FirestoreProvider();
		}
		return provider;
	}
	
	public DocumentReference getRef(String collectionName, String documentName) {
		return db.collection(collectionName).document(documentName);
	}
	
	public CollectionReference getCollection(String collectionName) {
		return db.collection(collectionName);
	}
	
	public void setData(Map<String, Object> data, DocumentReference docRef) {
		try {
			ApiFuture<WriteResult> result = docRef.set(data);
			System.out.println("Update time : " + result.get().getUpdateTime());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public List<QueryDocumentSnapshot> getData(String collectionName) {
		List<QueryDocumentSnapshot> documents = null;
		try {
			ApiFuture<QuerySnapshot> query = db.collection(collectionName).get();
			QuerySnapshot querySnapshot;
			querySnapshot = query.get();
			documents = querySnapshot.getDocuments();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return documents;
	}
	
	public String getAddress(String email) {
		String address = null;
		try {
			CollectionReference users = db.collection("Users");
			Query query = users.whereEqualTo("email", email);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				System.out.println("doc.getId():"+doc.getId());
				address = doc.getString("address");
			}
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return address;
	}
	
	public String getLocation(String email) {
		String address = null;
		try {
			CollectionReference users = db.collection("Users");
			Query query = users.whereEqualTo("email", email);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				System.out.println("doc.getId():"+doc.getId());
				address = doc.getString("location");
			}
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return address;
	}
	
	public String getFileName(String address) {
		String fileName = null;
		try {
			CollectionReference users = db.collection("Users");
			Query query = users.whereEqualTo("address", address);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				System.out.println("doc.getId():"+doc.getId());
				fileName = doc.getString("fileName");
			}
			System.out.println("fileName: "+fileName);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	public String getBalance(String address) {
		String balance = null;
		try {
			CollectionReference cities = db.collection("Wallets");
			Query query = cities.whereEqualTo("address", address);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				balance = doc.getString("balance");
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return balance;
	}
	
	public void logNewAccount(String email, String uid, String address, String location, String fileName) {
		DocumentReference docRef = getRef("Users", email);
		Map<String, Object> data = new HashMap<>();
		data.put("email", email);
		data.put("uid", uid);
		data.put("address", address);
		data.put("location", location);
		data.put("fileName", fileName);
		setData(data, docRef);
		System.out.println("Record in firestore is logged");
	}
	
	public void deleteAccount(String email) {
		DocumentReference docRef = getRef("Users", email);
		docRef.delete();
		System.out.println("Record in firestore is deleted");
	}
	
	public void setShipmentRoute(String shipmentId, String seller, String buyer, String sellerLocation, String buyerLocation, String[] waypoints) {
		DocumentReference docRef = getRef("Shipments", shipmentId);
		Map<String, Object> data = new HashMap<>();
		data.put("shipmentId", shipmentId);
		data.put("buyer", buyer);
		data.put("seller", seller);
		data.put("sellerLocation", sellerLocation);
		data.put("buyerLocation", buyerLocation);
		data.put("waypoints", waypoints);
		setData(data, docRef);
		System.out.println("Record in firestore is logged");
	}
	
	public QueryDocumentSnapshot getShipmentDetails(String shipmentId) {
		QueryDocumentSnapshot d = null;
		try {
			CollectionReference users = db.collection("Shipments");
			Query query = users.whereEqualTo("shipmentId", shipmentId);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			for(QueryDocumentSnapshot doc: querySnapshot.get().getDocuments()) {
				d = doc;
				break;
			}
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return d;
	}
	
}
