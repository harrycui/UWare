package base;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Snapshots {

	private long blockSize;

	private String filedate;

	private String fileName;

	private List<UserFile> files;

	private long fileCount;

	public Snapshots(Path filePath)
	{
		try
		{	
			// Buffer file to stream
			Stream<String> sstream = Files.lines(filePath);
			Iterator<String> iter = sstream.iterator();

			files = new ArrayList<UserFile>();

			this.fileName = filePath.getFileName().toString();

			fileCount = 0;

			blockSize = 4;
			filedate = "";

			String filename=null;
			long filesize=0;
			long blocksize=0;
			String[] tmpstrs=null;
			BigInteger filehash=null;
			List<FileBlock> fileblocks=null;


			// main file reading loop
			while(iter.hasNext())
			{
				String str = iter.next();
				if (str.startsWith("File path: "))
				{
					filename = str.substring(11);
				}
				else if (str.startsWith("Chunk Hash"))
				{
					List<FileBlock> blocks = new ArrayList<FileBlock>();
					while (iter.hasNext())
					{
						str = iter.next();
						if (str.equals(""))
							break;
						tmpstrs = str.split("\t");
						String blockHashStr = tmpstrs[0].replace(":","");
						blocksize = Integer.parseInt(tmpstrs[2].trim());
						blocks.add(new FakeBlock(new BigInteger(blockHashStr, 16), blocksize));
					}
					fileblocks=blocks;
				}
				else if (str.startsWith("Whole File Hash: "))
				{
					str=str.substring(17);
					filehash = new BigInteger(str, 16);
					filesize = 0;
					for (FileBlock b : fileblocks)
						filesize+=b.blockSize();
					files.add(new FakeFile(filename,filesize,fileblocks,filehash));
				}

			}

			sstream.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}

	public String getFiledate() {
		return filedate;
	}

	public void setFiledate(String filedate) {
		this.filedate = filedate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<UserFile> getFiles() {
		return files;
	}

	public void setFiles(List<UserFile> files) {
		this.files = files;
	}

	public long getFileCount() {
		return fileCount;
	}

	public void setFileCount(long fileCount) {
		this.fileCount = fileCount;
	}

}
