package patternMatcher.demo.patterns.tokenTypes;

import edu.stanford.nlp.trees.Tree;
import patternMatcher.demo.wrappers.SentenceWrapper;

// wildcard * implementation
public class OneWordToken extends Token {

	private int backtrackCount = 0;
	
	public OneWordToken(String value) {
		super(value);
		isOptionLeft = true;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {
		boolean matched = false;
		
		switch (backtrackCount) {
		
		// first matching of the token - consumes no part of sentence
		case 0:
			backtrackCount++;
			matched = true;
			break;
		
		// after backtracking, consumes one token
		case 1:
			backtrackCount++;
			Tree matchItem = sentence.getNextLeaf();
			if (matchItem != null) {
				setMatchItem(matchItem);
				matched = true;
			} else {
				matched = false;
			}
			isOptionLeft = false;
			break;
			
		default:
			matched = false;
		}
		this.lastMatchPosition = sentence.getCurrentPosition();
		return matched;
	}
	
	@Override
	public void resetToken() {
		super.resetToken();
		backtrackCount = 0;
		this.lastMatchPosition = 50000;
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	@Override
	public String toString() {
		return "Wild card: *";
	}

}
