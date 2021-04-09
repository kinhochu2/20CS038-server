package cityu.cs.fyp.firebase;

import com.google.firebase.auth.*;
import com.google.firebase.auth.UserRecord.CreateRequest;

public class FireAuthProvider {
	private FirebaseAuth firebaseAuth;
	private static FireAuthProvider provider = null;
	
	private FireAuthProvider() {
		if(FirebaseAppInitializer.AppHasInitialized())
			firebaseAuth = FirebaseAuth.getInstance(FirebaseAppInitializer.getFirebaseApp());
	}
	
	public static FireAuthProvider getInstance() {
		if(provider == null) {
			provider = new FireAuthProvider();
		}
		return provider;
	}

	public String getUserByEmail(String email) throws FirebaseAuthException {
		UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
		System.out.println("Successfully fetched user data: " + userRecord.getEmail());
		return userRecord.getUid();
	}
	
	public UserRecord getUserById(String uid) throws FirebaseAuthException  {
		UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
		System.out.println("Successfully fetched user data: " + userRecord.getEmail());
		return userRecord;
	}
	
	public String createUserWithEmail(String email, String password) throws FirebaseAuthException {
		CreateRequest request = new CreateRequest()
			    .setEmail(email)
			    .setEmailVerified(false)
			    .setPassword(password);
		UserRecord userRecord = firebaseAuth.createUser(request);
		System.out.println("Successfully created new user: " + userRecord.getUid());
		return userRecord.getUid();
	}
	
	public void deleteUser(String uid) throws FirebaseAuthException {
		firebaseAuth.deleteUser(uid);
		System.out.println("Successfully deleted user.");
	}
	
}
