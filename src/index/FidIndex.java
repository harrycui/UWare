package index;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class FidIndex {
	
	public FidIndex() {
		index = new HashMap<Long, BigInteger>();
	}
	
	public BigInteger getR(long fid) {
		return index.get(fid);
	}
	
	public void addNewEntry(long fid, BigInteger r) {
		index.put(fid, r);
	}
	
	private Map<Long, BigInteger> index;
}
