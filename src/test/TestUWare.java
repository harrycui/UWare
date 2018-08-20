package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import base.ConfigParser;
import base.Constant;
import base.Counter;
import base.Constant.DedupEngine;
import base.Constant.SamplingMethod;
import base.Snapshots;
import base.UserFile;
import framework.UWareDedup;
import framework.User;

public class TestUWare {
	
	
	public static int t1 = 0;
	
	public static int t2 = 0;
	
	public static int t3 = 0;

	public static void main(String args[]) {

		if (args.length < 1) {

			System.err.println("Error: no argument provided!");

			System.exit(Constant.ERROR_ARGUMENTS);
		} else {
			System.out.println("Config file is " + args[0]);
		}
		

		ConfigParser config = new ConfigParser(args[0]);

		// initialization
		String rootPath = config.getString("rootPath").replace("\\", "/");
		System.out.println("rootPath is " + rootPath);

		String testFilePath = rootPath + config.getString("testFilePath");
		System.out.println("testFilePath is " + testFilePath);

		String outFilePath = rootPath + config.getString("outFilePath");
		System.out.println("outFilePath is " + outFilePath);

		int samplingMethod = config.getInt("samplingMethod");
		int dedupEngine = config.getInt("dedupEngine");
		
		long checkFrequence = config.getLong("checkFrequence");
		
		int testingNum = config.getInt("testingNum");
		

		for (int i = 0; i < Simulator.blockNum.length; i++) {
			List<Long> totalUWareTimeList = new ArrayList<>(testingNum);
			List<Long> totalUWareCandidateNumList = new ArrayList<>(testingNum);

			Simulator.UWareTimes.add(totalUWareTimeList);
			Simulator.UWareCandidateNum.add(totalUWareCandidateNumList)
;		}
		
//		if (dedupEngine != 1) {
//			checkFrequence *= 85; // extend its frequence
//		}

		// sampling ratio
		int R = config.getInt("R");

		// int topK = config.getInt("topK");
		
		List<Counter> checkPoints = new ArrayList<>(4);
		
		long count = 0;
		
		long preClientSideTime = 0;
		long preUWareTime = 0;

		File writename = null;
		PrintStream out = null;
		Path folder = Paths.get(testFilePath);

		try {
			writename = new File(outFilePath + "output-case-" + dedupEngine + "-R-" + R + "-method-" + samplingMethod + "-" + checkFrequence + ".csv");
			writename.createNewFile();
			out = new PrintStream(new FileOutputStream(writename));

			List<Path> list;
			list = Files.walk(folder).collect(Collectors.toList());

			int userId = 1;

			User user = new User(userId, R, DedupEngine.values()[dedupEngine - 1],
					SamplingMethod.values()[samplingMethod - 1]);

			UWareDedup dedup = new UWareDedup();
			
			if (user.getEngine() == DedupEngine.FileLevel) {
			
				for (int f = 1; f < list.size(); ++f) {

					// System.out.println(list.get(f).getFileName());
					SoftReference<Snapshots> snap = new SoftReference<Snapshots>(new Snapshots(list.get(f)));

					System.out.println("Processing the DB file -> " + snap.get().getFileName());

					SoftReference<List<UserFile>> files = new SoftReference<List<UserFile>>(snap.get().getFiles());

					for (int i = 0; i < files.get().size(); ++i) {
						
						UserFile file = files.get().get(i);

						Simulator.uploadFileCaseOneAndThree(user, dedup, file);
						
						if (++count == checkFrequence) {
							
							checkPoints.add(new Counter(Simulator.counter, preClientSideTime, preUWareTime));
							
							preClientSideTime = Simulator.counter.totalClientSideTime;
							preUWareTime = Simulator.counter.totalUWareTime;
							
							count = 0;
						}
					}
				}
			} else if (user.getEngine() == DedupEngine.SimilarityBasedDualLevel) {
				
				for (int f = 1; f < list.size(); ++f) {

					// System.out.println(list.get(f).getFileName());
					SoftReference<Snapshots> snap = new SoftReference<Snapshots>(new Snapshots(list.get(f)));

					System.out.println("Processing the DB file -> " + snap.get().getFileName());

					SoftReference<List<UserFile>> files = new SoftReference<List<UserFile>>(snap.get().getFiles());

					for (int i = 0; i < files.get().size(); ++i) {
						
						UserFile file = files.get().get(i);

						Simulator.uploadFileCaseOneAndThree(user, dedup, file);
						
						if (++count == checkFrequence) {
							
							Simulator.counter.totalRPTCount = dedup.getRpTIndex().getNumOfRPTs();

							Simulator.counter.totalFidInRPTIndexCount = dedup.getRpTIndex().getNumOfFids();
							
							checkPoints.add(new Counter(Simulator.counter, preClientSideTime, preUWareTime));
							
							preClientSideTime = Simulator.counter.totalClientSideTime;
							preUWareTime = Simulator.counter.totalUWareTime;
							
							count = 0;
						}
					}
				}
			} else if (user.getEngine() == DedupEngine.BlockLevel) {
				
				for (int f = 1; f < list.size(); ++f) {

					SoftReference<Snapshots> snap = new SoftReference<Snapshots>(new Snapshots(list.get(f)));

					System.out.println("Processing the DB file -> " + snap.get().getFileName());

					SoftReference<List<UserFile>> files = new SoftReference<List<UserFile>>(snap.get().getFiles());

					for (int i = 0; i < files.get().size(); ++i) {
						
						UserFile file = files.get().get(i);

						Simulator.uploadFileCaseTwo(user, dedup, file);

						if (++count == checkFrequence) {
							
							checkPoints.add(new Counter(Simulator.counter, preClientSideTime, preUWareTime));
							
							preClientSideTime = Simulator.counter.totalClientSideTime;
							preUWareTime = Simulator.counter.totalUWareTime;
							
							count = 0;
						}
					}
					
					System.gc();
				}
			} else if (user.getEngine() == DedupEngine.PerBlockRandomness) {
				for (int f = 1; f < list.size(); ++f) {

					SoftReference<Snapshots> snap = new SoftReference<Snapshots>(new Snapshots(list.get(f)));

					System.out.println("Processing the DB file -> " + snap.get().getFileName());

					SoftReference<List<UserFile>> files = new SoftReference<List<UserFile>>(snap.get().getFiles());

					for (int i = 0; i < files.get().size(); ++i) {
						
						UserFile file = files.get().get(i);

						Simulator.uploadFileCaseFour(user, dedup, file);
						
						if (++count == checkFrequence) {
							
							Simulator.counter.totalRPTCount = dedup.getRpTIndex().getNumOfRPTs();

							Simulator.counter.totalFidInRPTIndexCount = dedup.getRpTIndex().getNumOfFids();
							
							checkPoints.add(new Counter(Simulator.counter, preClientSideTime, preUWareTime));
							
							preClientSideTime = Simulator.counter.totalClientSideTime;
							preUWareTime = Simulator.counter.totalUWareTime;
							
							count = 0;
						}
					}
				}
			}
			
			if (count != 0) {
				checkPoints.add(new Counter(Simulator.counter, preClientSideTime, preUWareTime));
			}

			String outTitle = "num of files, unique files, duplicated files, " + 
					"near-duplicated files, num of blocks, unique blocks, " + 
					"duplicated blocks, total file size, unique block(file) size, " + 
					"deduplicate block size, num of rpT, num of fid in RPTIndex, " +
					"total client time, total UWare time, cum client time, cum UWare time, dedup ratio";
			
			System.out.println(outTitle);
			out.println(outTitle);
			
			for (Counter c : checkPoints) {
				
				String outStr = c.toString();
				
				System.out.println(outStr);
				
				out.println(outStr);
			}

			
			
			// for CDF output
			
			for (int i = 0; i < Simulator.UWareTimes.size(); i++) {
				
				System.out.println("For file with less " + Simulator.blockNum[i] + " blocks: total num is " + Simulator.UWareTimes.get(i).size());
				
				
				assert(Simulator.UWareTimes.get(i).size() == Simulator.UWareCandidateNum.get(i).size());
				
				if (Simulator.UWareTimes.get(i).size() == 0) {
					continue;
				}
				
				int c = 1;
				
				long avg = 0;
				
				for (int j = 0; j < Simulator.UWareTimes.get(i).size(); j++) {
					
					long tempTime = Simulator.UWareTimes.get(i).get(j);
					
					if (c <= testingNum) {
						System.out.print(tempTime + ";");
					}
					avg += tempTime;
					c++;
				}
				
				System.out.println("\nCandidates num --> ");
				
				for (int j = 0; j < testingNum && j < Simulator.UWareCandidateNum.get(i).size(); j++) {
					
					System.out.print(Simulator.UWareCandidateNum.get(i).get(j) + ";");
				}
				
				avg = avg / Simulator.UWareTimes.get(i).size();
				
				out.println(Simulator.blockNum[i] + "," + avg);
				System.out.println("\nAverage time of file with less " + Simulator.blockNum[i] + " blocks is " + avg + " ns");
			}
			
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.err.println(""+t1+"  "+t2+"  "+t3);
	}
}
