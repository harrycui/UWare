package base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import framework.ClientExtension;

public class FakeFile implements UserFile {

	public FakeFile(String name, long fileSize, List<FileBlock> blocks, BigInteger fileHash) {
		this.fileName = name;
		this.fileSize = fileSize;
		this.blocks = new ArrayList<FileBlock>(blocks);
		this.fT = fileHash;
	}

	@Override
	public Iterator<FileBlock> blockIterator() {
		return blocks.iterator();
	}

	@Override
	public int numOfBlock() {
		return blocks.size();
	}

	@Override
	public String fileName() {
		return fileName;
	}

	@Override
	public long fileSize() {
		return fileSize;
	}

	@Override
	public BigInteger getfT() {
		return fT;
	}
	
	@Override
	public byte[] getData() {
		
		return fT.toByteArray();
	}
	
	private BigInteger fT;
	
	private List<FileBlock> blocks;
	
	private String fileName;
	
	//size in bytes
	private long fileSize;
}
