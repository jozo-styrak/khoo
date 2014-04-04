package patternMatcher.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import patternMatcher.demo.patterns.PatternFactory;
import patternMatcher.demo.wrappers.TextWrapper;

/*
 * Main class for running relation extraction
 * reads files from specified folder and outputs results
 * 
 * -f from what directory
 * -t where to output
 * -m if it is man format
 */
public class ExtractRelations {
	
	public static void main(String[] args) throws IOException {

		String directoryFrom = "";
		String directoryOutput  = "";
		boolean isManFormat = false;
		
		try {
			
			for (int i = 0; i < args.length; i++) {
				if (args[i].compareTo("-f") == 0) {
					directoryFrom = args[i+1];
				} else if (args[i].compareTo("-t") == 0) {
					directoryOutput = args[i+1];
				} else if (args[i].compareTo("-m") == 0) {
					isManFormat = true;
				} //else throw new Exception("Unknown paramater " + args[i]); 
			}
			
			ExtractRelations.extractRelations(directoryFrom, (directoryOutput.endsWith("/")) ? directoryOutput : directoryOutput + "/", isManFormat);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		

	}
	
	public static void extractRelations(String fromFolder, String outputFolder, boolean isManFormat) throws IOException {
		List<File> FILES = new ArrayList<File>();
		File folder = new File(fromFolder);//new File("data/aviation_reports/files/");
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			FILES.add(files[i]);
		}
		
		// load properties from file
		Properties properties = new Properties();
		try {
			properties.load(new BufferedInputStream(new FileInputStream("settings/settings.prop")));
		} catch (IOException e) {
			System.out.println("Settings file not found!");
			throw new IOException("Error loading system settings");
		}
		
		PatternFactory e1Patterns = new PatternFactory();
//		e1Patterns.loadSubpatterns("data/subpatterns/e1.e2.subpatterns.data");
//		e1Patterns.loadVerbGroups("data/verb_groups/verb.groups.e1.e2.conj.data");
//		e1Patterns.loadPatternsDefinitions("data/patterns/e1.patterns.edited.data");
		e1Patterns.loadSubpatterns(properties.getProperty("e1.e2.subpatterns.file"));
		e1Patterns.loadVerbGroups(properties.getProperty("e1.e2.verbgroups.file"));
		e1Patterns.loadPatternsDefinitions(properties.getProperty("e1.patterns.file"));
		
		PatternFactory e2Patterns = new PatternFactory();
//		e2Patterns.loadSubpatterns("data/subpatterns/e1.e2.subpatterns.data");
//		e2Patterns.loadVerbGroups("data/verb_groups/verb.groups.e1.e2.conj.data");
//		e2Patterns.loadPatternsDefinitions("data/patterns/e2.patterns.data");
		e2Patterns.loadSubpatterns(properties.getProperty("e1.e2.subpatterns.file"));
		e2Patterns.loadVerbGroups(properties.getProperty("e1.e2.verbgroups.file"));
		e2Patterns.loadPatternsDefinitions(properties.getProperty("e2.patterns.file"));
		
		PatternFactory e3Patterns = new PatternFactory();
//		e3Patterns.loadSubpatterns("data/subpatterns/e3.subpatterns.edited.data");
//		e3Patterns.loadVerbGroups("data/verb_groups/verb.groups.e3.ver03.data");
//		e3Patterns.loadPatternsDefinitions("data/patterns/e3.patterns.all.data");
		e3Patterns.loadSubpatterns(properties.getProperty("e3.subpatterns.file"));
		e3Patterns.loadVerbGroups(properties.getProperty("e3.verbgroups.file"));
		e3Patterns.loadPatternsDefinitions(properties.getProperty("e3.patterns.file"));
		
		long startTime = System.currentTimeMillis();
		
		for (File file : FILES) {
		
			if (file.isFile()) {
				try {
					long fileStartTime = System.currentTimeMillis();
					TextWrapper text = new TextWrapper();
					String filename = file.getName().substring(0, file.getName().indexOf("."));
					System.out.println("Processing file \'"+file.getName()+"\'");
					text.loadAndParseTextFile(file, isManFormat);
					text.setOutputStream(outputFolder +filename+".txt");
					text.setDebugOutputStream(outputFolder + filename + "-debug.txt");

					System.out.println("Extracting E2 patterns");
					long checkpoint = System.currentTimeMillis();
					text.extractRelationsFromSentencePairs(e2Patterns);
					System.out.println("Extraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
					
					System.out.println("Extracting E1 patterns");
					checkpoint = System.currentTimeMillis();
					text.extractRelationsLazy(e1Patterns);
					System.out.println("Extraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
					
					System.out.println("Extracting E3 patterns");
					checkpoint = System.currentTimeMillis();
					text.extractRelationsLazy(e3Patterns);
					System.out.println("Extraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
					
					System.out.println("Total duration for this file: " + String.format("%.2f", (System.currentTimeMillis()-fileStartTime)/1000.0/60.0) + " min\n");
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// output usage data
		PrintStream out = new PrintStream(new File(outputFolder + "usage_data.txt"));
		System.out.println("----------- Printing e1 pattern usage -----------");
		e1Patterns.printUsageData(System.out);
		System.out.println("----------- Printing e2 pattern usage -----------");
		e2Patterns.printUsageData(System.out);
		System.out.println("----------- Printing e3 pattern usage -----------");
		e3Patterns.printUsageData(System.out);
		
		out.println("----------- Printing e1 pattern usage -----------");
		e1Patterns.printUsageData(out);
		out.println("----------- Printing e2 pattern usage -----------");
		e2Patterns.printUsageData(out);
		out.println("----------- Printing e3 pattern usage -----------");
		e3Patterns.printUsageData(out);
		
		out.close();
		
		System.out.println("\nTotal duration: " + String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0/60.0) + " min");	
	}

}
