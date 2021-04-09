package cityu.cs.fyp.java_ethereum;

import java.math.BigInteger;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.utils.Numeric;

public class LocationContractController {
	
	private LocationContract contract = null;
	private BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
	private BigInteger GAS_LIMIT = BigInteger.valueOf(6721975L);
	private Web3Provider web3Provider;
	private static LocationContractController instance = null;
	private int shipmentIdCount = 0;
	
	private LocationContractController() {
		init();
	}
	
	private void init() {
		web3Provider = Web3Provider.getInstance();
		Credentials credentials = web3Provider.getCredentials();
		try {
			String contractAddress = deployContract(web3Provider.getWeb3j(), credentials);
			contract = loadContract(contractAddress, web3Provider.getWeb3j(), credentials);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static LocationContractController getInstance() {
		if(instance == null) {
			instance = new LocationContractController();
		}
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	private String deployContract(Web3j web3j, Credentials credentials) throws Exception{
	    return LocationContract.deploy(web3j, credentials, GAS_PRICE, GAS_LIMIT).send().getContractAddress();
	}
	
	@SuppressWarnings("deprecation")
	private LocationContract loadContract(String address, Web3j web3, Credentials credentials){
	    return LocationContract.load(address, web3, credentials, GAS_PRICE, GAS_LIMIT);
	}
	
	public String createShipment(String itemId, String sName, String eName, String distance, String time) throws Exception {
		BigInteger weiValue = BigInteger.valueOf(4);
		TransactionReceipt receipt = contract.createShipment(itemId, sName, eName, distance, time, weiValue).send();
		if(receipt != null) {
			System.out.println("createShipment result: "+receipt.toString());
			return receipt.getGasUsed().toString(10);
		}else {
			System.out.println("createShipment failed, receipt is null");
			return "";
		}
	}
	
	public Tuple3<String, String, String> getShipmentDetails(String id) throws Exception {
		Tuple3<String, String, String> result = contract.getShipmentDetails(Numeric.decodeQuantity(id)).send();
		if(result != null) {
			System.out.println("getShipmentDetails result: "+result.toString());
		}else {
			System.out.println("getShipmentDetails failed, receipt is null");
		}
		return result;
	}
	
	public void addWaypointToRoute(String id, String name, boolean finished) throws Exception {
		TransactionReceipt receipt = contract.addWaypointToRoute(BigInteger.valueOf(Integer.valueOf(id)), name, finished).send();
		if(receipt != null) {
			System.out.println("addWaypointToRoute result: "+receipt.toString());
		}else {
			System.out.println("addWaypointToRoute failed, receipt is null");
		}
	}
	
	public Tuple2<List<String>, Boolean> getShipmentWaypoints(String id) throws Exception {
		System.out.println("getShipmentWaypoints, id: "+id);
		Tuple2<List<String>, Boolean> result = contract.getShipmentWaypoints(Numeric.decodeQuantity(id)).send();
		if(result != null) {
			System.out.println("getShipmentWaypoints result: "+result.toString());
		}else {
			System.out.println("getShipmentWaypoints failed, receipt is null");
		}
		return result;
	}
	
	public int getShipmentId() {
		return this.shipmentIdCount++;
	}
	
	public void setWaypointToRouteFinished(String id) throws Exception {
		TransactionReceipt receipt = contract.setWaypointToRouteFinished(BigInteger.valueOf(Integer.valueOf(id))).send();
		if(receipt != null) {
			System.out.println("setWaypointToRouteFinished result: "+receipt.toString());
		}else {
			System.out.println("setWaypointToRouteFinished failed, receipt is null");
		}
	}
}
