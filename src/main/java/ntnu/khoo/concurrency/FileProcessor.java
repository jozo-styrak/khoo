package ntnu.khoo.concurrency;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import ntnu.khoo.patterns.PatternFactory;
import ntnu.khoo.patterns.output.BratOutput;
import ntnu.khoo.wrappers.TextWrapper;

public class FileProcessor implements Runnable {

	private TextWrapper text;
	private List<PatternFactory> patterns;
	private File inputFile;
	private String outputFolder;
	private LexicalizedParser parser;
	
	public FileProcessor(LexicalizedParser parser, File file, List<PatternFactory> patterns, String outputFolder) {
//		this.text = new TextWrapper(parser);
		this.parser = parser;
		this.inputFile = file;
		this.patterns = patterns;
		this.outputFolder = outputFolder;
	}
	
	@Override
	public void run() {
		
		this.text = new TextWrapper(parser);
		String filename = this.inputFile.getName().substring(0, this.inputFile.getName().indexOf("."));
		text.setOutputPrefix(filename + "\t");
		
		// process file
		try {
			
			long fileStartTime = System.currentTimeMillis();
			
			System.out.println(filename + "\tProcessing file \'"+this.inputFile.getName()+"\'");
//			text.loadAndParseTextFile(this.inputFile, true);
			text.loadAndParseTextFileWithPositions(this.inputFile);
//			text.setOutputStream(outputFolder +filename+".txt");
			text.addOutputStream(new BratOutput(new PrintStream(new File(outputFolder +filename+".txt"))));
			//text.setDebugOutputStream(outputFolder + filename + "-debug.txt");
			
			System.out.println(filename + "\tExtracting E2 patterns");
			long checkpoint = System.currentTimeMillis();
			text.extractRelationsFromSentencePairs(patterns.get(0));
			System.out.println(filename + "\tExtraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
			
			System.out.println(filename + "\tExtracting E1 patterns");
			checkpoint = System.currentTimeMillis();
			text.extractRelationsLazy(patterns.get(1));
			System.out.println(filename + "\tExtraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
			
			System.out.println(filename + "\tExtracting E3 patterns");
			checkpoint = System.currentTimeMillis();
			text.extractRelationsLazy(patterns.get(2));
			System.out.println(filename + "\tExtraction duration: " + String.format("%.2f", (System.currentTimeMillis()-checkpoint)/1000.0/60.0) + " min");
			
			System.out.println(filename + "\tTotal duration for this file: " + String.format("%.2f", (System.currentTimeMillis()-fileStartTime)/1000.0/60.0) + " min\n");
			
		} catch (IOException e) {
			System.out.println(filename + "\tFile input error");
		}
	}

}
