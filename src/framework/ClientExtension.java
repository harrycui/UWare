package framework;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import base.Constant.SamplingMethod;
import base.FileBlock;
import base.UserFile;
import secure.MyAES;
import secure.PRF;

public class ClientExtension {
	
	public static final BigInteger genTag(byte[] data) {
		
		byte[] rawTag = PRF.SHA256(data);
		
		assert(rawTag != null);
				
		return new BigInteger(rawTag);
	}
	
	public static final BigInteger genStarTag(byte[] data, byte[] rstar) {
		
		byte[] rawTag = PRF.Sha256DataAndR(data, rstar);
		
		assert(rawTag != null);
				
		return new BigInteger(rawTag);
	}

	public static final BigInteger genTag(UserFile file) {
		
		byte[] rawTag = PRF.SHA256(file.getData());
		
		assert(rawTag != null);
				
		return new BigInteger(rawTag);
	}
	
	
	public static final BigInteger genTag(FileBlock block) {
		
		byte[] rawTag = PRF.SHA256(block.getData());
		
		assert(rawTag != null);
				
		return new BigInteger(rawTag);
	}
	
	public static final Set<BigInteger> genRPTs(List<BigInteger> bTList, int R, SamplingMethod method) {
		
		Set<BigInteger> rpTags = new HashSet<BigInteger>();
		
		// TODO
		switch(method) {
		case Uniform:
			for (int i = 0; i < bTList.size(); ++i) {
				if (i % R == 0) {
					rpTags.add(bTList.get(i));
				}
			}
			break;
		case Minimum:
			break;
		case Random:
			break;
		default:
			break;
		}
		
		return rpTags;
	}
	
	public static final byte[] genK(byte[] data, byte[] r) {
		
		return PRF.Sha256DataAndR(data, r);
	}
	
	public static final byte[] encrypt(byte[] key, byte[] data) {
		
		// Fixed IV in our design
		final byte[] iv = new byte[16];
		
		return MyAES.aesEnc(key, data, iv);
	}
	
	public static final byte[] decrypt(byte[] key, byte[] cData) {
		
		// Fixed IV in our design
		final byte[] iv = new byte[16];
		
		return MyAES.aesDec(key, cData, iv);
	}
}
