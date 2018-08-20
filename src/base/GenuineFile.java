package base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GenuineFile implements UserFile {
	
	public GenuineFile(List<FileBlock> blocks, String fileName) {
		
		this.blocks = new ArrayList<FileBlock>(blocks);
		
		this.fileName = fileName;
		
		fileSize = 0;
		for(FileBlock block : blocks)
			fileSize += block.blockSize();
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
		
		int blockSize = blocks.get(0).getData().length;
		
		byte[] fData = new byte[blocks.size()*blockSize];
		
		for (int i = 0; i < blocks.size(); i++) {
			System.arraycopy(blocks.get(i).getData(), 0, fData, i*blockSize, blockSize);
		}
		
		return fData;
	}
	
	private List<FileBlock> blocks;
	
	private String fileName;
	
	private long fileSize;
	
	BigInteger fT;
}
