package ntnu.khoo.wrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ntnu.khoo.wrappers.SentencePairWrapper;
import ntnu.khoo.wrappers.SentenceWrapper;
import ntnu.khoo.patterns.Pattern;
import ntnu.khoo.patterns.PatternFactory;
import ntnu.khoo.patterns.PatternMatcher;
import ntnu.khoo.patterns.output.Output;
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
	private List<Integer> sentencePositions;
	private List<Tree> parseTrees;
	private StanfordCoreNLP pipeline;
	private List<Output> outputStreams;
	
	// id - identificator for multithreading
	private String outputPrefix = "";
	
	public TextWrapper() {
		this(LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"));

	}
	
	public TextWrapper(LexicalizedParser parser) {
		this.parser = parser;
		this.sentences = new ArrayList<String>();
		this.parseTrees = new ArrayList<Tree>();
		this.sentencePositions = new ArrayList<Integer>();
		
		// annotator used for sentence splitting
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    this.pipeline = new StanfordCoreNLP(props);
//	    this.fileStream = null;
//	    this.debugFileStream = null;		
	    this.outputStreams = new ArrayList<Output>();
	}
	
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
//				System.out.println(this.outputPrefix + line);
			    Annotation lineAnnotation = new Annotation(line);
			    pipeline.annotate(lineAnnotation);
			    List<CoreMap> sentenceMap = lineAnnotation.get(SentencesAnnotation.class);
			    
			    for(CoreMap s: sentenceMap) {
			    	sentences.add(s.toString());
			    }
			}
		}
		
		System.out.println(this.outputPrefix + "Read "+this.sentences.size()+" sentences");
		System.out.println(this.outputPrefix + "Parsing sentences...");
		long startTime = System.currentTimeMillis();
		this.parseSentences();
		System.out.println(this.outputPrefix + "Parsing done in " + String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0) + " sec");
		reader.close();
	}
	
	public void loadAndParseTextFileWithPositions(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		loadAndParseTextFileWithPositions(reader);
	}
	
	public void loadAndParseTextFileWithPositions(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		loadAndParseTextFileWithPositions(reader);
	}	
	
	// for each sentence save position within the containing file - for brat ouput
	// not removing annotation brackets
	public void loadAndParseTextFileWithPositions(BufferedReader reader) throws IOException {
		String originalLine;
		int position = 0;
		
		while ((originalLine = reader.readLine()) != null) {
			
			String line = originalLine;
			
//			// comment allowed in text
			if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
//				line = line.replaceAll("\"", "").trim();
//				System.out.println(this.outputPrefix + line);
				int inlinePosition = position;
			    Annotation lineAnnotation = new Annotation(line);
			    pipeline.annotate(lineAnnotation);
			    List<CoreMap> sentenceMap = lineAnnotation.get(SentencesAnnotation.class);
			    
			    for(CoreMap s: sentenceMap) {
			    	sentences.add(s.toString());
			    	sentencePositions.add(inlinePosition);
			    	inlinePosition += s.toString().length() + 1;
			    }
			}
			position += originalLine.length() + 1;
		}
		
//		for (int i = 0; i < sentences.size(); i++) {
//			System.out.println(sentencePositions.get(i) + "\t\t" + sentences.get(i));
//		}
		
		System.out.println(this.outputPrefix + "Read "+this.sentences.size()+" sentences");
		System.out.println(this.outputPrefix + "Parsing sentences...");
		long startTime = System.currentTimeMillis();
		this.parseSentences();
		System.out.println(this.outputPrefix + "Parsing done in " + String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0) + " sec");
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
		
		// load pattern definitions
		List<String> patterns = patternFactory.getPatternsDefinitions();
		System.out.println(this.outputPrefix + "Number of patterns: " + patterns.size());
		
		// get sentence count 
		int count = (this.parseTrees.size() > 0) ? this.parseTrees.size() : this.sentences.size();
		
		
		// extract patterns for each sentence
		for (int i = 0; i < count; i++) {
			
			SentenceWrapper sentence = (this.parseTrees.size() > 0) ? new SentenceWrapper(this.parseTrees.get(i), this.parser) : new SentenceWrapper(this.sentences.get(i), this.parser);
			
			// for output purposes
			sentence.setSentencePosition(this.sentencePositions.get(i));
			
			for (String patternDefinition : patterns) {
				
				try {
					
					// create pattern object
					Pattern pattern = patternFactory.createPattern(patternDefinition.startsWith("C"), patternDefinition.substring(2).trim());
					pattern.returnToken(new WildcardToken("***"));
					
					// try to filter given pattern
					if (pattern.useThisPattern(sentence)) {
					
//						System.out.println(patternDefinition);
					
						// try pattern til it fails
						while (PatternMatcher.tryPattern(sentence, pattern)) {
							
							// add output to output file
							if (pattern.isCausal()) {
								List<Tree> cause = pattern.getCause();
								List<Tree> effect = pattern.getEffect();
								if (cause.size() > 0 && effect.size() > 0) {
									for (Output output : this.outputStreams) {
										output.printRelation(sentence, cause, effect);
									}
								}
							}
							
							// flag matched literals
							pattern.flag(sentence);
							// reset pattern tokens
							pattern.resetPattern();
							// reset order of sentnece
							sentence.resetProcessOrder();
//							patternFactory.actualizeUsageData(patternDefinition);
						}
						sentence.resetProcessOrder();
						pattern.resetPattern();
					}
					
				} catch (Exception e) {
					System.out.println(this.outputPrefix + "Pattern: \'"+patternDefinition+"\', message: " + e.getMessage());
					e.printStackTrace();
				}
			}
			
		}	
		
	}
	
	// method for extraction of relations from sentence pair
	public void extractRelationsFromSentencePairs(PatternFactory patternFactory) {
		
		SentencePairWrapper pair = new SentencePairWrapper(this.parseTrees.get(0), this.parseTrees.get(1));
		pair.setSentencePosition(this.sentencePositions.get(0));
		pair.setSentencePosition(this.sentencePositions.get(1));
		List<String> patterns = patternFactory.getPatternsDefinitions();
		
		for (int i = 1; i < this.parseTrees.size(); i++) {
			
			for (String patternDefinition : patterns) {
			
				try {
					Pattern pattern = patternFactory.createPattern(patternDefinition.startsWith("C"), patternDefinition.substring(2).trim());
					pattern.returnToken(new WildcardToken("***"));
					
					if (pattern.useThisPattern(pair)) {
						
//						System.out.println(patternDefinition);
					
						if (PatternMatcher.tryPattern(pair, pattern)) {
							
							// add output to output file
							if (pattern.isCausal()) {
								// outputs only non empty relations
								List<Tree> cause = pattern.getCause();
								List<Tree> effect = pattern.getEffect();
								if (cause.size() > 0 && effect.size() > 0) {
									for (Output output : this.outputStreams) {
										output.printRelation(pair, cause, effect);
									}								}
							}
							
							pattern.flag(pair);
							pattern.resetPattern();
							pair.resetProcessOrder();
							patternFactory.actualizeUsageData(patternDefinition);
						} else {
							pair.resetProcessOrder();
						}
					}
				} catch (Exception e) {
					System.out.println("Pattern: \'"+patternDefinition+"\', message: " + e.getMessage());
					e.printStackTrace();
				}
			}
			pair.addSentence(this.parseTrees.get(i));
			pair.setSentencePosition(this.sentencePositions.get(i));
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

	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}

	public void addOutputStream(Output outputStream) {
		this.outputStreams.add(outputStream);
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