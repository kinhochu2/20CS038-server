package cityu.cs.fyp.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
import java.io.FileReader;
import java.util.Iterator;
 
/**
 * @author Crunchify.com
 * How to Read JSON Object From File in Java?
 */
 
public class JSONReader {
	
	@SuppressWarnings("unchecked")
	public static void readKeys() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:/Users/Kinson/Documents/FYP/blockchain/keys.json"));
 
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray privateKeys = (JSONArray) jsonObject.get("private_keys");
 
			Iterator<JSONObject> iterator = privateKeys.iterator();
			while (iterator.hasNext()) {
				System.out.println(iterator.next());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
