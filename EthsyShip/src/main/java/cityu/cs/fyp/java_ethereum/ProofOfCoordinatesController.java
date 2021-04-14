package cityu.cs.fyp.java_ethereum;

import java.math.BigInteger;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.utils.Numeric;

public class ProofOfCoordinatesController {
	
	private ProofOfCoordinates contract = null;
	private BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
	private BigInteger GAS_LIMIT = BigInteger.valueOf(6721975L);
	private Web3Provider web3Provider;
	private static ProofOfCoordinatesController instance = null;
	private int requestIdCount = 0;
	
	private ProofOfCoordinatesController() {
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
	
	public static ProofOfCoordinatesController getInstance() {
		if(instance == null) {
			instance = new ProofOfCoordinatesController();
		}
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	private String deployContract(Web3j web3j, Credentials credentials) throws Exception{
	    return LocationContract.deploy(web3j, credentials, GAS_PRICE, GAS_LIMIT).send().getContractAddress();
	}
	
	@SuppressWarnings("deprecation")
	private ProofOfCoordinates loadContract(String address, Web3j web3, Credentials credentials){
	    return ProofOfCoordinates.load(address, web3, credentials, GAS_PRICE, GAS_LIMIT);
	}
	
	public String createRequest(String shipmentId, String proverLat, String proverLng, String proverAddr
			, String preHx, String timestamp, int signedHx) throws Exception {
		TransactionReceipt receipt = contract.createRequest(shipmentId, proverLat, proverLng
				, proverAddr, preHx, timestamp).send();
		this.setSignedHx(requestIdCount, signedHx);
		if(receipt != null) {
			System.out.println("createRequest result: "+receipt.toString());
			return receipt.getBlockHash().toString();
		}else {
			System.out.println("createRequest failed, receipt is null");
			return "";
		}
	}
	
	public void setSignedHx(int requestId, int signedHx) throws Exception{
		TransactionReceipt receipt = contract.setRequestSignedHx(BigInteger.valueOf(requestId), BigInteger.valueOf(signedHx)).send();
		if(receipt != null) {
			System.out.println("setSignedHx result: "+receipt.toString());
		}else {
			System.out.println("setSignedHx failed, receipt is null");
		}
	}
	
	public String addResponse(String requestId, String shipmentId, String witnessLat, String witnessLng
			, String witnessAddr, String timestamp, int signedHx) throws Exception {
		TransactionReceipt receipt = contract.addResponse(BigInteger.valueOf(Integer.valueOf(requestId))
				, shipmentId, witnessLat, witnessLng, witnessAddr
				, timestamp, BigInteger.valueOf(signedHx)).send();
		if(receipt != null) {
			System.out.println("createRequest result: "+receipt.toString());
			return receipt.getBlockHash().toString();
		}else {
			System.out.println("createRequest failed, receipt is null");
			return "";
		}
	}
	
	public Tuple4<String, String, String, String> loadResponseDetails(String requestId, String address) throws Exception {
		Tuple4<String, String, String, String> result = contract.loadResponseDetails(Numeric.decodeQuantity(requestId), address).send();
		if(result != null) {
			System.out.println("loadResponseDetails result: "+result.toString());
			return result;
		}else {
			System.out.println("loadResponseDetails failed, result is null");
			return null;
		}
	}
	
	public Tuple5<String, String, String, String, String> loadRequestDetails(String requestId) throws Exception {
		Tuple5<String, String, String, String, String> result = contract.loadRequestDetails(Numeric.decodeQuantity(requestId)).send();
		if(result != null) {
			System.out.println("loadRequestDetails result: "+result.toString());
			return result;
		}else {
			System.out.println("loadRequestDetails failed, result is null");
			return null;
		}
	}
	
	public int submitBlock(String requestId, String preBlockHx) throws Exception {
		TransactionReceipt receipt = contract.submitBlock(Numeric.decodeQuantity(requestId), Numeric.decodeQuantity(preBlockHx)).send();
		if(receipt != null) {
			System.out.println("submitBlock result: "+receipt.toString());
			return receipt.hashCode();
		}else {
			System.out.println("submitBlock failed, receipt is null");
			return -1;
		}
	}
	
	public Tuple2<String, BigInteger> getRequestSignedHx(String requestId) throws Exception {
		Tuple2<String, BigInteger> result = contract.getRequestSignedHx(Numeric.decodeQuantity(requestId)).send();
		if(result != null) {
			System.out.println("getRequestSignedHx result: "+result.toString());
			return result;
		}else {
			System.out.println("getRequestSignedHx failed, result is null");
			return null;
		}
	}
	
	public Tuple2<String, BigInteger> getResponseSignedHx(String requestId, String witnessAddr) throws Exception {
		Tuple2<String, BigInteger> result = contract.getResponseSignedHx(Numeric.decodeQuantity(requestId), witnessAddr).send();
		if(result != null) {
			System.out.println("getResponseSignedHx result: "+result.toString());
			return result;
		}else {
			System.out.println("getResponseSignedHx failed, result is null");
			return null;
		}
	}
	
	public boolean verifyBlocks(String blockHx, int totalCount) throws Exception {
		Tuple2<Boolean, BigInteger> result = contract.verifyBlocks(Numeric.decodeQuantity(blockHx), BigInteger.valueOf(totalCount)).send();
		if(result != null) {
			System.out.println("verifyBlocks result: "+result.toString());
			return result.component1();
		}else {
			System.out.println("verifyBlocks failed, result is null");
			return false;
		}
	}
	
	public Tuple4<List<BigInteger>, List<String>, List<String>, List<String>> getRequestsByShipmentId(String shipmentId) throws Exception {
		Tuple4<List<BigInteger>, List<String>, List<String>, List<String>> result = contract.getRequestsByShipmentId(shipmentId).send();
		if(result != null) {
			System.out.println("getRequestsByShipmentId result: "+result.toString());
			return result;
		}else {
			System.out.println("getRequestsByShipmentId failed, result is null");
			return null;
		}
	}
	
	public Tuple4<List<String>, List<String>, List<String>, List<String>> getResponseByRequestId(String requestId) throws Exception {
		Tuple4<List<String>, List<String>, List<String>, List<String>> result = contract.getResponseByRequestId(BigInteger.valueOf(Integer.valueOf(requestId))).send();
		if(result != null) {
			System.out.println("getResponseByRequestId result: "+result.toString());
			return result;
		}else {
			System.out.println("getResponseByRequestId failed, result is null");
			return null;
		}
	}
	
	public int getId() {
		return this.requestIdCount++;
	}
}
