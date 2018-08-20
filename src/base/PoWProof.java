package base;

import java.math.BigInteger;
import java.util.List;

public class PoWProof {

	private BigInteger fCT;
	
	private List<Byte> fEnc;

	private List<List<Byte>> bEncs;
	
	private List<BigInteger> bCTs;
	
	private List<BlockProperty> bCTs2;
	
	public PoWProof(BigInteger fCT, List<Byte> fEnc, List<BigInteger> bCTs , List<List<Byte>> bEncs) {
		
		this.fCT = fCT;
		this.bCTs = bCTs;
		this.fEnc = fEnc;
		this.bEncs = bEncs;
	}
	
	public PoWProof(List<BlockProperty> bCTs2) {
		
		this.bCTs2 = bCTs2;
	}

	public BigInteger getfCT() {
		return fCT;
	}

	public List<BigInteger> getbCTs() {
		return bCTs;
	}
	
	public List<BlockProperty> getbCTs2() {
		return bCTs2;
	}
	
	public List<Byte> getfEnc() {
		return fEnc;
	}

	public List<List<Byte>> getbEncs() {
		return bEncs;
	}
}
