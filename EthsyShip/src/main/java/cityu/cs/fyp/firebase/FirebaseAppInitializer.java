package cityu.cs.fyp.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseAppInitializer {
	
	private static InputStream serviceAccount;
	private static GoogleCredentials credentials;
	private static boolean hasInitialized = false;
	private static FirebaseApp firebaseApp;
	
	private static void init() throws IOException {
		serviceAccount = new FileInputStream("C:/Users/Kinson/Documents/GitHub/ethsyship-firebase-adminsdk-7cvgv-03e26630e0.json");
		credentials = GoogleCredentials.fromStream(serviceAccount);
		@SuppressWarnings("deprecation")
		FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredentials(credentials)
			    .build();
		FirebaseApp.initializeApp(options);
		firebaseApp = FirebaseApp.getInstance();
		System.out.println("FirebaseApp has initialized.");
	}
	
	public static boolean AppHasInitialized() {
		if(!hasInitialized) {
			try {
				init();
				hasInitialized = true;
			} catch (IOException e) {
				System.out.println("Error during initalizing firebase app.");
				e.printStackTrace();
				hasInitialized = false;
			}
		}
		return hasInitialized;
	}
	
	public static FirebaseApp getFirebaseApp() {
		return firebaseApp;
	}
}
