package test;

import java.math.BigInteger;

import secure.MyAES;
import secure.PRF;

public class TestSecurityFunction {
	
	public static void main(String args[]) {
		
		BigInteger msg = new BigInteger("13579");
		
		byte[] key = PRF.SHA256(msg.toByteArray());
		
		assert(key.length == 32);
		
		byte[] iv = new byte[16];
		
		byte[] c = MyAES.aesEnc(key, msg.toByteArray(), iv);
		
		System.out.println("ciphertext length: " + c.length);
		System.out.println("c = " + new BigInteger(c));
		
		byte[] dc = MyAES.aesDec(key, c, iv);
		
		System.out.println("plaintext length: " + dc.length);
		System.out.println("dc = " + new BigInteger(dc));
	}

}
