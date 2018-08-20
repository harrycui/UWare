package base;

/**
 * This class is used to define all the constant values.
 */
public class Constant {

	// Error
	public static final int ERROR_ARGUMENTS = 2;

	// Operation type
	public static final int OPERATION_LOAD_SERVER_DB = 1;
	public static final int OPERATION_TEST = 2;

	public static final int FID_NULL = -1;

	public enum SamplingMethod {
		Uniform, Minimum, Random
	};

	public enum FileStatus {
		Duplicate, // The file is duplicate and genuine.
		NearDuplicate, // The file is similar to existing ones.
		Irresolute, // The file is duplicate and wait to be PoWed.
		Fresh // The file is a fresh one.
	};

	public enum DedupEngine {
		FileLevel, BlockLevel, SimilarityBasedDualLevel, PerBlockRandomness
	};

	public static final int rLen = 64;
}
