package ntnu.khoo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ntnu.khoo.concurrency.FileProcessor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import ntnu.khoo.patterns.PatternFactory;

/*
 * Main class for running relation extraction
 * reads files from specified folder and outputs results
 * 
 * -f from what directory
 * -t where to output
 * -n number of threads, default 1
 */
public class ExtractRelations {
	
	public static void main(String[] args) throws IOException {

		String directoryFrom = "";
		String directoryOutput  = "";
		boolean isManFormat = true;
		int numberOfThreads = 1;
		
		try {
			
			for (int i = 0; i < args.length; i++) {
				if (args[i].compareTo("-f") == 0) {
					directoryFrom = args[i+1];
				} else if (args[i].compareTo("-t") == 0) {
					directoryOutput = args[i+1];
				} else if (args[i].compareTo("-n") == 0) {
					numberOfThreads = Integer.parseInt(args[i+1]);
				}
//				} else throw new Exception("Unknown paramater " + args[i]); 
			}
			
			ExtractRelations.extractRelationsParallelized(numberOfThreads, directoryFrom, (directoryOutput.endsWith("/")) ? directoryOutput : directoryOutput + "/", isManFormat);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		

	}
	
	public static void extractRelationsParallelized(int numberOfThreads, String fromFolder, String outputFolder, boolean isManFormat) throws IOException {
		List<File> FILES = new ArrayList<File>();
		File folder = new File(fromFolder);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			FILES.add(files[i]);
		}
		
		// load properties with names of pattern files
		Properties properties = new Properties();
		try {
			properties.load(new BufferedInputStream(new FileInputStream("settings/settings.prop")));
		} catch (IOException e) {
			System.out.println("Settings file not found!");
			throw new IOException("Error loading system settings");
		}
		
		// create object container for pattern factories
		List<PatternFactory> patterns = new ArrayList<PatternFactory>();
		
		PatternFactory e1Patterns = new PatternFactory();
		e1Patterns.loadSubpatterns(properties.getProperty("e1.e2.subpatterns.file"));
		e1Patterns.loadVerbGroups(properties.getProperty("e1.e2.verbgroups.file"));
		e1Patterns.loadPatternsDefinitions(properties.getProperty("e1.patterns.file"));
		
		PatternFactory e2Patterns = new PatternFactory();
		e2Patterns.loadSubpatterns(properties.getProperty("e1.e2.subpatterns.file"));
		e2Patterns.loadVerbGroups(properties.getProperty("e1.e2.verbgroups.file"));
		e2Patterns.loadPatternsDefinitions(properties.getProperty("e2.patterns.file"));
		
		PatternFactory e3Patterns = new PatternFactory();
		e3Patterns.loadSubpatterns(properties.getProperty("e3.subpatterns.file"));
		e3Patterns.loadVerbGroups(properties.getProperty("e3.verbgroups.file"));
		e3Patterns.loadPatternsDefinitions(properties.getProperty("e3.patterns.file"));
		
		patterns.add(e2Patterns);
		patterns.add(e1Patterns);
		patterns.add(e3Patterns);
		
		long startTime = System.currentTimeMillis();
		
		// load parser
		LexicalizedParser parser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		
		// create executor service
		// each file is processed within own thread
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		System.out.println("\n***** PROCESSING " + numberOfThreads + " THREADS CONCURRENTLY *****\n");
		
		for (File file : FILES) {
		
			if (file.isFile()) {
				try {
					Runnable processFile = new FileProcessor(parser, file, patterns, outputFolder);
					executor.execute(processFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}				

		}
			
		executor.shutdown();
		while (!executor.isTerminated()) {}
			
		// output usage data
		// used for testing purposes
//		PrintStream out = new PrintStream(new File(outputFolder + "usage_data.txt"));
//		System.out.println("----------- Printing e1 pattern usage -----------");
//		e1Patterns.printUsageData(System.out);
//		System.out.println("----------- Printing e2 pattern usage -----------");
//		e2Patterns.printUsageData(System.out);
//		System.out.println("----------- Printing e3 pattern usage -----------");
//		e3Patterns.printUsageData(System.out);
//		
//		out.println("----------- Printing e1 pattern usage -----------");
//		e1Patterns.printUsageData(out);
//		out.println("----------- Printing e2 pattern usage -----------");
//		e2Patterns.printUsageData(out);
//		out.println("----------- Printing e3 pattern usage -----------");
//		e3Patterns.printUsageData(out);
		
//		out.close();
		
		System.out.println("\nTotal duration: " + String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0/60.0) + " min");	
	}

}
