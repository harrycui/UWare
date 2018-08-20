package base;

import java.math.BigInteger;
import java.util.Iterator;

public interface UserFile {	
	
	public Iterator<FileBlock> blockIterator();

	public int numOfBlock();

	public String fileName();

	public long fileSize();
	
	public BigInteger getfT();
	
	public byte[] getData();
}
