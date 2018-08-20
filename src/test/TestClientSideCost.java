package test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import base.Constant;
import base.Constant.SamplingMethod;
import base.FileBlock;
import base.GenuineBlock;
import base.GenuineFile;
import base.InitQuery;
import base.InitResult;
import base.PoWProof;
import base.TimeCount;
import base.UserFile;
import framework.ClientExtension;

public class TestClientSideCost {

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
				
				byte[] fData = gf.getData();

				// Step 2: prepare for initDedup
				long cTime1s = System.nanoTime();

				BigInteger fT = ClientExtension.genTag(fData);

				long cTime1 = System.nanoTime() - cTime1s;
				
				long cTime2s = System.nanoTime();

				List<BigInteger> bTs = new ArrayList<>(gf.numOfBlock());
				Iterator<FileBlock> blockIter = gf.blockIterator();

				while (blockIter.hasNext()) {
					bTs.add(ClientExtension.genTag(blockIter.next().getData()));
				}

				long cTime2 = System.nanoTime() - cTime2s;

				assert (bTs.size() == gf.numOfBlock());

				long cTime3s = System.nanoTime();

				Set<BigInteger> rpTs = ClientExtension.genRPTs(bTs, R, METHOD);

				long cTime3 = System.nanoTime() - cTime3s;

				InitQuery initQuery = new InitQuery(fT, bTs, rpTs);

				// create initial response

				BigInteger r = new BigInteger(Constant.rLen, rnd);

				InitResult initResult = new InitResult(r, null, null);

				// Step 3: PoW proof

				byte[] fKcK = null;

				byte[] fCbC = null;

				BigInteger fCT = null;

				List<BigInteger> bCTs = null;

				// file-level

				long cTime4s = System.nanoTime();

				fKcK = ClientExtension.genK(fData, initResult.getR().toByteArray());
				
				long cTime4 = System.nanoTime() - cTime4s;
				
				long cTime5s = System.nanoTime();

				fCbC = ClientExtension.encrypt(fKcK, fData);
				
				long cTime5 = System.nanoTime() - cTime5s;
				
				long cTime6s = System.nanoTime();

				fCT = ClientExtension.genTag(fCbC);
				
				long cTime6 = System.nanoTime() - cTime6s;

				// TODO this test do not use rstar
				PoWProof p1 = new PoWProof(fCT, null, null, null);

				// block-level and similarity-based dual level

				long blkKeyGen = 0;
				long blkEnc = 0;
				long blkTagGen = 0;

				bCTs = new ArrayList<BigInteger>(gf.numOfBlock());

				blockIter = gf.blockIterator();

				while (blockIter.hasNext()) {
					
					byte[] blockData = blockIter.next().getData();
					
					long cTime7s = System.nanoTime();

					fKcK = ClientExtension.genK(blockData, initResult.getR().toByteArray());
					
					blkKeyGen += System.nanoTime() - cTime7s;

					long cTime8s = System.nanoTime();
					
					fCbC = ClientExtension.encrypt(fKcK, blockData);
					
					blkEnc += System.nanoTime() - cTime8s;

					long cTime9s = System.nanoTime();

					bCTs.add(ClientExtension.genTag(fCbC));
					
					blkTagGen += System.nanoTime() - cTime9s;
				}

				//TODO this test do not use rstar
				PoWProof p2 = new PoWProof(null, null, bCTs,  null);
				
				long c1TagGen = cTime1 + cTime6;
				long c1Enc = cTime5;
				long c1KeyGen = cTime4;
				
				long c2TagGen = cTime1 + cTime2 + blkTagGen;
				long c2Enc = blkEnc;
				long c2KeyGen = blkKeyGen;
				
				long c3TagGen = cTime1 + cTime2 + blkTagGen;
				long c3Enc = blkEnc;
				long c3KeyGen = blkKeyGen;
				long c3RPTGen = cTime3;
				
				// added on 16-Feb-2017
				long c4TagGen = cTime1 + cTime2 + blkTagGen;
				long c4Enc = blkEnc;
				long c4KeyGen = blkKeyGen;

				long case1 = c1TagGen + c1Enc + c1KeyGen;
				long case2 = c2TagGen + c2Enc + c2KeyGen;
				long case3 = c3TagGen + c3Enc + c3KeyGen + c3RPTGen;
				long case4 = c4TagGen + c4Enc + c4KeyGen;

				TimeCount tc = new TimeCount(case1, case2, case3, case4, c1TagGen, c1Enc, c1KeyGen, c2TagGen, c2Enc, c2KeyGen, c3TagGen, c3Enc, c3KeyGen, c3RPTGen, c4TagGen, c4Enc, c4KeyGen);

				times.add(tc);
			}

			totalTimes.add(times);
		}

		System.out.println("file size (KB), case 1 (ms), case 2 (ms), case 3 (ms), case 4 (ms), c1TagGen, c1Enc, c1KeyGen, c2TagGen, c2Enc, c2KeyGen, c3TagGen, c3Enc, c3KeyGen, c3RPTGen, c4TagGen, c4Enc, c4KeyGen");

		long[] case1 = new long[fileSize.length];
		long[] case2 = new long[fileSize.length];
		long[] case3 = new long[fileSize.length];
		long[] case4 = new long[fileSize.length];
		
		long[] c1TagGen = new long[fileSize.length];
		long[] c1Enc = new long[fileSize.length];
		long[] c1KeyGen = new long[fileSize.length];
		long[] c2TagGen = new long[fileSize.length];
		long[] c2Enc = new long[fileSize.length];
		long[] c2KeyGen = new long[fileSize.length];
		long[] c3TagGen = new long[fileSize.length];
		long[] c3Enc = new long[fileSize.length];
		long[] c3KeyGen = new long[fileSize.length];
		long[] c3RPTGen = new long[fileSize.length];
		long[] c4TagGen = new long[fileSize.length];
		long[] c4Enc = new long[fileSize.length];
		long[] c4KeyGen = new long[fileSize.length];

		for (int i = 0; i < round; ++i) {
			for (int j = 0; j < fileSize.length; ++j) {

				case1[j] += totalTimes.get(i).get(j).getCase1();
				case2[j] += totalTimes.get(i).get(j).getCase2();
				case3[j] += totalTimes.get(i).get(j).getCase3();
				case4[j] += totalTimes.get(i).get(j).getCase4();
				
				c1TagGen[j] += totalTimes.get(i).get(j).getC1TagGen();
				c1Enc[j] += totalTimes.get(i).get(j).getC1Enc();
				c1KeyGen[j] += totalTimes.get(i).get(j).getC1KeyGen();
				
				c2TagGen[j] += totalTimes.get(i).get(j).getC2TagGen();
				c2Enc[j] += totalTimes.get(i).get(j).getC2Enc();
				c2KeyGen[j] += totalTimes.get(i).get(j).getC2KeyGen();
				
				c3TagGen[j] += totalTimes.get(i).get(j).getC3TagGen();
				c3Enc[j] += totalTimes.get(i).get(j).getC3Enc();
				c3KeyGen[j] += totalTimes.get(i).get(j).getC3KeyGen();
				c3RPTGen[j] += totalTimes.get(i).get(j).getC3RPTGen();
				
				c4TagGen[j] += totalTimes.get(i).get(j).getC4TagGen();
				c4Enc[j] += totalTimes.get(i).get(j).getC4Enc();
				c4KeyGen[j] += totalTimes.get(i).get(j).getC4KeyGen();
			}
		}

		for (int j = 0; j < fileSize.length; ++j) {

			System.out.println(fileSize[j] + ", " + case1[j] / round / 1000000.0 + ", " + case2[j] / round / 1000000.0 + ", " + case3[j] / round / 1000000.0 + ", " + case4[j] / round / 1000000.0 + 
					", " + c1TagGen[j] / round / 1000000.0 + ", " + c1Enc[j] / round / 1000000.0 + ", " + c1KeyGen[j] / round / 1000000.0 + 
					", " + c2TagGen[j] / round / 1000000.0 + ", " + c2Enc[j] / round / 1000000.0 + ", " + c2KeyGen[j] / round / 1000000.0 + 
					", " + c3TagGen[j] / round / 1000000.0 + ", " + c3Enc[j] / round / 1000000.0 + ", " + c3KeyGen[j] / round / 1000000.0 + 
					", " + c3RPTGen[j] / round / 1000000.0 + ", " + c4TagGen[j] / round / 1000000.0 + ", " + c4Enc[j] / round / 1000000.0 + ", " + c4KeyGen[j] / round / 1000000.0);
		}
	}
}
