package base;

import java.math.BigInteger;

public interface FileBlock {
	
	public BigInteger getbT();

	public byte[] getData();
	
	public BigInteger getR();
	
	public long blockSize();	
}
