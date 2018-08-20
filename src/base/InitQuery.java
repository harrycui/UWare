package base;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class InitQuery {

	private BigInteger fT;
	
	private List<BigInteger> bTs;
	
	private Set<BigInteger> rpTs;
	
	public InitQuery(BigInteger fT, List<BigInteger> bTs, Set<BigInteger> rpTs) {
		
		this.fT = fT;
		this.bTs = bTs;
		this.rpTs = rpTs;
	}

	public BigInteger getfT() {
		return fT;
	}

	public List<BigInteger> getbTs() {
		return bTs;
	}

	public Set<BigInteger> getRpTs() {
		return rpTs;
	}
}
