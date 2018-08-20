package framework;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import test.TestUWare;
import base.BlockProperty;
import base.Constant;
import base.Constant.DedupEngine;
import base.Constant.FileStatus;
import base.Constant.SamplingMethod;
import base.FinalResult;
import base.InitQuery;
import base.InitResult;
import base.PoWProof;
import index.FTIndex;
import index.FidIndex;
import index.RPTIndex;

public class UWareDedup {
	
	private static long newFid = 1;
	
	private static long newBid = 1;
	
	private static Random rnd = new Random();

	private FTIndex fTIndex;
	private RPTIndex rpTIndex;
	private FidIndex fidIndex;
	
	//buffer for case 4  max is 35000000
	private Set<BigInteger> bf4SetBigInteger1 = new LinkedHashSet<BigInteger>(100);
	private Map<BigInteger, BlockProperty> bf4MapBigIntegerBlockProperty1 = new HashMap<BigInteger, BlockProperty>(100);
	private Map<BigInteger, BlockProperty> bf4MapBigIntegerBlockProperty2 = new HashMap<BigInteger, BlockProperty>(100);
	private Map<BigInteger, Long> bf4MapBigIntegerLong1 = new HashMap<BigInteger, Long>(100);
	private Map<BigInteger, Long> bf4MapBigIntegerLong2 = new HashMap<BigInteger, Long>(100);
	
	// <FT,R>
	public static Map<BigInteger, BigInteger> oneFileSelfDedupBufferForCaseTwo = new HashMap<BigInteger, BigInteger>();
	
	
	public RPTIndex getRpTIndex() {
		return rpTIndex;
	}


	public UWareDedup() {
		
		this.fTIndex = new FTIndex();
		this.rpTIndex = new RPTIndex();
		this.fidIndex = new FidIndex();
	}
	
	
	public InitResult dedupInitCaseOneAndThree(InitQuery initQuery, DedupEngine dedupEngine) {
		InitResult initResult = null;
		
		switch (dedupEngine) {
		case FileLevel:
			initResult = dedupInitCaseOne(initQuery.getfT());
			break;
		case SimilarityBasedDualLevel:
			initResult= dedupInitCaseThree(initQuery.getfT(), initQuery.getRpTs());
			break;
		case PerBlockRandomness:
			initResult= dedupInitCaseFour(initQuery.getfT(), initQuery.getbTs());
			break;
		default:
			break;
		}
		
		return initResult;
	}
		
	public List<InitResult> dedupInitCaseTwo(InitQuery initQuery, DedupEngine dedupEngine) {
		
		assert(dedupEngine == DedupEngine.BlockLevel);
		
		List<InitResult> initResults = new ArrayList<InitResult>();
		
		for (BigInteger bT : initQuery.getbTs()) {
			
			InitResult initResult = dedupInitCaseOne(bT);
			
			initResults.add(initResult);
		}
		
		return initResults;
	}

	private InitResult dedupInitCaseOne(BigInteger fT) {
		
		InitResult initResult = null;
		
		BigInteger r = fTIndex.getR(fT);
		
		FileStatus fileStatus = FileStatus.Fresh;
		Set<Long> fidSet = null;
		
		// find a duplicated file
		if (r != null) {
			fileStatus = FileStatus.Irresolute;
			fidSet = fTIndex.getFidSet(fT);
		} else {
			//r = new BigInteger(Constant.rLen, rnd);
			if (oneFileSelfDedupBufferForCaseTwo.containsKey(fT))
			{
				fileStatus = FileStatus.Irresolute;
				r=oneFileSelfDedupBufferForCaseTwo.get(fT);
			}
			else
			{
				r = new BigInteger(Constant.rLen, rnd);
				oneFileSelfDedupBufferForCaseTwo.put(fT,r);
			}
		}
		
		BigInteger rStar = new BigInteger(Constant.rLen, rnd);
		
		initResult = new InitResult(r, fileStatus, fidSet);
		initResult.setrStar(rStar);
		
		return initResult;
	}
	
	private InitResult dedupInitCaseThree(BigInteger fT, Set<BigInteger> rpTs) {
		
		InitResult initResult = null;
		
		BigInteger r = fTIndex.getR(fT);
		FileStatus fileStatus = FileStatus.Fresh;
		Set<Long> fidSet = null;
		
		// find a duplicated file
		if (r != null) {
			fileStatus = FileStatus.Irresolute;
			fidSet = fTIndex.getFidSet(fT);
			
			assert(fidSet.size() == 1);
		} else { // perform similarity-based block-level dedup checking
			
			Set<Long> similarFidSet = rpTIndex.getTopOneFid(rpTs);
			
			if (similarFidSet != null) {
				fileStatus = FileStatus.NearDuplicate;
				long topSimilarFid = getOneElementFromSet(similarFidSet);
				
				r = fidIndex.getR(topSimilarFid);
				//fidSet = fTIndex.getFidSet(similarFT);
				
				fidSet = new HashSet<>();
				
				fidSet.add(topSimilarFid);
				
				assert(fidSet.size() == 1);
			} else {
				r = new BigInteger(Constant.rLen, rnd);
			}
		}
		
		BigInteger rStar = new BigInteger(Constant.rLen, rnd);
		initResult = new InitResult(r, fileStatus, fidSet);
		initResult.setrStar(rStar);
		
		return initResult;
	}
	
	private InitResult dedupInitCaseFour(BigInteger fT, List<BigInteger> bTs) {

		// TODO the R and samplingMethod may be changed
		Set<BigInteger> rpTs = ClientExtension.genRPTs(bTs, 128, SamplingMethod.Uniform);

		InitResult initResult = null;
		
		FileStatus fileStatus = FileStatus.Fresh;
		Set<Long> fidSet = null;
		List<BlockProperty> btrList = new ArrayList<BlockProperty>();
		
		Map<BigInteger, BlockProperty> tmpBTBuffer = bf4MapBigIntegerBlockProperty2;
		tmpBTBuffer.clear();
		
		// find a duplicated file
		if (fTIndex.hasFT(fT)) {
			
			fileStatus = FileStatus.Irresolute;
			
			fidSet = fTIndex.getFidSet(fT);
			
			assert(fidSet.size() == 1);
			
			Set<BigInteger> tmpBTIndex = bf4SetBigInteger1;
			
			tmpBTIndex.clear();
						
			Map<BigInteger, BlockProperty> tmpBtrIndex = bf4MapBigIntegerBlockProperty1;
			
			tmpBtrIndex.clear();
			
			for (Long fid : fidSet) {
				List<Long> bidList = UWareDB.BidTable.get(fid);
				
				for (Long bid : bidList) {
					BlockProperty tmpBP = UWareDB.BCTRTable.get(bid);
					if (tmpBTIndex.add(tmpBP.BT)) {
						tmpBtrIndex.put(tmpBP.BT,tmpBP);
					}
				}
			}

			for (BigInteger bt : bTs) {
				BlockProperty bp = tmpBtrIndex.get(bt);
				
				assert(bp != null);
				
				btrList.add(bp);
			}
		} else { // perform similarity-based block-level dedup checking

			fidSet = rpTIndex.getTopAllFid(rpTs);

			if (fidSet.size() > 0) {
				
				fileStatus = FileStatus.NearDuplicate;
				
				Set<BigInteger>  tmpBTIndex = bf4SetBigInteger1;
				tmpBTIndex.clear();
				Map<BigInteger, BlockProperty> tmpBtrIndex = bf4MapBigIntegerBlockProperty1;
				tmpBtrIndex.clear();
				
				for (Long fid : fidSet) {
					List<Long> bidList = UWareDB.BidTable.get(fid);
					for (Long bid : bidList) {
						BlockProperty tmpBP = UWareDB.BCTRTable.get(bid);
						assert(tmpBP!=null);
						if (tmpBTIndex.add(tmpBP.BT)) {
							tmpBtrIndex.put(tmpBP.BT,tmpBP);
						}
					}
				}
				
				for (BigInteger bt : bTs) {
					BlockProperty bp = tmpBtrIndex.get(bt);

					if (bp == null) {
						if(tmpBTBuffer.containsKey(bt))
						{
							bp = tmpBTBuffer.get(bt);
						}
						else
						{
							bp = new BlockProperty();
							long newBid = UWareDedup.newBid++;
							bp.bid = newBid;
							bp.BT = bt;
							bp.R = new BigInteger(Constant.rLen, rnd);
							bp.BCT = null;
							UWareDB.BCTRTable.put(newBid, bp);
							tmpBTBuffer.put(bt,bp);
						}
					}
					btrList.add(bp);
				}
			} else {
				
				// exactly new file
				
				for (BigInteger bt : bTs) {
					
					if(tmpBTBuffer.containsKey(bt))
					{
						btrList.add(tmpBTBuffer.get(bt));
					}
					else
					{
						BlockProperty bp = new BlockProperty();
						
						long newBid = UWareDedup.newBid++;
						
						bp.bid = newBid;
						bp.BT = bt;
						bp.R = new BigInteger(Constant.rLen, rnd);
						bp.BCT = null;
						UWareDB.BCTRTable.put(newBid, bp);
						btrList.add(bp);
						tmpBTBuffer.put(bt,bp);
					}
				}
			}
		}

		initResult = new InitResult(null, fileStatus, fidSet);
		initResult.setBtrList(btrList);
		initResult.setRpTs(rpTs);

		return initResult;
	}
	
	// For small set only
	private <T> T getOneElementFromSet(Set<T> set) {
		List<T> list = new ArrayList<T>(set);
		return list.get(0);
	}
	

	public FinalResult dedupFinalCaseOneAndThree(PoWProof proof, DedupEngine dedupEngine, InitQuery initQuery, InitResult initResult) {
		FinalResult finalResult = null;

		switch (dedupEngine) {
		case FileLevel:
			finalResult = dedupFinalCaseOne(proof, initQuery, initResult);
			break;
		case SimilarityBasedDualLevel:
			finalResult = dedupFinalCaseThree(proof, initQuery, initResult);
			break;
		case PerBlockRandomness:
			finalResult = dedupFinalCaseFour(proof.getbCTs(), initQuery, initResult);
			break;
		default:
			break;
		}

		return finalResult;
	}
	

	public List<FinalResult> dedupFinalCaseTwo(PoWProof proof, DedupEngine dedupEngine, InitQuery initQuery, List<InitResult> initResults) {
		
		List<FinalResult> finalResults = new ArrayList<FinalResult>();;
		
		assert(dedupEngine == DedupEngine.BlockLevel);
		
		Map<BigInteger, Long> bCTsBuffer = bf4MapBigIntegerLong2;
		bCTsBuffer.clear();
		
		for (int i = 0; i < proof.getbCTs().size(); ++i) {
			
			BigInteger bCT = proof.getbCTs().get(i);
			
			FinalResult finalResult = null;
			
			long bid = -1;
			
			FileStatus fileStatus = initResults.get(i).getFileStatus();
			
			if (initResults.get(i).getFileStatus() == FileStatus.Fresh) {

				bid = UWareDedup.newBid++;
				
				// update the DB table
				//UWareDB.FCTTable.put(bid, bCT);
				UWareDB.FencTable.put(bid, proof.getbEncs().get(i));
				
				// update the FTIndex
				fTIndex.updateIndex(initQuery.getbTs().get(i), initResults.get(i).getR(), bid);
			} else {
				
				boolean duplicated = false;
				
				Set<Long> bidSet = fTIndex.getFidSet(initQuery.getbTs().get(i));
				
				for (Long testingBid : bidSet) {
					
					//compute BCT with rStar
					BigInteger rStar = initResults.get(i).getrStar();
					List<Byte> benc = UWareDB.FencTable.get(testingBid);
					byte[] bbytes = new byte[benc.size()];
					for(int k=0; k<benc.size(); k++)
					{
						bbytes[k] = benc.get(k);
					}
					BigInteger serverBCT = ClientExtension.genStarTag(bbytes, rStar.toByteArray());
					
					if (bCT.equals(serverBCT)) {
					//if (true) {	
						bid = testingBid;
						fileStatus = FileStatus.Duplicate;
						duplicated = true;
						break;
					}
				}
				
				
				if (!duplicated) {
					bid = UWareDedup.newBid++;
					
					// update the DB table
					//UWareDB.FCTTable.put(bid, bCT);
					UWareDB.FencTable.put(bid, proof.getbEncs().get(i));
					
					// update the FTIndex
					fTIndex.updateIndex(initQuery.getfT(), initResults.get(i).getR(), bid);
				}
				
			}
			
			finalResult = new FinalResult(bid, null, fileStatus);
			
			finalResults.add(finalResult);
		}
		
		return finalResults;
	}

	private FinalResult dedupFinalCaseOne(PoWProof proof, InitQuery initQuery, InitResult initResult) {
		
		FinalResult finalResult = null;
		
		long fid = -1;
		
		FileStatus fileStatus = initResult.getFileStatus();
		
		BigInteger fCT = proof.getfCT();
		
		if (initResult.getFileStatus() == FileStatus.Fresh) {
			
			fid = UWareDedup.newFid++;
			
			// update the DB table
			//UWareDB.FCTTable.put(fid, fCT);
			UWareDB.FencTable.put(fid, proof.getfEnc());
			
			// update the FTIndex
			fTIndex.updateIndex(initQuery.getfT(), initResult.getR(), fid);
		} else {
			
			boolean duplicated = false;
			
			Set<Long> fidSet = fTIndex.getFidSet(initQuery.getfT());
			
			for (Long testingFid : fidSet) {
				
				BigInteger rStar = initResult.getrStar();
				List<Byte> benc = UWareDB.FencTable.get(testingFid);
				byte[] fbytes = new byte[benc.size()];
				for(int k=0; k<benc.size(); k++)
				{
					fbytes[k] = benc.get(k);
				}
				BigInteger serverFCT = ClientExtension.genStarTag(fbytes, rStar.toByteArray());
				
				if (fCT.equals(serverFCT)) {
					
					fid = testingFid;
					fileStatus = FileStatus.Duplicate;
					duplicated = true;
					break;
				}
			}
			
			
			if (!duplicated) {
				fid = UWareDedup.newFid++;
				
				// update the DB table
				//UWareDB.FCTTable.put(fid, fCT);
				UWareDB.FencTable.put(fid, proof.getfEnc());
				
				// update the FTIndex
				fTIndex.updateIndex(initQuery.getfT(), initResult.getR(), fid);
			}
			
		}
		
		finalResult = new FinalResult(fid, null, fileStatus);
		
		return finalResult;
	}
	
	
	private FinalResult dedupFinalCaseThree(PoWProof proof, InitQuery initQuery, InitResult initResult) {
		
		FinalResult finalResult = null;
		
		List<BigInteger> bCTs = proof.getbCTs();
		
		long fid = -1;
		List<Long> bidList = new ArrayList<Long>();
		List<Boolean> dedupList = new ArrayList<Boolean>();
		
		FileStatus fileStatus = initResult.getFileStatus();
		
		if (initResult.getFileStatus() == FileStatus.Fresh) {
			
			fid = UWareDedup.newFid++;
			
			// update the DB table
			for (int i = 0; i < bCTs.size(); i++) {
				
				long newBid = UWareDedup.newBid++;
				
				//UWareDB.BCTTable.put(newBid, bCTs.get(i));
				UWareDB.BencTable.put(newBid, proof.getbEncs().get(i));
				
				bidList.add(newBid);
				dedupList.add(false);
			}
			
			UWareDB.BidTable.put(fid, bidList);
			
			// update the FidIndex
			fidIndex.addNewEntry(fid, initResult.getR());
			
			// update the FTIndex
			fTIndex.updateIndex(initQuery.getfT(), initResult.getR(), fid);
			
			//update the RPTIndex
			rpTIndex.add(initQuery.getRpTs(), fid);
		} else if (initResult.getFileStatus() == FileStatus.NearDuplicate) {
			
			Set<Long> fidSet = initResult.getFidSet();
			
			assert(fidSet.size() == 1);
			
			List<Long> tempBidList = null;
			
			Map<BigInteger, Long> tempBCTs = new HashMap<BigInteger, Long>();
			
			for (Long testingFid : fidSet) {
				
				tempBidList = UWareDB.BidTable.get(testingFid);
				
				for (Long oldBid : tempBidList) {
					
					BigInteger rStar = initResult.getrStar();
					List<Byte> benc = UWareDB.BencTable.get(oldBid);
					byte[] bbytes = new byte[benc.size()];
					for(int k=0; k<benc.size(); k++)
					{
						bbytes[k] = benc.get(k);
					}
					BigInteger serverBCT = ClientExtension.genStarTag(bbytes, rStar.toByteArray());
					
					//BigInteger tempBCT = UWareDB.BCTTable.get(oldBid);
					BigInteger tempBCT = serverBCT;
					
					if (tempBCT != null) {

						tempBCTs.put(tempBCT, oldBid);
					}
					
					//tempBCTs.put(UWareDB.BCTTable.get(testingBid), testingBid);
				}
			}
			
			//tempBidList.clear();
			
			Map<BigInteger, Long> bCTsBuffer = new HashMap<BigInteger, Long>();
			
			for (int i=0; i<bCTs.size(); i++) {
				
				BigInteger testingBCT = bCTs.get(i);
				
				assert(testingBCT != null);
				
				if (tempBCTs.containsKey(testingBCT)) {
					
					bidList.add(tempBCTs.get(testingBCT));
					dedupList.add(true);
					
					TestUWare.t1++;
					// TODO: for testing, I marked all the duplicated blocks' id as 0
					//bidList.add(0L);
				} else {
					
					if (!bCTsBuffer.containsKey(testingBCT)) {

						long newBid = UWareDedup.newBid++;
						//UWareDB.BCTTable.put(newBid, testingBCT);
						UWareDB.BencTable.put(newBid, proof.getbEncs().get(i));
						bidList.add(newBid);
						dedupList.add(false);
						TestUWare.t2++;
						bCTsBuffer.put(testingBCT, newBid);
					} else {
						
						bidList.add(bCTsBuffer.get(testingBCT));
						dedupList.add(true);
						// TODO: for testing, I marked all the duplicated blocks' id as 0
						//bidList.add(0L);
						TestUWare.t3++;
					}
				}
			}
			
			fid = UWareDedup.newFid++;
			
			UWareDB.BidTable.put(fid, bidList);
			
			// update the FidIndex
			fidIndex.addNewEntry(fid, initResult.getR());
			
			// update the FTIndex
			fTIndex.updateIndex(initQuery.getfT(), initResult.getR(), fid);
			
			//update the RPTIndex
			rpTIndex.add(initQuery.getRpTs(), fid);
			
			fileStatus = FileStatus.NearDuplicate;
			
		} else {
			
			fileStatus = FileStatus.Duplicate;
			
			Set<Long> fidSet = initResult.getFidSet();
			
			List<Long> tempBidList = null;
			
			assert(fidSet.size() == 1);
			
			
			Map<BigInteger, Long> tempBCTs = new HashMap<BigInteger, Long>();
			
			for (Long testingFid : fidSet) {
				
				tempBidList = UWareDB.BidTable.get(testingFid);
				
				for (Long testingBid : tempBidList) {
					
					BigInteger rStar = initResult.getrStar();
					List<Byte> benc = UWareDB.BencTable.get(testingBid);
					byte[] bbytes = new byte[benc.size()];
					for(int k=0; k<benc.size(); k++)
					{
						bbytes[k] = benc.get(k);
					}
					BigInteger serverBCT = ClientExtension.genStarTag(bbytes, rStar.toByteArray());
					
					//BigInteger tempBCT = UWareDB.BCTTable.get(testingBid);
					BigInteger tempBCT = serverBCT;
					
					if (tempBCT != null) {

						tempBCTs.put(tempBCT, testingBid);
					}
				}
			}
			
			//tempBidList.clear();
			
			Map<BigInteger, Long> bCTsBuffer = new HashMap<BigInteger, Long>();
			
			boolean freshOrNot = true;
			
			for (int i=0; i< bCTs.size(); i++) {
				
				BigInteger testingBCT = bCTs.get(i);
				
				if (tempBCTs.containsKey(testingBCT)) {
					
					freshOrNot = false;
					
					bidList.add(tempBCTs.get(testingBCT));
					dedupList.add(true);
					
				} else {
					
					// if one block has a mismatch
					fileStatus = FileStatus.NearDuplicate;
					
					if (!bCTsBuffer.containsKey(testingBCT)) {
						
						long newBid = UWareDedup.newBid++;
						//UWareDB.BCTTable.put(newBid, testingBCT);
						UWareDB.BencTable.put(newBid, proof.getbEncs().get(i));
						bidList.add(newBid);
						dedupList.add(false);
						
						bCTsBuffer.put(testingBCT, newBid);
					} else {
						
						bidList.add(bCTsBuffer.get(testingBCT));
						dedupList.add(true);
					}
				}
			}
			
			if (fileStatus != FileStatus.Duplicate) {
				
				
				fid = UWareDedup.newFid++;
				
				UWareDB.BidTable.put(fid, bidList);
				
				// update the FidIndex
				fidIndex.addNewEntry(fid, initResult.getR());
				
				// update the FTIndex
				fTIndex.updateIndex(initQuery.getfT(), initResult.getR(), fid);
				
				//update the RPTIndex
				rpTIndex.add(initQuery.getRpTs(), fid);
			}
			
			if (freshOrNot) {
				fileStatus = FileStatus.Fresh;
			}
		}
		
		finalResult = new FinalResult(fid, bidList, fileStatus);
		
		finalResult.setDedupList(dedupList);
		
		return finalResult;
	}

	private FinalResult dedupFinalCaseFour(List<BigInteger> bCTs, InitQuery initQuery, InitResult initResult) {
		
		FinalResult finalResult = null;
		
		List<BlockProperty> btrList = initResult.getBtrList();
		List<Boolean> dedupList = new ArrayList<Boolean>();
		
		long fid = -1;
		List<Long> bidList = new ArrayList<Long>();
		
		FileStatus fileStatus = initResult.getFileStatus();
		
		if (initResult.getFileStatus() == FileStatus.Fresh) {
			
			fid = UWareDedup.newFid++;
			
			// update the DB table
			for (int i = 0; i < btrList.size(); i++) {
				
				long newBid = btrList.get(i).bid;
				BlockProperty bp = UWareDB.BCTRTable.get(newBid);
				bp.BCT = bCTs.get(i);
				assert(bp.BCT != null);
				
				//UWareDB.BCTRTable.replace(newBid, bp);
				
				bidList.add(newBid);
				dedupList.add(false);
			}
			
			UWareDB.BidTable.put(fid, bidList);
			
			// update the FidIndex
			//fidIndex.addNewEntry(fid, null);
			
			// update the FTIndex
			fTIndex.updateIndex(initQuery.getfT(), null, fid);
			
			//update the RPTIndex
			rpTIndex.add(initResult.getRpTs(), fid);
		} else if (initResult.getFileStatus() == FileStatus.NearDuplicate) {
			
			Map<BigInteger, Long> tempBCTs = bf4MapBigIntegerLong1;
			tempBCTs.clear();
			
			for (BlockProperty bp : btrList) {
				
				BigInteger tempBCT = UWareDB.BCTRTable.get(bp.bid).BCT;
				
				if (tempBCT != null) {

					tempBCTs.put(tempBCT, bp.bid);
				}
			}
			
			Map<BigInteger, Long> bCTsBuffer = bf4MapBigIntegerLong2;
			bCTsBuffer.clear();
			
			for (int i = 0; i<bCTs.size(); i++){
				
				if (tempBCTs.containsKey(bCTs.get(i))) {

					bidList.add(btrList.get(i).bid);
					dedupList.add(true);
					TestUWare.t1++;

				} else {
					
					if (!bCTsBuffer.containsKey(bCTs.get(i))) {
						long newBid = btrList.get(i).bid;
						BlockProperty bp = UWareDB.BCTRTable.get(newBid);
						assert(bp.BCT == null);
						bp.BCT = bCTs.get(i);
						
						bidList.add(newBid);
						bCTsBuffer.put(bCTs.get(i), newBid);
						dedupList.add(false);
						TestUWare.t2++;
					} else {
						dedupList.add(true);
						bidList.add(bCTsBuffer.get(bCTs.get(i)));
						TestUWare.t3++;
					}
				}
			}
			
			fid = UWareDedup.newFid++;
			
			UWareDB.BidTable.put(fid, bidList);
			
			// update the FidIndex
			//fidIndex.addNewEntry(fid, null);
			
			// update the FTIndex
			fTIndex.updateIndex(initQuery.getfT(), null, fid);
			
			//update the RPTIndex
			rpTIndex.add(initResult.getRpTs(), fid);
			
			fileStatus = FileStatus.NearDuplicate;
			
		} else {
			
			fileStatus = FileStatus.Duplicate;
			
			Map<BigInteger, Long> tempBCTs = bf4MapBigIntegerLong1;
			tempBCTs.clear();
			
			for (BlockProperty bp : btrList) {
				
				BigInteger tempBCT = UWareDB.BCTRTable.get(bp.bid).BCT;
				
				if (tempBCT != null) {

					tempBCTs.put(tempBCT, bp.bid);
				}
			}
			
			Map<BigInteger, Long> bCTsBuffer = bf4MapBigIntegerLong2;
			bCTsBuffer.clear();
			
			boolean freshOrNot = true;
			
			for (int i = 0; i<bCTs.size(); i++) {
				
				if (tempBCTs.containsKey(bCTs.get(i))) {
					
					freshOrNot = false;
					
					dedupList.add(true);
					bidList.add(btrList.get(i).bid);
				} else {
					
					// if one block has a mismatch
					assert(false);
					fileStatus = FileStatus.NearDuplicate;
					
					if (!bCTsBuffer.containsKey(bCTs.get(i))) {
						long newBid = btrList.get(i).bid;
						BlockProperty bp = UWareDB.BCTRTable.get(newBid);
						assert(bp.BCT == null);
						bp.BCT = bCTs.get(i);
						//UWareDB.BCTRTable.replace(newBid, bp);

						bidList.add(newBid);
						dedupList.add(false);
						
						bCTsBuffer.put(bCTs.get(i), newBid);
					} else {		
						dedupList.add(true);
						bidList.add(bCTsBuffer.get(bCTs.get(i)));
					}
				}
			}
			
			if (fileStatus != FileStatus.Duplicate) {
				
				fid = UWareDedup.newFid++;
				
				UWareDB.BidTable.put(fid, bidList);
				
				// update the FidIndex
				//fidIndex.addNewEntry(fid, null);
				
				// update the FTIndex
				fTIndex.updateIndex(initQuery.getfT(), null, fid);
				
				//update the RPTIndex
				rpTIndex.add(initResult.getRpTs(), fid);
			}
			
			if (freshOrNot) {
				fileStatus = FileStatus.Fresh;
				assert(false);
			}
		}
		
		finalResult = new FinalResult(fid, bidList, fileStatus);
		finalResult.setDedupList(dedupList);
		return finalResult;
	}


}


