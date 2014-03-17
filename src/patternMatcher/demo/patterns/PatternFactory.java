package patternMatcher.demo.patterns;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import patternMatcher.demo.patterns.tokenTypes.EmptyToken;
import patternMatcher.demo.patterns.tokenTypes.LiteralToken;
import patternMatcher.demo.patterns.tokenTypes.OneWordToken;
import patternMatcher.demo.patterns.tokenTypes.POSTagToken;
import patternMatcher.demo.patterns.tokenTypes.PhraseToken;
import patternMatcher.demo.patterns.tokenTypes.SlotToken;
import patternMatcher.demo.patterns.tokenTypes.SubpatternGroup;
import patternMatcher.demo.patterns.tokenTypes.Token;
import patternMatcher.demo.patterns.tokenTypes.VerbGroupToken;
import patternMatcher.demo.patterns.tokenTypes.WildcardToken;
import edu.stanford.nlp.util.StringUtils;

/*
 * class handles loading of patern objects, subpatterns, verbgroups
 * creation of tokens and patterns, etc.
 */
public class PatternFactory {

	private HashMap<String, List<String>> subpatterns;
	private HashMap<String, List<String>> verbGroups;
	
	// pattern lists, currently used list of definitions, object are created at the time of usage because of insufficient memory
	private List<Pattern> patterns;
	private List<String> patternsDefinitions;
	
	// pattern statistics
	private List<Integer> patternsUsageCount;
	
	public PatternFactory() {
		this.subpatterns = new HashMap<String, List<String>>();
		this.verbGroups = new HashMap<String, List<String>>();
		this.patterns = new ArrayList<Pattern>();
		this.patternsDefinitions = new ArrayList<String>();
		this.patternsUsageCount = new ArrayList<Integer>();
	}
	
	// loading of patterns from pattern file and automatic creation of objects
	// not used, memory leaks
	public void loadPatterns(String patternFile) throws Exception {
		
		BufferedReader in = new BufferedReader(new FileReader(patternFile));
		String line = null;
		
		System.out.println("Loading patterns");
		
		while ((line = in.readLine()) != null) {
			if (!line.startsWith("#")) {
				try {
					this.patterns.add(this.createPattern((line.startsWith("C")), line.substring(2).trim()));
				} catch (Exception e) {
					System.out.println("Error creating pattern \'"+line+"\': "+ e.getMessage());
					//e.printStackTrace();
				}
			}
		}
		for (Pattern pattern : this.patterns) {
			pattern.returnToken(new WildcardToken("***"));
		}
		
		in.close();
		System.out.println("Loaded " + this.patterns.size() + " patterns");
		
	}
	
	// lazy loading of patterns
	public void loadPatternsDefinitions(String patternFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(patternFile));
		String line = null;
		
		System.out.println("Loading patterns definitions");
		
		while ((line = in.readLine()) != null) {
			if (!line.startsWith("#")) {
				this.patternsDefinitions.add(line.trim());
			}
		}
		
		in.close();
		System.out.println("Loaded " + this.patternsDefinitions.size() + " patterns definitions");
		
		// initialize pattern usage structure
		for (int i = 0; i < this.patternsDefinitions.size(); i++) {
			this.patternsUsageCount.add(0);
		}
		
	}
	
	public List<String> getPatternsDefinitions() {
		return patternsDefinitions;
	}

	public void loadSubpatterns(String subpatternFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(subpatternFile));
		String line = null;
		List<String> patterns = new ArrayList<String>();
		String subpatternKey = "";
		
		System.out.println("Loading subpatterns");
		
		while ((line = in.readLine()) != null) {
			
			if (line.trim().startsWith("#")) {
				if (patterns.size() > 0) {
					subpatterns.put(subpatternKey, patterns);
					patterns = new ArrayList<String>();
				}
				subpatternKey = line.trim().substring(1);
			} else if (line.trim().length() > 0) {
				patterns.add(line.trim());
			}
			
		}
		if (patterns.size() > 0) {
			subpatterns.put(subpatternKey, patterns);
		}
		
		
		System.out.println("Loaded " + this.subpatterns.size() + " subpatterns");
		
		in.close();
		
	}
	
	public void loadVerbGroups(String verbGroupFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(verbGroupFile));
		String line = null;
		List<String> verbs = new ArrayList<String>();
		String verbGroupKey = "";
		
		System.out.println("Loading verb groups");
		
		while ((line = in.readLine()) != null) {
			
			if (line.trim().startsWith("#")) {
				if (verbs.size() > 0) {
					this.verbGroups.put(verbGroupKey, verbs);
					verbs = new ArrayList<String>();
				}
				verbGroupKey = line.trim().substring(1);
			} else if (line.trim().length() > 0 && !line.startsWith("*")) {
				verbs.add(line.trim());
			}
			
		}
		if (verbs.size() != 0) {
			this.verbGroups.put(verbGroupKey, verbs);
		}
		
		System.out.println("Loaded " + this.verbGroups.size() + " verb groups");
		
		in.close();	
		
	}
	
	// resolve subpattern string into list of patterns
	public List<Pattern> getSubpattern(String subpatternString) throws Exception {
		if (this.subpatterns.containsKey(subpatternString)) {
			List<Pattern> patterns = new ArrayList<Pattern>();
			for (String patternString : this.subpatterns.get(subpatternString)) {
				patterns.add(createPattern(patternString));
			}
			return patterns;
		} else throw new Exception("Subpattern \'"+subpatternString+"\' not found");
	}
	
	// resolve and return new Token object based on string value supplied
	public Token createToken(String value) throws Exception {
		Token newToken = null;
		if (value.startsWith("&")) newToken = this.createSubpatternGroup(value.substring(1));
		else if (value.startsWith("@")) newToken = new POSTagToken(value.substring(1));
		else if (value.startsWith("$")) newToken = this.createVerbGroupToken(value.substring(1));
		else if (value.compareTo("*") == 0) newToken = new OneWordToken(value);
		else if (value.compareTo("***") == 0) newToken = new WildcardToken(value);
		else if (value.compareTo("[1]") == 0) newToken = new SlotToken(SlotToken.SlotType.CAUSE_SLOT);
		else if (value.compareTo("[2]") == 0) newToken = new SlotToken(SlotToken.SlotType.EFFECT_SLOT);
		else if (value.startsWith("[") && value.endsWith(":0]") && StringUtils.isCapitalized(value.substring(1, value.indexOf(":")))) newToken = new PhraseToken(value.substring(1, value.indexOf(":")), 0);
		else if (value.startsWith("[") && value.endsWith(":1]") && StringUtils.isCapitalized(value.substring(1, value.indexOf(":")))) newToken = new PhraseToken(value.substring(1, value.indexOf(":")), SlotToken.SlotType.CAUSE_SLOT);
		else if (value.startsWith("[") && value.endsWith(":2]") && StringUtils.isCapitalized(value.substring(1, value.indexOf(":")))) newToken = new PhraseToken(value.substring(1, value.indexOf(":")), SlotToken.SlotType.EFFECT_SLOT);
		else if (value.startsWith("[") && value.endsWith(":1]") && !StringUtils.isCapitalized(value.substring(1, value.indexOf(":")))) newToken = new POSTagToken(value.substring(1, value.indexOf(":")), SlotToken.SlotType.CAUSE_SLOT);
		else if (value.startsWith("[") && value.endsWith(":2]") && !StringUtils.isCapitalized(value.substring(1, value.indexOf(":")))) newToken = new POSTagToken(value.substring(1, value.indexOf(":")), SlotToken.SlotType.EFFECT_SLOT);
		else if (value.compareTo("_") == 0) newToken = new EmptyToken("_");
		else newToken = new LiteralToken(value);
		return newToken;
	}
	
	public Pattern createPattern(boolean isCausal, String value) throws Exception {
		String[] tokens = value.split(" ");
		Pattern pattern = new Pattern(isCausal, value);
		for (int i = tokens.length; i > 0; i -= 1) {
			Token token = this.createToken(tokens[i-1].trim());
			pattern.returnToken(token);
		}
		pattern.setPatternString(value);
		pattern.setLastToProcess();
		return pattern;
	}	
	
	public Pattern createPattern(String value) throws Exception {
		return this.createPattern(true, value);
	}
	
	public SubpatternGroup createSubpatternGroup(String value) throws Exception { 
		List<Pattern> subpatterns = this.getSubpattern(value);
		SubpatternGroup subpattern = new SubpatternGroup(value, subpatterns);
		return subpattern;
	}
	
	public VerbGroupToken createVerbGroupToken(String value) {
		String string = value;
		String tilda = "";
		if (string.endsWith("~")) {
			string = string.substring(0, string.length() - 1);
			tilda = "~";
		}
		String vgId = (string.contains("|")) ? string.substring(0, string.indexOf("|")) : string;
		String tag = (string.contains("|")) ? string.substring(string.indexOf("|")) : "";
		VerbGroupToken token = new VerbGroupToken(string);
		List<Token> verbs = new ArrayList<Token>();
		try {
			for (String v : this.verbGroups.get(vgId)) {
				verbs.add(new LiteralToken(v + tag + tilda));
			}
			token.setVerbs(verbs);
			return token;
		} catch (Exception e) {
			System.out.println("Error creating verb group \'"+vgId+"\'");
			throw e;
		}
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	public void printSubpatterns() {
		for (String key : this.subpatterns.keySet()) {
			System.out.println(key + " : ");
			for (String sub : this.subpatterns.get(key)) {
				System.out.println("  " + sub);
			}
		}
	}
	
	// for testing, add pattern into pattern list
	public void addPattern(boolean isCausal, String patternDefinition) throws Exception {
		this.patterns.add(createPattern(isCausal, patternDefinition));
	}
	
	/*
	 * methods for collecting of pattern usage data
	 * when is some pattern used, the counf of this pattern usafe is increased
	 */
	public void actualizeUsageData(String patternDefinition) {
		for (int i = 0; i < this.patternsDefinitions.size(); i++) {
			if (this.patternsDefinitions.get(i).compareTo(patternDefinition) == 0) {
				this.patternsUsageCount.set(i, this.patternsUsageCount.get(i) + 1);
			}
		}
	}
	
	public void printUsageData(PrintStream out) {
		for (int i = 0; i < this.patternsDefinitions.size(); i++) {
			if (this.patternsUsageCount.get(i) > 0) {
				out.printf("%-85s%3s\n", this.patternsDefinitions.get(i), this.patternsUsageCount.get(i).toString());
			}
		}
	}
	
}
