package base;

import java.math.BigInteger;

import secure.PRF;

public class GenuineBlock implements FileBlock {
	
	public GenuineBlock(byte[] data) {
		this.data = data;
		
		this.bT = new BigInteger(PRF.SHA256(data));
	}

	@Override
	public BigInteger getbT() {
		return bT;
	}
	
	public BigInteger getR() {
		return new BigInteger(PRF.SHA256(bT.toByteArray()));
	}

	@Override
	public byte[] getData() {
		return data;
	}
	
	@Override
	public long blockSize() {
		return data.length;
	}
	
	private byte[] data;
	
	private BigInteger bT;
}
