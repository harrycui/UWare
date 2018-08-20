package base;

public class Counter {

	public long totalFileCount = 0;
	public long totalUniqueFileCount = 0;
	public long totalDupFileCount = 0;
	public long totalSimilarFileCount = 0;

	public long totalBlockCount = 0;
	public long totalUniqueBlockCount = 0;
	public long totalDupBlockCount = 0;

	public long totalFileSize = 0;
	public long totalUniqueBlockSize = 0;
	public long totalDupBlockSize = 0;

	public long totalRPTCount = 0;
	public long totalFidInRPTIndexCount = 0;

	public long totalClientSideTime = 0;
	public long totalUWareTime = 0;
	
	public long cumClientSideTime = 0;
	public long cumUWareTime = 0;

	public double dedupRatioInSize = 0;
	
	public String toString() {

		return this.totalFileCount + ", " + this.totalUniqueFileCount + ", " + this.totalDupFileCount + ", "
				+ this.totalSimilarFileCount + ", " + this.totalBlockCount + ", " + this.totalUniqueBlockCount + ", "
				+ this.totalDupBlockCount + ", " + this.totalFileSize/1024/1024 + ", " + this.totalUniqueBlockSize/1024/1024 + ", "
				+ this.totalDupBlockSize/1024/1024 + ", " + this.totalRPTCount + ", " + this.totalFidInRPTIndexCount + ", "
				+ this.totalClientSideTime + ", " + this.totalUWareTime + ", " + this.cumClientSideTime + ", "
				+ this.cumUWareTime + ", " + this.dedupRatioInSize;
	}

	public Counter() {
		super();
	}

	public Counter(Counter c, long preClientSideTime, long preUWareTime) {

		super();
		this.totalFileCount = c.totalFileCount;
		this.totalUniqueFileCount = c.totalUniqueFileCount;
		this.totalDupFileCount = c.totalDupFileCount;
		this.totalSimilarFileCount = c.totalSimilarFileCount;
		this.totalFileSize = c.totalFileSize;
		this.totalBlockCount = c.totalBlockCount;
		this.totalUniqueBlockCount = c.totalUniqueBlockCount;
		this.totalDupBlockCount = c.totalDupBlockCount;
		this.totalUniqueBlockSize = c.totalUniqueBlockSize;
		this.totalDupBlockSize = c.totalDupBlockSize;
		this.totalRPTCount = c.totalRPTCount;
		this.totalFidInRPTIndexCount = c.totalFidInRPTIndexCount;
		this.totalClientSideTime = c.totalClientSideTime;
		this.totalUWareTime = c.totalUWareTime;
		
		this.cumClientSideTime = c.totalClientSideTime - preClientSideTime;
		this.cumUWareTime = c.totalUWareTime - preUWareTime;

		assert (totalFileSize == totalUniqueBlockSize + totalDupBlockSize);

		this.dedupRatioInSize = 100.0 * totalDupBlockSize / totalFileSize;
	}
}
