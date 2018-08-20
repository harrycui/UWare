package test;

import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.jcajce.provider.symmetric.Threefish;

import base.BlockProperty;
import base.Constant.DedupEngine;
import base.Constant.FileStatus;
import base.Counter;
import base.FileBlock;
import base.FinalResult;
import base.InitQuery;
import base.InitResult;
import base.PoWProof;
import base.UserFile;
import framework.ClientExtension;
import framework.UWareDB;
import framework.UWareDedup;
import framework.User;

public class Simulator {
	
	public static long numOfCandidate = 0;
	
	public static Counter counter = new Counter();
	
	public static List<List<Long>> UWareTimes = new ArrayList<>(6);
	
	public static List<List<Long>> UWareCandidateNum = new ArrayList<>(6);
	
	//public static int[] blockNum = { 1, 16, 256, 2048, 4096, 65536 };
	
	// for aws testing
	public static int[] blockNum = { 4096, 131072, 0, 0, 0, 0 };

	public static boolean uploadFileCaseOneAndThree(User user, UWareDedup dedup, UserFile file) {
		
		Simulator.counter.totalFileSize += file.fileSize();
		
		// Step 1: client side preparation
		
		long cTime1s = System.nanoTime();
		
		BigInteger fT = ClientExtension.genTag(file);
		
		List<BigInteger> bTs = null;
		
		Set<BigInteger> rpTs = null;
		
		Iterator<FileBlock> blockIter = null;
		
		if (user.getEngine() != DedupEngine.FileLevel) {
			bTs = new ArrayList<>(file.numOfBlock());
			
			blockIter = file.blockIterator();
			
			while (blockIter.hasNext()) {
				bTs.add(ClientExtension.genTag(blockIter.next().getData()));
			}
			
			assert(bTs.size() == file.numOfBlock());
		}
		
		if (user.getEngine() == DedupEngine.SimilarityBasedDualLevel) {
			rpTs = ClientExtension.genRPTs(bTs, user.getR(), user.getMethod());
		}
		
		InitQuery initQuery = new InitQuery(fT, bTs, rpTs);
		
		long cTime1 = System.nanoTime() - cTime1s;
		
		// Step 2: query the UWare service
		
		long sTime1s = System.nanoTime();
		
		InitResult initResult = dedup.dedupInitCaseOneAndThree(initQuery, user.getEngine());
		
		long sTime1 = System.nanoTime() - sTime1s;
		
		// Step 3: PoW proof
		
		long cTime2s = System.nanoTime();
		
		byte[] fKcK = null;
		
		byte[] fCbC = null;
		
		BigInteger fCT = null;
		
		List<BigInteger> bCTs = null;
		
		PoWProof proof = null;
		
		if (user.getEngine() == DedupEngine.FileLevel) {
			
			fKcK = ClientExtension.genK(file.getData(), initResult.getR().toByteArray());
			
			fCbC = ClientExtension.encrypt(fKcK, file.getData());
			
			fCT = ClientExtension.genStarTag(fCbC, initResult.getrStar().toByteArray());
			
		    List<Byte> fCbcList = new ArrayList<Byte>(fCbC.length);
		    for(byte b: fCbC)
		    {
		    	fCbcList.add(b);
		    }

			proof = new PoWProof(fCT, fCbcList, null, null);
		} else if (user.getEngine() == DedupEngine.SimilarityBasedDualLevel) {
			
			bCTs = new ArrayList<BigInteger>(file.numOfBlock());
			
			blockIter = file.blockIterator();
			
			List<List<Byte>> bCbcLists = new ArrayList<List<Byte>>();
			
			while (blockIter.hasNext()) {
				
				byte[] blockData = blockIter.next().getData();
				
				fKcK = ClientExtension.genK(blockData, initResult.getR().toByteArray());
				
				fCbC = ClientExtension.encrypt(fKcK, blockData);
				
				bCTs.add(ClientExtension.genStarTag(fCbC, initResult.getrStar().toByteArray()));
				
			    List<Byte> bCbcList = new ArrayList<Byte>(fCbC.length);
			    for(byte b: fCbC)
			    {
			    	bCbcList.add(b);
			    }
			    
			    bCbcLists.add(bCbcList);
			}
			
			proof = new PoWProof(null, null, bCTs, bCbcLists);
		}
		
		long cTime2 = System.nanoTime() - cTime2s;
		
		// Step 4: send proof to do final checking
		
		long sTime2s = System.nanoTime();
		
		FinalResult finalResult = dedup.dedupFinalCaseOneAndThree(proof, user.getEngine(), initQuery, initResult);
		
		long sTime2 = System.nanoTime() - sTime2s;
		
		long curClientSideTime = cTime1 + cTime2;
		long curUWareTime = sTime1 + sTime2;
		
		if (file.numOfBlock() <= Simulator.blockNum[0]) {
			
			UWareTimes.get(0).add(curUWareTime);
			UWareCandidateNum.get(0).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[0] && file.numOfBlock() <= Simulator.blockNum[1]) {
			
			UWareTimes.get(1).add(curUWareTime);
			UWareCandidateNum.get(1).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[1] && file.numOfBlock() <= Simulator.blockNum[2]) {
			
			UWareTimes.get(2).add(curUWareTime);
			UWareCandidateNum.get(2).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[2] && file.numOfBlock() <= Simulator.blockNum[3]) {
			
			UWareTimes.get(3).add(curUWareTime);
			UWareCandidateNum.get(3).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[3] && file.numOfBlock() <= Simulator.blockNum[4]) {
			
			UWareTimes.get(4).add(curUWareTime);
			UWareCandidateNum.get(4).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[4] && file.numOfBlock() <= Simulator.blockNum[5]) {
			
			UWareTimes.get(5).add(curUWareTime);
			UWareCandidateNum.get(5).add(numOfCandidate);
		}
		
		
		Simulator.counter.totalClientSideTime += curClientSideTime;
		Simulator.counter.totalUWareTime += curUWareTime;
		
		if(finalResult.getFileStatus() == FileStatus.Duplicate)
		{
			Simulator.counter.totalDupFileCount++;
			Simulator.counter.totalDupBlockSize += file.fileSize();
			
			Simulator.counter.totalDupBlockCount += file.numOfBlock();
		} else if (finalResult.getFileStatus() == FileStatus.Fresh) {

			Simulator.counter.totalUniqueFileCount++;
			Simulator.counter.totalUniqueBlockSize += file.fileSize();
			Simulator.counter.totalUniqueBlockCount += file.numOfBlock();
		} else if (finalResult.getFileStatus() == FileStatus.NearDuplicate) {
			
			Simulator.counter.totalSimilarFileCount++;
			
			blockIter = file.blockIterator();
			
			int i = 0;
			while (blockIter.hasNext()) {
				
				if (finalResult.getDedupList().get(i) == true) {
					Simulator.counter.totalDupBlockSize += blockIter.next()
							.blockSize();
					Simulator.counter.totalDupBlockCount++;
				} else {
					Simulator.counter.totalUniqueBlockSize += blockIter.next()
							.blockSize();
					Simulator.counter.totalUniqueBlockCount++;
				}
				
				++i;
			}
		}
		
		Simulator.counter.totalFileCount++;
		Simulator.counter.totalBlockCount += file.numOfBlock();
		
		return true;
	}
	
	public static boolean uploadFileCaseTwo(User user, UWareDedup dedup, UserFile file) {
		
		UWareDedup.oneFileSelfDedupBufferForCaseTwo.clear();
		
		// Step 1: client side preparation
		SoftReference<List<BigInteger>> bTs = null;
		
		assert(user.getEngine() == DedupEngine.BlockLevel);
		
		long cTime1s = System.nanoTime();
		
		bTs = new SoftReference<List<BigInteger>>(new ArrayList<BigInteger>(file.numOfBlock()));
		
		Iterator<FileBlock> blockIter = file.blockIterator();
		
		while (blockIter.hasNext()) {
			bTs.get().add(ClientExtension.genTag(blockIter.next().getData()));
		}
		
		assert(bTs.get().size() == file.numOfBlock());
		
		SoftReference<InitQuery> initQuery = new SoftReference<InitQuery>(new InitQuery(null, bTs.get(), null));
		
		long cTime1 = System.nanoTime() - cTime1s;
		
		// Step 2: query the UWare service
		
		long sTime1s = System.nanoTime();
		
		List<InitResult> initResults = dedup.dedupInitCaseTwo(initQuery.get(), user.getEngine());
		
		long sTime1 = System.nanoTime() - sTime1s;
		
		// Step 3: PoW proof
		
		long cTime2s = System.nanoTime();
		
		SoftReference<byte[]> fKcK = null;
		
		SoftReference<byte[]> fCbC = null;
		
		SoftReference<List<BigInteger>> bCTs = null;
		
		SoftReference<PoWProof> proof = null;
		
		bCTs = new SoftReference<List<BigInteger>>(new ArrayList<BigInteger>(file.numOfBlock()));
		
		blockIter = file.blockIterator();
		
		List<List<Byte>> bCbcLists = new ArrayList<List<Byte>>();
		
		int i = 0;
		while (blockIter.hasNext()) {
			
			byte[] blockData = blockIter.next().getData();
			
			fKcK = new SoftReference<byte[]>(ClientExtension.genK(blockData, initResults.get(i).getR().toByteArray()));
			
			fCbC = new SoftReference<byte[]>(ClientExtension.encrypt(fKcK.get(), blockData));
			
			bCTs.get().add(ClientExtension.genStarTag(fCbC.get(), initResults.get(i).getrStar().toByteArray()));
			
			i++;
			
		    List<Byte> bCbcList = new ArrayList<Byte>(fCbC.get().length);
		    for(byte b: fCbC.get())
		    {
		    	bCbcList.add(b);
		    }
		    
		    bCbcLists.add(bCbcList);
		}
		
		proof = new SoftReference<PoWProof>(new PoWProof(null, null, bCTs.get(), bCbcLists));
		
		long cTime2 = System.nanoTime() - cTime2s;
		
		// Step 4: send proof to do final checking
		
		long sTime2s = System.nanoTime();
		
		SoftReference<List<FinalResult>> finalResults = new SoftReference<List<FinalResult>>(dedup.dedupFinalCaseTwo(proof.get(), user.getEngine(), initQuery.get(), initResults));
		
		long sTime2 = System.nanoTime() - sTime2s;
		
		long curClientSideTime = cTime1 + cTime2;
		long curUWareTime = sTime1 + sTime2;
		
		if (file.numOfBlock() <= Simulator.blockNum[0]) {
			
			UWareTimes.get(0).add(curUWareTime);
			UWareCandidateNum.get(0).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[0] && file.numOfBlock() <= Simulator.blockNum[1]) {
			
			UWareTimes.get(1).add(curUWareTime);
			UWareCandidateNum.get(1).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[1] && file.numOfBlock() <= Simulator.blockNum[2]) {
			
			UWareTimes.get(2).add(curUWareTime);
			UWareCandidateNum.get(2).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[2] && file.numOfBlock() <= Simulator.blockNum[3]) {
			
			UWareTimes.get(3).add(curUWareTime);
			UWareCandidateNum.get(3).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[3] && file.numOfBlock() <= Simulator.blockNum[4]) {
			
			UWareTimes.get(4).add(curUWareTime);
			UWareCandidateNum.get(4).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[4] && file.numOfBlock() <= Simulator.blockNum[5]) {
			
			UWareTimes.get(5).add(curUWareTime);
			UWareCandidateNum.get(5).add(numOfCandidate);
		}
		
		numOfCandidate = 0;
		
		Simulator.counter.totalClientSideTime += curClientSideTime;
		Simulator.counter.totalUWareTime += curUWareTime;
		
		blockIter = file.blockIterator();
		
		i = 0;
		while (blockIter.hasNext()) {
			
			FinalResult fr = finalResults.get().get(i);
			
			long blockSize = blockIter.next().blockSize();
			
			if(fr.getFileStatus() == FileStatus.Duplicate)
			{
				
				Simulator.counter.totalDupBlockCount++;
				Simulator.counter.totalDupBlockSize += blockSize;
				
			} else if (fr.getFileStatus() == FileStatus.Fresh) {

				Simulator.counter.totalUniqueBlockCount++;
				Simulator.counter.totalUniqueBlockSize += blockSize;
			}
			
			Simulator.counter.totalBlockCount++;
			Simulator.counter.totalFileSize += blockSize;
			
			i++;
		}
		
		Simulator.counter.totalFileCount++;
		
		return true;
	}

	public static boolean uploadFileCaseFour(User user, UWareDedup dedup,
			UserFile file) {

		

		// Step 1: client side preparation

		long cTime1s = System.nanoTime();

		BigInteger fT = ClientExtension.genTag(file);

		List<BigInteger> bTs = null;

		Iterator<FileBlock> blockIter = null;

		bTs = new ArrayList<>(file.numOfBlock());

		blockIter = file.blockIterator();

		while (blockIter.hasNext()) {
			bTs.add(ClientExtension.genTag(blockIter.next().getData()));
		}

		assert (bTs.size() == file.numOfBlock());

		InitQuery initQuery = new InitQuery(fT, bTs, null);

		long cTime1 = System.nanoTime() - cTime1s;

		// Step 2: query the UWare service

		long sTime1s = System.nanoTime();

		InitResult initResult = dedup.dedupInitCaseOneAndThree(initQuery,user.getEngine());

		long sTime1 = System.nanoTime() - sTime1s;

		// Step 3: PoW proof

		long cTime2s = System.nanoTime();

		byte[] fKcK = null;

		byte[] fCbC = null;

		List<BigInteger> bCTs = null;

		PoWProof proof = null;

		bCTs = new ArrayList<BigInteger>(file.numOfBlock());

		blockIter = file.blockIterator();

		List<BlockProperty> btrs = initResult.getBtrList();

		Iterator<BlockProperty> btrIter = btrs.iterator();

		blockIter = file.blockIterator();
		
		assert(btrs.size() == file.numOfBlock());

		while (blockIter.hasNext()) {

			FileBlock block = blockIter.next();

			byte[] blockData = block.getData();

			assert (btrIter.hasNext());

			BlockProperty btr = btrIter.next();

			fKcK = ClientExtension.genK(blockData, btr.R.toByteArray());

			fCbC = ClientExtension.encrypt(fKcK, blockData);

			bCTs.add(ClientExtension.genTag(fCbC));
		}

		// TODO case four do not use rStar
		proof = new PoWProof(null, null, bCTs, null);

		long cTime2 = System.nanoTime() - cTime2s;

		// Step 4: send proof to do final checking

		long sTime2s = System.nanoTime();

		FinalResult finalResult = dedup.dedupFinalCaseOneAndThree(proof,
				user.getEngine(), initQuery, initResult);

		long sTime2 = System.nanoTime() - sTime2s;

		long curClientSideTime = cTime1 + cTime2;
		long curUWareTime = sTime1 + sTime2;

		if (file.numOfBlock() <= Simulator.blockNum[0]) {

			UWareTimes.get(0).add(curUWareTime);
			UWareCandidateNum.get(0).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[0]
				&& file.numOfBlock() <= Simulator.blockNum[1]) {

			UWareTimes.get(1).add(curUWareTime);
			UWareCandidateNum.get(1).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[1]
				&& file.numOfBlock() <= Simulator.blockNum[2]) {

			UWareTimes.get(2).add(curUWareTime);
			UWareCandidateNum.get(2).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[2]
				&& file.numOfBlock() <= Simulator.blockNum[3]) {

			UWareTimes.get(3).add(curUWareTime);
			UWareCandidateNum.get(3).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[3]
				&& file.numOfBlock() <= Simulator.blockNum[4]) {

			UWareTimes.get(4).add(curUWareTime);
			UWareCandidateNum.get(4).add(numOfCandidate);
		} else if (file.numOfBlock() > Simulator.blockNum[4]
				&& file.numOfBlock() <= Simulator.blockNum[5]) {

			UWareTimes.get(5).add(curUWareTime);
			UWareCandidateNum.get(5).add(numOfCandidate);
		}

		// numOfCandidate = 0;

		Simulator.counter.totalClientSideTime += curClientSideTime;
		Simulator.counter.totalUWareTime += curUWareTime;

		if (finalResult.getFileStatus() == FileStatus.Duplicate) {
			// System.out.println(file.fileName() + " is " +
			// FileStatus.Duplicate);
			// System.out.println("File No. " + finalResult.getFid() + " is " +
			// FileStatus.Duplicate);

			Simulator.counter.totalDupFileCount++;
			Simulator.counter.totalDupBlockSize += file.fileSize();

			Simulator.counter.totalDupBlockCount += file.numOfBlock();
		} else if (finalResult.getFileStatus() == FileStatus.Fresh) {

			Simulator.counter.totalUniqueFileCount++;
			Simulator.counter.totalUniqueBlockSize += file.fileSize();
			Simulator.counter.totalUniqueBlockCount += file.numOfBlock();
		} else if (finalResult.getFileStatus() == FileStatus.NearDuplicate) {

			Simulator.counter.totalSimilarFileCount++;

			blockIter = file.blockIterator();

			int i = 0;
			while (blockIter.hasNext()) {
				
				long blkSize = blockIter.next().blockSize();
				assert(blkSize > 0);
				
				if (finalResult.getDedupList().get(i) == true) {
					Simulator.counter.totalDupBlockSize += blkSize;
					Simulator.counter.totalDupBlockCount++;
				} else {
					Simulator.counter.totalUniqueBlockSize += blkSize;
					Simulator.counter.totalUniqueBlockCount++;
				}
				
				++i;
			}

		}

		Simulator.counter.totalFileCount++;
		Simulator.counter.totalFileSize += file.fileSize();
		Simulator.counter.totalBlockCount += file.numOfBlock();

		return true;
	}
}
