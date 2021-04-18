package cityu.cs.fyp.java_ethereum;

import java.math.BigInteger;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.utils.Numeric;

import cityu.cs.fyp.firebase.FirestoreProvider;

public class MarketContractController {
	
	private MarketContract contract = null;
	private BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
	private BigInteger GAS_LIMIT = BigInteger.valueOf(6721975L);
	private Web3Provider web3Provider;
	private static MarketContractController instance = null;
	private static int itemCount = 0;
	
	private MarketContractController() {
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
	
	public static MarketContractController getInstance() {
		if(instance == null) {
			instance = new MarketContractController();
		}
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	private String deployContract(Web3j web3j, Credentials credentials) throws Exception{
	    return MarketContract.deploy(web3j, credentials, GAS_PRICE, GAS_LIMIT).send().getContractAddress();
	}
	
	@SuppressWarnings("deprecation")
	private MarketContract loadContract(String address, Web3j web3, Credentials credentials){
	    return MarketContract.load(address, web3, credentials, GAS_PRICE, GAS_LIMIT);
	}
	
	public String addItem(String name, String seller, int price, String location) throws Exception {
		System.out.println("Adding item, seller address: "+seller);
		TransactionReceipt receipt = contract.addItemToMarket(name, seller, location, BigInteger.valueOf(price)).send();
		if(receipt != null) {
			System.out.println("addItem result: "+receipt.toString());
			itemCount++;
			return receipt.getGasUsed().toString(10);
		}else {
			System.out.println("addItem failed, receipt is null");
			return "";
		}
	}
	
	public Tuple5<List<BigInteger>, List<String>, List<String>, List<String>, List<BigInteger>> getItems() throws Exception {
		Tuple5<List<BigInteger>, List<String>, List<String>, List<String>, List<BigInteger>> result = contract.getItemsFromMarket().send();
		if(result != null) {
			System.out.println("getItems result: "+result.toString());
		}else {
			System.out.println("getItems failed, receipt is null");
		}
		return result;
	}
	
	public String buyItem(String buyer, String buyerLocation, int id, int amount, int shipmentId) throws Exception {
		System.out.println("Buying item, buyer address: "+buyer);
		TransactionReceipt receipt = contract.buyItem(buyer, buyerLocation, BigInteger.valueOf(id), BigInteger.valueOf(amount), BigInteger.valueOf(shipmentId)).send();
		if(receipt != null) {
			System.out.println("buyItem result: "+receipt.toString());
			return receipt.getGasUsed().toString(10);
		}else {
			System.out.println("buyItem failed, receipt is null");
			return "";
		}
	}
	
	public Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> getItemsFromBuyer(String buyer) throws Exception {
		Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> result = contract.getUnpaidItemsFromBuyer(buyer).send();
		if(result != null) {
			System.out.println("getItemsFromBuyer result: "+result.toString());
		}else {
			System.out.println("getItemsFromBuyer failed, receipt is null");
		}
		return result;
	}
	
	public Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> getItemsFromSeller(String seller) throws Exception {
		Tuple5<List<BigInteger>, List<BigInteger>, List<String>, List<String>, List<BigInteger>> result = contract.getUnpaidItemsFromSeller(seller).send();
		if(result != null) {
			System.out.println("getItemsFromSeller result: "+result.toString());
		}else {
			System.out.println("getItemsFromSeller failed, receipt is null");
		}
		return result;
	}
	
	public void finishShipping(String seller, String buyer, String shipmentId, String value, String password) throws Exception {
		TransactionReceipt receipt = contract.finishShipping(buyer, seller, Numeric.decodeQuantity(shipmentId)).send();
		String fileName = FirestoreProvider.getInstance().getFileName(seller);
		this.web3Provider.sendTransaction(seller, buyer, value, password, fileName);
		if(receipt != null) {
			System.out.println("finishShipping result: "+receipt.toString());
		}else {
			System.out.println("finishShipping failed, receipt is null");
		}
	}
	
	
	public int getItemPrice(String id) throws Exception {
		BigInteger result = contract.getItemPrice(BigInteger.valueOf(Integer.valueOf(id))).send();
		System.out.println("getItemPrice result: "+result.toString());
		return result.intValue();
	}
	
	public String getItemName(String id) throws Exception {
		String result = contract.getItemName(BigInteger.valueOf(Integer.valueOf(id))).send();
		System.out.println("getItemName result: "+result.toString());
		return result;
	}
	
	public String getItemSellerLocation(String id) throws Exception {
		String result = contract.getItemSellerLocation(BigInteger.valueOf(Integer.valueOf(id))).send();
		System.out.println("getItemName result: "+result.toString());
		return result;
	}
	
	public int getItemCount() {
		return itemCount;
	}
	
	
}
