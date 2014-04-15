package ntnu.khoo.wrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ntnu.khoo.patterns.Pattern;
import ntnu.khoo.patterns.PatternFactory;
import ntnu.khoo.patterns.PatternMatcher;
import ntnu.khoo.patterns.token.WildcardToken;
import ntnu.khoo.utils.TreeUtils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/*
 * holds list of sentences for relation extraction
 */
public class TextWrapper {
	
	private LexicalizedParser parser;
	private List<String> sentences;
	private List<Tree> parseTrees;
	private StanfordCoreNLP pipeline;
	
	// output streams
	private PrintStream fileStream;
	private PrintStream debugFileStream;
	
	public TextWrapper() {
		this.parser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		this.sentences = new ArrayList<>();
		this.parseTrees = new ArrayList<>();
		
		// annotator used for sentence splitting
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    this.pipeline = new StanfordCoreNLP(props);
	    this.fileStream = null;
	    this.debugFileStream = null;
	}
	
	// remove man annotation
	// currently just strips brackets and labels
//	public String processManFormat(String line) {
//		return line.replaceAll("\\(", "").replaceAll("\\[", "").replaceAll("<", "").replaceAll("\\]C", "").replaceAll("\\]T", "").replaceAll("\\]E", "").replaceAll("\\{", "").replaceAll("\\)C", "").replaceAll("\\)T", "").replaceAll("\\)E", "").replaceAll("\\}C", "").replaceAll("\\}T", "").replaceAll("\\}E", "").replaceAll(">C", "").replaceAll(">E", "").replaceAll(">T", "").replaceAll("\\]", "").replaceAll("\\}", "").replaceAll("\\)", "").replaceAll("\\]", "").replaceAll("\\}", "");
//	}
	// adjusted method for new annotation
	public String processManFormat(String line) {
		return line.replaceAll("\\[", "").replaceAll("\\][CTE][0-9]*", "");
	}
	
	public void loadAndParseTextFile(File file, boolean isManFormat) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		loadAndParseTextFile(reader, isManFormat);
	}
	
	public void loadAndParseTextFile(String file, boolean isManFormat) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		loadAndParseTextFile(reader, isManFormat);
	} 
	
	// read file and parse sentences
	public void loadAndParseTextFile(BufferedReader reader, boolean isManFormat) throws IOException {
		String line;
		
		while ((line = reader.readLine()) != null) {
			// comment allowed in text
			if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
				line = (isManFormat) ? this.processManFormat(line.replaceAll("\"", "")) : line.replaceAll("\"", "").trim();
//				System.out.println(line);
			    Annotation lineAnnotation = new Annotation(line);
			    pipeline.annotate(lineAnnotation);
			    List<CoreMap> sentenceMap = lineAnnotation.get(SentencesAnnotation.class);
			    
			    for(CoreMap s: sentenceMap) {
			    	sentences.add(s.toString());
			    }
			}
		}
		
		System.out.println("Read "+this.sentences.size()+" sentences");
		System.out.println("Parsing sentences...");
		long startTime = System.currentTimeMillis();
		this.parseSentences();
		System.out.println("Parsing done in " + String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0) + " sec");
		reader.close();
	}
	
	public List<String> getSentences() {
		return this.sentences;
	}
	
	// for testing
	public void addSentence(String s) {
		this.sentences.add(s.replaceAll("\"", ""));
	}
	
	// lazy extraction - apply sequentaly patterns supplied by given patternFactory
	public void extractRelationsLazy(PatternFactory patternFactory) {
		
		List<String> patterns = patternFactory.getPatternsDefinitions();
		System.out.println("Number of patterns: " + patterns.size());
		//for (String patternString : patterns) {
		//	System.out.println("->" + patternString);
		//}
		
		int count = (this.parseTrees.size() > 0) ? this.parseTrees.size() : this.sentences.size();
		
		for (int i = 0; i < count; i++) {
			
//			System.out.println("\nSentence: " + this.sentences.get(i));
			if (debugFileStream != null) debugFileStream.println("\nSentence: " + this.sentences.get(i));
			//if (fileStream != null) fileStream.println("\nSentence: " + s);
			SentenceWrapper sentence = (this.parseTrees.size() > 0) ? new SentenceWrapper(this.parseTrees.get(i), this.parser) : new SentenceWrapper(this.sentences.get(i), this.parser);
			//sentence.printProcessOrder();
			
			for (String patternDefinition : patterns) {
				
//				System.out.println("Pattern: " + patternDefinition);
				try {
					Pattern pattern = patternFactory.createPattern(patternDefinition.startsWith("C"), patternDefinition.substring(2).trim());
					pattern.returnToken(new WildcardToken("***"));
					
					if (pattern.useThisPattern(sentence)) {
					
						while (PatternMatcher.tryPattern(sentence, pattern)) {
//							System.out.println("Matched Pattern: " + pattern.getPatternString());
							pattern.printCausalInformation(System.out);
//							System.out.println("Verbs: " + pattern.getVerbString());
//							pattern.printCausalInformationConjunctions(System.out);
//							System.out.println(pattern.getMatches());
							
							pattern.flag(sentence);
							if (fileStream != null) {
//								fileStream.println("\nSentence:   " + this.sentences.get(i));
//								fileStream.println("Pattern:    " + pattern.getPatternString());
//								pattern.printCausalInformationConjunctions(fileStream);
//								fileStream.println("Verbs:      " + pattern.getVerbString());
								pattern.printCausalInformation(fileStream);
//								fileStream.println(pattern.getMatches());
							} else {
//								System.out.println("No output file");
							}
							if (debugFileStream != null) {
								debugFileStream.println("Pattern: " + pattern.getPatternString());
//								pattern.printCausalInformationConjunctions(debugFileStream);
								pattern.printCausalInformation(debugFileStream);
								debugFileStream.println(pattern.getMatches());
							}
							//System.out.println(pattern.getMatches());
							pattern.resetPattern();
							sentence.resetProcessOrder();
							patternFactory.actualizeUsageData(patternDefinition);
						}
						//} else {
							//System.out.println(pattern.getMatches());
						//System.out.println("Unsuccessful parse");
						sentence.resetProcessOrder();
						pattern.resetPattern();
						//}
					} else {
//						System.out.println("\t*irelevant pattern");
					}
				} catch (Exception e) {
					System.out.println("Pattern: \'"+patternDefinition+"\', message: " + e.getMessage());
					e.printStackTrace();
				}
			}
			
		}	
		
		//if (this.fileStream != null) this.fileStream.close();
	}
	
	// method for extraction of relations from sentence pair
	public void extractRelationsFromSentencePairs(PatternFactory patternFactory) {
		SentencePairWrapper pair = new SentencePairWrapper(this.parseTrees.get(0), this.parseTrees.get(1));
		
		List<String> patterns = patternFactory.getPatternsDefinitions();
		//System.out.println("Number of patterns: " + patterns.size());
		
		for (int i = 1; i < this.parseTrees.size(); i++) {
		
			//pair.printProcessOrderInline();
			//pair.printProcessOrder();
//			System.out.println("\nSentence pair: " + pair.getSentencePairString());
			if (debugFileStream != null) debugFileStream.println("\nSentence pair: " + pair.getSentencePairString());
			
			for (String patternDefinition : patterns) {
			
				//System.out.println("Pattern: " + patternDefinition);
				try {
					Pattern pattern = patternFactory.createPattern(patternDefinition.startsWith("C"), patternDefinition.substring(2).trim());
					pattern.returnToken(new WildcardToken("***"));
					//System.out.println("Pattern: " + pattern.getPatternString());
					//while (PatternMatcher.tryPattern(sentence, pattern)) {
					
					if (pattern.useThisPattern(pair)) {
					
						if (PatternMatcher.tryPattern(pair, pattern)) {
//							System.out.println("Pattern: " + pattern.getPatternString());
//							pattern.printCausalInformation(System.out);
//							fileStream.println(pattern.getMatches());
//							System.out.println("Successful parse");
							pattern.flag(pair);
							if (fileStream != null && pattern.isCausal()) {
//								fileStream.println("Pattern: " + pattern.getPatternString());
								pattern.printCausalInformation(fileStream);
//								fileStream.println(pattern.getMatches());
							}
							//System.out.println(pattern.getMatches());
							if (debugFileStream != null) {
								debugFileStream.println("Pattern: " + pattern.getPatternString());
								pattern.printCausalInformation(debugFileStream);
//								debugFileStream.println(pattern.getMatches());
							}
							pattern.resetPattern();
							pair.resetProcessOrder();
							patternFactory.actualizeUsageData(patternDefinition);
							//}
						} else {
							//System.out.println(pattern.getMatches());
							//System.out.println("Unsuccessful parse");
							pair.resetProcessOrder();
							//pattern.resetPattern();
						}
					} else {
						//System.out.println("*");
					}
				} catch (Exception e) {
					System.out.println("Pattern: \'"+patternDefinition+"\', message: " + e.getMessage());
					e.printStackTrace();
				}
			}
			pair.addSentence(this.parseTrees.get(i));
		}
	
	}

	public void setOutputStream(String filename) {
		try {
			System.out.println("Creating output file");
			//String outputFilename = "data/output/"+filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf("."))+"-output.txt";
			this.fileStream = new PrintStream(new File(filename));
			System.out.println("Output in file \'"+filename+"\'");
		} catch (Exception e) {
			System.out.println("Error creating output file");
			System.out.println(e.getMessage());
			this.fileStream = null;
		}
	}
	
	public void setDebugOutputStream(String filename) {
		try {
			System.out.println("Creating debug output file");
			//String outputFilename = "data/output/"+filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf("."))+"-output.txt";
			this.debugFileStream = new PrintStream(new File(filename));
			System.out.println("Debug output in file \'"+filename+"\'");
		} catch (Exception e) {
			System.out.println("Error creating output file");
			System.out.println(e.getMessage());
			this.debugFileStream = null;
		}
	}
	
	public void parseSentences() {
		//Tree dotSentence = this.parser.parse(SentenceWrapper.DOT_SENTENCE);;
		for (String sentence : this.sentences) {
			//System.out.println("Parsing sentence \'" + sentence + "\'");
			Tree parsed = this.parser.parse(sentence);
			//System.out.println(parsed);
			//parsed.addChild(0, dotSentence.getLeaves().get(1).parent(dotSentence));
			TreeUtils.preprocessPhraseLabels(parsed);
			this.parseTrees.add(parsed);
//			parsed.indentedListPrint();
		}
	}
	
//	// test method
//	private void parseString(String string) {
//		if (string.length() > 0) {
//			Tree parsed = this.parser.parse(string);
//			parsed.indentedListPrint();
//		}
//	}
//	
}
