package base;

public class TimeCount {

	public long case1;

	public long case2;

	public long case3;
	
	public long case4;
	
	public long c1TagGen;
	public long c1Enc;
	public long c1KeyGen;
	public long c2TagGen;
	public long c2Enc;
	public long c2KeyGen;
	public long c3TagGen;
	public long c3Enc;
	public long c3KeyGen;
	public long c3RPTGen;
	public long c4TagGen;
	public long c4Enc;
	public long c4KeyGen;

	public TimeCount(long case1, long case2, long case3, long case4) {
		this.case1 = case1;
		this.case2 = case2;
		this.case3 = case3;
		this.case4 = case4;
	}

	public TimeCount(long case1, long case2, long case3, long case4, long c1TagGen, long c1Enc, long c1KeyGen, long c2TagGen,
			long c2Enc, long c2KeyGen, long c3TagGen, long c3Enc, long c3KeyGen, long c3RPTGen, long c4TagGen, long c4Enc, long c4KeyGen) {
		super();
		this.case1 = case1;
		this.case2 = case2;
		this.case3 = case3;
		this.case4 = case4;
		this.c1TagGen = c1TagGen;
		this.c1Enc = c1Enc;
		this.c1KeyGen = c1KeyGen;
		this.c2TagGen = c2TagGen;
		this.c2Enc = c2Enc;
		this.c2KeyGen = c2KeyGen;
		this.c3TagGen = c3TagGen;
		this.c3Enc = c3Enc;
		this.c3KeyGen = c3KeyGen;
		this.c3RPTGen = c3RPTGen;
		this.c4TagGen = c4TagGen;
		this.c4Enc = c4Enc;
		this.c4KeyGen = c4KeyGen;
	}



	public String toString() {

		return case1 + ", " + case2 + ", " + case3 + ", " + case4;
	}

	public long getCase1() {
		return case1;
	}

	public long getCase2() {
		return case2;
	}

	public long getCase3() {
		return case3;
	}

	public long getC1TagGen() {
		return c1TagGen;
	}

	public long getC1Enc() {
		return c1Enc;
	}

	public long getC1KeyGen() {
		return c1KeyGen;
	}

	public long getC2TagGen() {
		return c2TagGen;
	}

	public long getC2Enc() {
		return c2Enc;
	}

	public long getC2KeyGen() {
		return c2KeyGen;
	}

	public long getC3TagGen() {
		return c3TagGen;
	}

	public long getC3Enc() {
		return c3Enc;
	}

	public long getC3KeyGen() {
		return c3KeyGen;
	}

	public long getC3RPTGen() {
		return c3RPTGen;
	}

	public long getCase4() {
		return case4;
	}

	public long getC4TagGen() {
		return c4TagGen;
	}

	public long getC4Enc() {
		return c4Enc;
	}

	public long getC4KeyGen() {
		return c4KeyGen;
	}
}
