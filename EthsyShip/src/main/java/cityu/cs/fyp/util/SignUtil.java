package cityu.cs.fyp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

public class SignUtil {
	
	public static int signMessage(byte[] message, ECKeyPair keyPair) {
		 SignatureData signatureData = Sign.signPrefixedMessage(message, keyPair);
		 return signatureData.hashCode();
	}
	
	public static BigInteger publicKeyFromPrivateKey(String privateKey) {
		return Sign.publicKeyFromPrivate(Numeric.toBigInt(privateKey));
	}
	
	public static byte[] objToByteArray(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(obj);
	    oos.flush();
	    byte [] data = bos.toByteArray();
	    return data;
	}
	
	public static boolean verifyAddressFromSignature(SignatureData signautre, byte[] msgHash, String address) {
		String addressRecovered = null;
        boolean match = false;

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey =
                    Sign.recoverFromSignature(
                            (byte) i,
                            new ECDSASignature(
                                    new BigInteger(1, signautre.getR()), new BigInteger(1, signautre.getS())),
                            msgHash);

            if (publicKey != null) {
                addressRecovered = "0x" + Keys.getAddress(publicKey);

                if (addressRecovered.equals(address)) {
                    match = true;
                    break;
                }
            }
        }
        return match;
	}

}
