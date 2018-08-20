package base;

import java.math.BigInteger;

import secure.PRF;

public class FakeBlock implements FileBlock {
	
	public FakeBlock(BigInteger bT, long blockSize) {
		
		this.bT = bT;
		
		this.realBlockSize = blockSize;
	}

	@Override
	public BigInteger getbT() {
		//return new BigInteger(PRF.SHA256(bT.toByteArray()));
		return bT;
	}
	
	public BigInteger getR() {
		return new BigInteger(PRF.SHA256(bT.toByteArray()));
	}
	
	@Override
	public byte[] getData() {
		return bT.toByteArray();
	}
	
	@Override
	public long blockSize() {
		return realBlockSize;
	}
	
	private BigInteger bT;
	//in bytes
	private long realBlockSize;
}
