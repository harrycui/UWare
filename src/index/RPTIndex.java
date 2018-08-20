package index;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import test.Simulator;

public class RPTIndex {
	public RPTIndex() {
		index = new HashMap<BigInteger, Set<Long>>();
	}

	public Set<Long> getTopOneFid(Set<BigInteger> rpTSet) {
		// Push fid lists of all RPTags to a multiset
		MultiSet<Long> similarFidBag = new HashMultiSet<Long>();
		for (BigInteger rpT : rpTSet) {
			Set<Long> fidSet = index.get(rpT);
			if (fidSet != null)
				similarFidBag.addAll(fidSet);
		}

		if (similarFidBag.isEmpty()) {
			return null;
		} else {
			// Sort fids by their occurrence in the multiset
			SortedMap<Integer, Set<Long>> countFidMap = new TreeMap<Integer, Set<Long>>();
			for (MultiSet.Entry<Long> fidCntEntry : similarFidBag.entrySet()) {
				long fid = fidCntEntry.getElement();
				int count = fidCntEntry.getCount();
				if (!countFidMap.containsKey(count))
					countFidMap.put(count, new HashSet<Long>());
				countFidMap.get(count).add(fid);
			}
			
			Simulator.numOfCandidate = countFidMap.size();

			// Return the fids with highest count
			return countFidMap.get(countFidMap.lastKey());

		}
	}
	
	public Set<Long> getTopKFid(Set<BigInteger> rpTSet) {
		// Push fid lists of all RPTags to a multiset
		MultiSet<Long> similarFidBag = new HashMultiSet<Long>();
		Set<Long> TopKSet = new HashSet<Long>();
		for (BigInteger rpT : rpTSet) {
			Set<Long> fidSet = index.get(rpT);
			if (fidSet != null)
				similarFidBag.addAll(fidSet);
		}

		if (similarFidBag.isEmpty()) {
			return TopKSet;
		} else {
			// Sort fids by their occurrence in the multiset
			SortedMap<Integer, Set<Long>> countFidMap = new TreeMap<Integer, Set<Long>>();
			for (MultiSet.Entry<Long> fidCntEntry : similarFidBag.entrySet()) {
				long fid = fidCntEntry.getElement();
				int count = fidCntEntry.getCount();
				if (!countFidMap.containsKey(count))
					countFidMap.put(count, new HashSet<Long>());
				countFidMap.get(count).add(fid);
			}
			Simulator.numOfCandidate = countFidMap.size();
			TopKSet=countFidMap.get(countFidMap.lastKey());
			return TopKSet;
		}
	}
	
	public Set<Long> getTopAllFid(Set<BigInteger> rpTSet) {
		// Push fid lists of all RPTags to a multiset
		Set<Long> similarFidBag = new HashSet<Long>();
		for (BigInteger rpT : rpTSet) {
			Set<Long> fidSet = index.get(rpT);
			if (fidSet != null)
				similarFidBag.addAll(fidSet);
		}
		
		return similarFidBag;
	}

	public void add(Set<BigInteger> rpTSet, long fid) {
		for (BigInteger rpT : rpTSet) {
			if (!index.containsKey(rpT))
				index.put(rpT, new HashSet<Long>());
			index.get(rpT).add(fid);
		}
	}

	private Map<BigInteger, Set<Long>> index;
	
	public long getNumOfRPTs() {
		return index.size();
	}
	
	public long getNumOfFids() {
		
		long numOfFids = 0;
		
		for (Set<Long> fidSet : index.values()) {
			numOfFids += fidSet.size();
		}
		
		return numOfFids;
	}
}
