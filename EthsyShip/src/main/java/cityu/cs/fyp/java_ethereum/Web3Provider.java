package cityu.cs.fyp.java_ethereum;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthSign;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


public class Web3Provider {

	private Web3j web3j = null;
	private Admin admin = null;
	private static Web3Provider provider = null;
	private Credentials credentials = null;

	private BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
	private BigInteger GAS_LIMIT = BigInteger.valueOf(6721975L);
	
	private Web3Provider() {
		build();
	}
	
	public static Web3Provider getInstance() {
		if(provider == null){
			provider = new Web3Provider();
		}
		return provider;
	}
	
	private void build() {
		//https://mainnet.infura.io/v3/89573bfd3d44416ba43c819873b81641 --infura
		//http://localhost:7545 --ganache
		//http://localhost:8545 --geth
		try {
			if(this.web3j == null)
				this.web3j = Web3j.build(new HttpService("http://localhost:8545"));
			if(this.admin == null)
				this.admin = Admin.build(new HttpService("http://localhost:8545"));
			
			System.out.println("Connected to Ethereum client version: " + this.web3j.web3ClientVersion().send().getWeb3ClientVersion());
			if(this.credentials == null) {
				this.credentials = WalletUtils.loadCredentials("", "C:/Users/Kinson/Documents/GitHub/Test/keystore/UTC--2021-02-18T11-10-52.251947300Z--882b8be5f3c14e95c0e32bbd59dd99b25bc2a117"); //geth
				//this.credentials = Credentials.create("1b0d11b60ddf3cc881c27da9edae1ac0c4a18b7f795f504df0074208cae2e4c9"); //ganache
				System.out.println("Credentials is created.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Web3j has initialized.");
	}
	
	public Web3j getWeb3j() {
		return this.web3j;
	}
	
	public Credentials getCredentials() {
		return this.credentials;
	}
	
	public List<String> getAccounts() throws IOException{
		EthAccounts ethAccounts = this.web3j.ethAccounts().send();
		return ethAccounts.getAccounts();
	}
	
	public String getBalance(String address) {
		JSONObject processJson = new JSONObject();
		String strTokenAmount = "";
		try {
			DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(admin.ethBlockNumber().send().getBlockNumber());
			EthGetBalance ethGetBalance = web3j.ethGetBalance(address,defaultBlockParameter).sendAsync().get();
			BigInteger wei = ethGetBalance.getBalance();
			java.math.BigDecimal tokenValue = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER);
			strTokenAmount = String.valueOf(tokenValue);
			
			processJson.put("balance", strTokenAmount);
			System.out.println("getBalance completed, balance: "+strTokenAmount);
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		return strTokenAmount;
	}
	
	
	public JSONObject createWallet(String password) {
		JSONObject processJson = new JSONObject();
		try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
            String sPrivatekeyInHex = privateKeyInDec.toString(16);
            WalletFile aWallet = Wallet.createStandard(password, ecKeyPair);
            String sAddress = aWallet.getAddress();
            processJson.put("address", "0x" + sAddress);
            processJson.put("privatekey", sPrivatekeyInHex);
            this.sendTransaction(sAddress, "100");
            System.out.println("createWallet completed, address: "+sAddress);
        } catch (CipherException | InvalidAlgorithmParameterException 
        		| NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
		return processJson;
	}
	
	public JSONObject sendTransaction(String to, String amount) {
		JSONObject processJson = new JSONObject();
		try {
			TransactionReceipt transactionReceipt = Transfer.sendFunds(
			        web3j, credentials, to,
			        BigDecimal.valueOf(Integer.parseInt(amount)), Convert.Unit.ETHER).send();
			String from = transactionReceipt.getFrom();
			BigInteger gasUsed = transactionReceipt.getGasUsed();
			String txHash = transactionReceipt.getTransactionHash();
			processJson.put("from", from);
			processJson.put("to", to);
			processJson.put("gasUsed", gasUsed);
			processJson.put("txHash", txHash);
			System.out.println("sendTransaction completed, txHash: "+txHash);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return processJson;
	}
	
	public JSONObject sendTransaction(String from, String to, String value, String password) {
		JSONObject processJson = new JSONObject();
		try {	
			if(this.unlockAccount(from, password)) {
		        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
		                from, DefaultBlockParameterName.LATEST).sendAsync().get();
		        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
		        Transaction transaction = Transaction.createEtherTransaction(from, nonce, GAS_PRICE, GAS_LIMIT, 
		        		to, Numeric.decodeQuantity(value));
		        EthSendTransaction transactionResponse = admin.ethSendTransaction(transaction).send();
				processJson.put("from", from);
				processJson.put("to", to);
				processJson.put("txHash", transactionResponse.hashCode());
				System.out.println("sendTransaction completed, txHash: "+transactionResponse.hashCode());
			}else {
				System.out.println("account is not unlocked yet");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return processJson;
	}
	
	public boolean unlockAccount(String address, String password) {
		boolean result = false;
		try {
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, password).send();
			result = personalUnlockAccount.accountUnlocked();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int signMessage(String address, String password, Object obj) {
		int hxCode = -1;
		String sha3HashOfDataToSign = Hash.sha3String(obj.toString());
		if(this.unlockAccount(address, password)) {
			try {
				EthSign ethsign = admin.ethSign(address, sha3HashOfDataToSign).send();
				hxCode = ethsign.hashCode();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return hxCode;
	}
	

}
