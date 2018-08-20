package test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import base.Constant.SamplingMethod;
import base.FileBlock;
import base.GenuineBlock;
import base.GenuineFile;
import base.TimeCount;
import base.UserFile;
import framework.ClientExtension;
import secure.PRF;

public class TestNoDedupCost {

	private static final int blockSize = 16;

	private static final SamplingMethod METHOD = SamplingMethod.Uniform;

	private static final int R = 128;

	public static void main(String args[]) {

		int round = 1;

		if (args.length >= 1) {

			round = Integer.parseInt(args[0]);
		}

		// KB
		long[] fileSize = { 16, 256, 4096, 32768, 65536, 1048576 };

		Random rnd = new Random();

		List<List<TimeCount>> totalTimes = new ArrayList<>();

		// initialize the system calls

		byte[] dummyData = new byte[16];

		byte[] dummyK = ClientExtension.genK(dummyData, dummyData);

		byte[] dummyC = ClientExtension.encrypt(dummyK, dummyData);

		BigInteger dummyCT = ClientExtension.genTag(dummyC);

		for (int loop = 0; loop < round; ++loop) {

			System.out.println("Round - " + (loop + 1));

			List<TimeCount> times = new ArrayList<>();

			for (int i = 0; i < fileSize.length; i++) {

				// Step 1: create different size files

				System.out.println("Creating and testing file in " + fileSize[i] + " KB...");

				long numOfBlocks = fileSize[i] / blockSize;

				List<FileBlock> blockList = new ArrayList<>();

				for (int j = 0; j < numOfBlocks; j++) {

					byte[] bData = new byte[blockSize * 1024];

					rnd.nextBytes(bData);

					GenuineBlock gb = new GenuineBlock(bData);

					blockList.add(gb);
				}

				UserFile gf = new GenuineFile(blockList, ("file-No-" + i + "-" + fileSize[i] + "KB.txt"));

				// Step 2: encryption

				byte[] fK = null;

				byte[] fC = null;

				byte[] fData = gf.getData();

				// file-level

				long cTime4s = System.nanoTime();

				fK = PRF.SHA256(fData);

				fC = ClientExtension.encrypt(fK, fData);

				long cTime4 = System.nanoTime() - cTime4s;

				long case1 = cTime4;
				long case2 = cTime4;
				long case3 = cTime4;
				long case4 = cTime4;

				TimeCount tc = new TimeCount(case1, case2, case3, case4);

				times.add(tc);
			}

			totalTimes.add(times);
		}

		System.out.println("file size (KB), case 1 (ms), case 2 (ms), case 3 (ms), case 4 (ms)");

		long[] case1 = new long[fileSize.length];
		long[] case2 = new long[fileSize.length];
		long[] case3 = new long[fileSize.length];
		long[] case4 = new long[fileSize.length];

		for (int i = 0; i < round; ++i) {
			for (int j = 0; j < fileSize.length; ++j) {

				case1[j] += totalTimes.get(i).get(j).getCase1();
				case2[j] += totalTimes.get(i).get(j).getCase2();
				case3[j] += totalTimes.get(i).get(j).getCase3();
				case4[j] += totalTimes.get(i).get(j).getCase4();
			}
		}

		for (int j = 0; j < fileSize.length; ++j) {

			System.out.println(fileSize[j] + ", " + case1[j] / round / 1000000.0 + ", " + case2[j] / round / 1000000.0 + ", " + case3[j] / round / 1000000.0 + ", " + case4[j] / round / 1000000.0);
		}
	}
}
