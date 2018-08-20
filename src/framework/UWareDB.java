package framework;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.BlockProperty;

public class UWareDB {

	public static Map<Long, List<Byte>> FencTable = new HashMap<Long, List<Byte>>();
	//public static Map<Long, BigInteger> FCTTable = new HashMap<Long, BigInteger>();
	
	public static Map<Long, List<Long>> BidTable = new HashMap<Long, List<Long>>();
	
	public static Map<Long, List<Byte>> BencTable = new HashMap<Long, List<Byte>>();
	//public static Map<Long, BigInteger> BCTTable = new HashMap<Long, BigInteger>();
	
	public static Map<Long, BlockProperty> BCTRTable = new HashMap<Long, BlockProperty>();
}
