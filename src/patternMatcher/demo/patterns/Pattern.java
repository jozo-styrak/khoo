package patternMatcher.demo.patterns;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.stanford.nlp.trees.Tree;
import patternMatcher.demo.patterns.tokenTypes.IFlagToken;
import patternMatcher.demo.patterns.tokenTypes.IPatternSlot;
import patternMatcher.demo.patterns.tokenTypes.LiteralToken;
import patternMatcher.demo.patterns.tokenTypes.SlotToken;
import patternMatcher.demo.patterns.tokenTypes.SubpatternGroup;
import patternMatcher.demo.patterns.tokenTypes.Token;
import patternMatcher.demo.patterns.tokenTypes.VerbGroupToken;
import patternMatcher.demo.wrappers.SentenceWrapper;

public class Pattern extends Token implements IPatternSlot, IFlagToken {

	// stacks of token to process and processed
	private Stack<Token> processed;
	private Stack<Token> toProcess;
	
	// whether this pattern contains causal information
	private boolean isCausal;
	
	// string representation
	private String patternString;
	
	public Pattern(boolean causal, String value) {
		super(value);
		this.isOptionLeft = true;
		this.processed = new Stack<Token>();
		this.toProcess = new Stack<Token>();
		this.isCausal = causal;
		this.patternString = "";
	}
	
	public boolean isConsumed() {
		return toProcess.empty();
	}
	
	// returns token from top of the stack to process
	public Token getNextToken() {
		return (toProcess.empty()) ? null : toProcess.pop();
	}
	
	// adds token to processed tokens
	public void consumeToken(Token token) {
		processed.push(token);
	}
	
	// pushes token back to stack to process
	public void returnToken(Token token) {
		token.resetToken();
		toProcess.push(token);
	}
	
	// traverse through stack of processed tokens to find a token with any option left
	public Token backtrack(SentenceWrapper sentence) {
		Token token = null;
		boolean option = false;
		while (!processed.empty() && !option) {
			Token t = processed.pop();
			if (t.isOptionLeft()) {
				option = true;
				token = t;
				// resets the position of index in sentence
				sentence.setCurrentPosition(token.getLastMatchPosition());
			} else {
				// resets token a pushes it back to stack
				t.resetToken();
				toProcess.push(t);
				// backtracks to previous leaf
				// not sure if it has any effect currently
				sentence.backtrack();
			}
		}
		return token;
	}
	
	@Override
	public String toString() {
		String ret = "Pattern:\n";
		for (Object token : toProcess.toArray()) {
			ret += ((Token)token).toString() + "\n";
		}
		return ret;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {
		boolean matched = PatternMatcher.tryPattern(sentence, this);
		this.isOptionLeft = false;
		//if (!matched) sentence.backtrack();
		if (matched) {
			this.lastMatchPosition = sentence.getCurrentPosition();
		}
		return matched;
	}
	
	@Override
	public void resetToken() {
		this.isOptionLeft = true;
		this.removeAllMatches();
	}

	public Stack<Token> getProcessed() {
		return processed;
	}

	public Stack<Token> getToProcess() {
		return toProcess;
	}

	@Override
	public List<Tree> getCause() {
		if (isCausal) {
			List<Tree> cause = new ArrayList<Tree>();
			for (Object token : this.processed.toArray()) {
				if (token instanceof IPatternSlot) {
					List<Tree> causes = ((IPatternSlot)token).getCause();
					if (causes != null) {
						for (Tree t : causes) {
							for (Tree leaf : t.getLeaves()) {
								cause.add(leaf);
							}
						}
					}
				}
			}
			return cause;
		} else return null;
	}

	@Override
	public List<Tree> getEffect() {
		if (isCausal) {
			List<Tree> effect = new ArrayList<Tree>();
			for (Object token : this.processed.toArray()) {
				if (token instanceof IPatternSlot) {
					List<Tree> effects = ((IPatternSlot)token).getEffect();
					if (effects != null) {
						for (Tree t : effects) {
							for (Tree leaf : t.getLeaves()) {
								effect.add(leaf);
							}
						}
					}
				}
			}
			return effect;
		} else return null;
	}
	
	public String getCauseString() {
		String cause = "";
		if (isCausal) {
			for (Tree leaf : getCause()) {
				cause = cause + leaf + " ";
			}
		}
		return this.processOutputString(cause);
	}
	
	public String getEffectString() {
		String effect = "";
		if (isCausal) {
			for (Tree leaf : getEffect()) {
				effect = effect + leaf + " ";
			}
		}
		return this.processOutputString(effect);
	}
	
	// simple casual information print
	public void printCausalInformation(PrintStream out) {
		if (isCausal) {
			String cause = this.getCauseString();
			String effect = this.getEffectString();
			if (cause.length() > 0 && effect.length() > 0) out.println(cause +"\t"+ effect); //out.println("Verb(" + this.getVerbString() + ")\t" + cause +"\t"+ effect);
		}
	}
	
	// print causal information, if there is a conjunction, generate subparts
	public void printCausalInformationConjunctions(PrintStream out) {
		if (isCausal) {
			for (String relation : this.generateCauseEffectConjunctionPairs()) {
				out.println(relation);
			}
		}
	}

	@Override
	public void flag(SentenceWrapper sentence) {
		for (Object token : this.processed.toArray()) {
			if (token instanceof IFlagToken) {
				((IFlagToken)token).flag(sentence);
			}
		}
	}
	
	// slot tokens behave differently when they are last in order, therefore we have to mark them as last in order 
	public void setLastToProcess() {
		if (this.getToProcess().get(0) instanceof SlotToken) {
			((SlotToken)this.getToProcess().get(0)).setLastToProcess(true);
		} else if (this.getToProcess().get(0) instanceof Pattern) {
			((Pattern)this.getToProcess().get(0)).setLastToProcess();
		}
	}
	
	public String getMatches() {
			String output = "";
			for (Object token : this.processed.toArray()) {
				output += "|" + ((Token)token).getMatches()+ "("+token.getClass().getSimpleName()+")";
			}
			return "{" + this.value + "("+this.getClass().getSimpleName()+")"  + output + "}";
	} 
	
	public void resetPattern() {
		while (!this.processed.empty()) {
			Token token = this.processed.pop();
			token.resetToken();
			this.toProcess.push(token);
		}
	}

	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}
	
	public boolean isCausal() {
		return isCausal;
	}
	
	public boolean isOptionLeft() {
		boolean isThere = false;
		for (Object token : this.processed.toArray()) {
			if (((Token)token).isOptionLeft()) {
				isThere = true;
			}
		}
		return isThere;
	}
	
	// returns list of literal tokens for filtering
	private List<LiteralToken> getLiteralTokens() {
		List<LiteralToken> literals = new ArrayList<LiteralToken>();
		for (Object token : this.toProcess.toArray()) {
			if (token instanceof LiteralToken) {
				literals.add((LiteralToken)token);
			}
		}
		return literals;
	}
	
	// get verb groups from this patterns
	private List<VerbGroupToken> getVerbGroupTokens() {
		List<VerbGroupToken> groups = new ArrayList<VerbGroupToken>();
		for (Object token : this.toProcess.toArray()) {
			if (token instanceof VerbGroupToken) {
				groups.add((VerbGroupToken)token);
			}
		}
		return groups;
	}
	
	// filter pattern, if it is necessary to use on sentence, based on literal tokens matching and verb groups
	public boolean useThisPattern(SentenceWrapper sentence) {
		boolean relevant = true;
		// first, check literals
		for (LiteralToken literal : this.getLiteralTokens()) {
			if (!sentence.containsLiteral(literal.getValue(), literal.isWildcard())) {
				relevant = false;
			}
		}
		
		// if matched all, check verb groups
		if (relevant) {
			for (VerbGroupToken verb : this.getVerbGroupTokens()) {
				if (!sentence.containsVerbFromVerbGroup(verb)) {
					relevant = false;
				}
			}
		}
		return relevant;
	}
	
	// get conjuncted subparts of given phrase
	// supported format - A, B and C | A and B | A
	private List<String> getConjunctedPhrases(String phrase) {
		List<String> phrases = new ArrayList<String>();
		String[] parts = phrase.split(" and ");
		// there is a 'and' conjunction
		if (parts.length > 1) {
			// try to split every non-ending part with ','
			for (int i = 0; i < parts.length-1; i ++) {
				String[] subparts = parts[i].split(",");
				for (String subpart : subparts) {
					phrases.add(subpart);
				}
			}
			phrases.add(parts[parts.length-1]);
		}
		else {
			for (String part : parts) {
				phrases.add(part);
			}
		}
		return phrases;
	}
	
	// if there is conjunction in cause or effect, this will produce list of pairs of cause and effect
	public List<String> generateCauseEffectConjunctionPairs() {
		List<String> pairs = new ArrayList<String>();
		if (isCausal) {
			String cause = this.getCauseString();
			String effect = this.getEffectString();
			if (cause.length() > 0 && effect.length() > 0) {
				for (String c : this.getConjunctedPhrases(cause)) {
					for (String e : this.getConjunctedPhrases(effect)) {
						pairs.add(c + "\t" + e);
					}
				}
			}
		}
		return pairs;
	}
	
	// debug method for extracting verb triggers in relations
	public String getVerbString() {
		String verb = "";
		for (Object token : this.processed.toArray()) {
			if (token instanceof VerbGroupToken) {
				verb += ((VerbGroupToken)token).getMatchedVerbString() + " ";
			} else if (token instanceof SubpatternGroup) {
				verb += ((SubpatternGroup)token).getMatchingPattern().getVerbString() + " ";
			}
		}
		return verb.trim();
	}
	
	// remove leading and ending dot from prhase result
	private String processOutputString(String phrase) {
		String output = (phrase.startsWith(".")) ? phrase.substring(1).trim() : phrase.trim();
		output = (output.endsWith(".")) ? output.substring(0, output.length() - 1) : output;
		return output;
	}
	
}
