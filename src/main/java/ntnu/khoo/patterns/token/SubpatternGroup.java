package ntnu.khoo.patterns.token;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import ntnu.khoo.patterns.Pattern;
import ntnu.khoo.patterns.PatternMatcher;
import ntnu.khoo.wrappers.SentenceWrapper;

// subpattern implementation
public class SubpatternGroup extends Token implements IFlagToken, IPatternSlot {

	private List<Pattern> subpatterns;
	
	// for looking in pattern lists
	private int index;
	
	// link to matched pattern
	private Pattern matchingPattern;
	
	public SubpatternGroup(String value, List<Pattern> subpatterns) {
		super(value);
		this.isOptionLeft = true;
		this.subpatterns = subpatterns;
		this.index = 0;
		this.matchingPattern = null;
	}
	
	public boolean match(SentenceWrapper sentence) {
		boolean matched = false;
		
		// if is method visited first time, the last position in sentence is remembered for backtracking
		if (this.index == 0) {
			this.lastMatchPosition = sentence.getCurrentPosition();
		}
		
		// if backtracking happened, test if current matched pattern has any other option
		if (this.matchingPattern != null) {
			if  (this.matchingPattern.isOptionLeft()) {
				Token token = this.matchingPattern.backtrack(sentence);
				// found token for backtracking
				if (token != null) {
					//this.matchingPattern.returnToken(token);
					this.matchingPattern.getToProcess().push(token);
					matched = PatternMatcher.tryPattern(sentence, this.matchingPattern);
					if (!matched) {
						sentence.setCurrentPosition(lastMatchPosition);  // in case of failure reset position
						this.matchingPattern = null;
					}
				} else {
					this.matchingPattern = null;
				}
			} else {
				this.matchingPattern = null;
			}
		}
		
		// pattern didn't succeed in retry, find a new one
		if (!matched) {
			this.matchingPattern = null;
			int currentPosition = sentence.getCurrentPosition();
			while ((!matched) && (this.index < this.subpatterns.size())) {
				matched = PatternMatcher.tryPattern(sentence, this.subpatterns.get(index));
				this.index++;
				if (matched) {
					this.matchingPattern = this.subpatterns.get(index-1);
					//this.lastMatchPosition = sentence.getCurrentPosition();
				} else {
					sentence.setCurrentPosition(currentPosition);
				}
			}		
		}
		
		if (this.matchingPattern == null && this.index >= this.subpatterns.size()) {
			this.isOptionLeft = false;
		}
				
		return matched;
	}
	
	public void resetToken() {
		this.isOptionLeft = true;
		this.index = 0;
		this.matchingPattern = null;
		this.removeAllMatches();
		for (Pattern pattern : this.subpatterns) {
			pattern.removeAllMatches();
			//pattern.backtrack();
			pattern.resetPattern();
		}
		this.lastMatchPosition = 50000;
	}
	
	public String getMatches() {
		if (this.matchingPattern != null) {
			return "[" + this.value + "("+this.getClass().getSimpleName()+")" + " /matching Pattern " + this.matchingPattern.getMatches() + "]";
		} else {
			return "[" + this.value + "("+this.getClass().getSimpleName()+")" + "NONE]";
		}
	}
	
	@Override
	public List<Tree> getCause() {
		if (this.matchingPattern.isCausal()) {
			List<Tree> cause = new ArrayList<>();
			for (Object token : this.matchingPattern.getProcessed().toArray()) {
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
		if (this.matchingPattern.isCausal()) {
			List<Tree> effect = new ArrayList<>();
			for (Object token : this.matchingPattern.getProcessed().toArray()) {
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

	@Override
	public void flag(SentenceWrapper sentence) {
		for (Object token : this.matchingPattern.getProcessed()) {
			if (token instanceof IFlagToken) {
				((IFlagToken)token).flag(sentence);
			}
		}
	}
	
	public Pattern getMatchingPattern() {
		return this.matchingPattern;
	}
}
