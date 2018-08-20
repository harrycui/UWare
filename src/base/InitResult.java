package base;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import base.Constant.FileStatus;

public class InitResult {

	private BigInteger r;
	
	private BigInteger rStar;
	
	public BigInteger getrStar() {
		return rStar;
	}

	public void setrStar(BigInteger rStar) {
		this.rStar = rStar;
	}

	private FileStatus fileStatus;
	
	private Set<Long> fidSet;
	
	private List<BlockProperty> btrList;
	
	private Set<BigInteger> rpTs;
	
	public Set<BigInteger> getRpTs() {
		return rpTs;
	}

	public void setRpTs(Set<BigInteger> rpTs) {
		this.rpTs = rpTs;
	}

	/**
	 * 
	 * @param r
	 * @param fileStatus
	 * @param fidSet empty means the incoming file is fresh
	 */
	public InitResult(BigInteger r, FileStatus fileStatus, Set<Long> fidSet) {
		
		this.r = r;
		this.fileStatus = fileStatus;
		this.fidSet = fidSet;
	}

	public List<BlockProperty> getBtrList() {
		return btrList;
	}

	public void setBtrList(List<BlockProperty> btrlist) {
		this.btrList = btrlist;
	}

	public BigInteger getR() {
		return r;
	}

	public FileStatus getFileStatus() {
		return fileStatus;
	}

	public Set<Long> getFidSet() {
		return fidSet;
	}
}
