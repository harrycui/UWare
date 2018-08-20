package index;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

public class FTIndex {
	
	public boolean hasFT(BigInteger fT) {
		
		return index.containsKey(fT);
	}
	
	public FTIndex() {
		index = new HashMap<BigInteger, ValueField>(100);
	}

	public BigInteger getR(BigInteger fT) {
		if (index.containsKey(fT))
			return index.get(fT).r;
		else
			return null;
	}

	public HashSet<Long> getFidSet(BigInteger fT) {
		return index.get(fT).fidSet;
	}

	
	public void updateIndex(BigInteger fT, BigInteger r, long fid) {
		
		if (index.containsKey(fT)) {
			
			index.get(fT).fidSet.add(fid);
		} else {
			index.put(fT, new ValueField(r, fid));
		}
	}

	private class ValueField {
		private ValueField() {
			r = BigInteger.ZERO;
			fidSet = new HashSet<Long>();
		}

		private ValueField(BigInteger r, long fid) {
			this.r = r;
			fidSet = new HashSet<Long>();
			fidSet.add(fid);
		}

		private BigInteger r;
		private HashSet<Long> fidSet;
	}

	private HashMap<BigInteger, ValueField> index;
}
