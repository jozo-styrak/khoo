package ntnu.khoo.patterns.token;

import edu.stanford.nlp.trees.Tree;
import ntnu.khoo.wrappers.SentenceWrapper;

// wildcard ***
public class WildcardToken extends Token {
	
	private int backtrackCount;

	public WildcardToken(String value) {
		super(value);
		isOptionLeft = true;
		backtrackCount = 0;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {

		boolean matched = false;
		
		if (backtrackCount == 0) {
			matched = true;
			backtrackCount++;
			this.lastMatchPosition = sentence.getCurrentPosition();
		} else {
			Tree matchItem = sentence.getNextLeaf();
			backtrackCount++;
			if (matchItem != null) {
				setMatchItem(matchItem);
				this.lastMatchPosition = sentence.getCurrentPosition();
				matched = true;
			} else {
				matched = false;
			}
		}
		
		return matched;
	}
	
	@Override
	public void resetToken() {
		super.resetToken();
		backtrackCount = 0;
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	@Override
	public String toString() {
		return "Wild card: ***";
	}

}
