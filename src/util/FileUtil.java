package util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import base.File;
import base.GenuineBlock;
import base.GenuineFile;
import base.UserFile;

public class FileUtil {

	public static File readBlocksFromFile(Path filePath, int R, long fileID, long blockID) {
		try
		{	
			// Buffer file to stream
			Stream<String> sstream = Files.lines(filePath);
			Iterator<String> iter = sstream.iterator();
			
			// ignore the first header line
			iter.next();
			
			// build block list to construct UserFile
			List<GenuineBlock> blocks = new ArrayList<GenuineBlock>();
			
			// main file reading loop
			while(iter.hasNext())
			{
				// extract raw hash
				String rawHash = iter.next().split("\t")[0].replace(":","");
				
				// add new block to list
				blocks.add(new GenuineBlock(new BigInteger(rawHash, 16).toByteArray()));
			}
			
			sstream.close();
			
			File uFile = new UserFile(blocks, filePath.toString());
			
			return uFile;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
