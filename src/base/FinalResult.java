package base;

import java.util.List;

import base.Constant.FileStatus;

public class FinalResult {

	private long fid;
	
	// could be null in file-level dedup
	private List<Long> bidList;
	
	private FileStatus fileStatus;
	
	private List<Boolean> dedupList;
	
	public List<Boolean> getDedupList() {
		return dedupList;
	}

	public void setDedupList(List<Boolean> dedupList) {
		this.dedupList = dedupList;
	}

	public FinalResult(long fid, List<Long> bidList, FileStatus fileStatus) {
		this.fid = fid;
		this.bidList = bidList;
		this.fileStatus = fileStatus;
	}

	public long getFid() {
		return fid;
	}

	public List<Long> getBidList() {
		return bidList;
	}

	public FileStatus getFileStatus() {
		return fileStatus;
	}
}
