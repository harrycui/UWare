package secure;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.Arrays;

/**
 * This class is used to implement pseudo-random function.
 */
public class PRF {

    public static final String hashType = "SHA-256";

	public static final char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String genShortHash(String fileName, int outputLength) throws Exception {

		String hash = genHash(fileName);

		return hash.substring(0, outputLength);
	}

	public static String genHash(String fileName) throws Exception {

		InputStream fis;
		fis = new FileInputStream(fileName);
		byte[] buffer = new byte[1024];
		MessageDigest digest = MessageDigest.getInstance(hashType);
		int numRead = 0;
		while ((numRead = fis.read(buffer)) > 0) {
			digest.update(buffer, 0, numRead);
		}
		fis.close();
		return toHexString(digest.digest());
	}

    public static byte[] SHA256(byte[] msg) {

        try {

            MessageDigest md = MessageDigest.getInstance(hashType);

            md.update(msg);
            
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static byte[] mergeArray(byte[] a, byte[] b)
    {
    	byte[] c = Arrays.copyOf(a, a.length+b.length);
    	for(int i=0; i<b.length; i++)
    	{
    		c[i+a.length] = b[i];
    	}
    	return c;
    }

    public static byte[] Sha256DataAndR(byte[] data, byte[] r) {

        byte[] digest;

    	digest = SHA256(mergeArray(data,r));

        return digest;
    }
    
    public static byte[] Sha256DataAndR(String data, String r) {

        byte[] digest;

        Charset asciiCs = Charset.forName("US-ASCII");
        digest = Sha256DataAndR(asciiCs.encode(data).array(),asciiCs.encode(r).array());
        return digest;
    }
}
