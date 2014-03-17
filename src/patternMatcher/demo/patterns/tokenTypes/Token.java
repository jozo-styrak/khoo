package patternMatcher.demo.patterns.tokenTypes;

import java.util.ArrayList;
import java.util.List;

import patternMatcher.demo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

/*
 * abstract parent
 */
public abstract class Token {

	// string value of token
	protected String value;
	
	// matched part of parse tree
	protected List<Tree> matchItems;
	
	// if there is option left for this token
	protected boolean isOptionLeft;
	
	// position in sentence for backtracking
	// sentence index should return to this position and try another option of this token 
	protected int lastMatchPosition;
	
	public Token() {
		this.matchItems = new ArrayList<Tree>();
		this.isOptionLeft = true;
	}
	
	public Token(String value) {
		this.value = value;
		this.matchItems = new ArrayList<Tree>();
	}
	
	public void setMatchItem(Tree match) {
		this.matchItems.add(match);
	}
	
	public void removeMatchItem() {
		if (this.matchItems.size() != 0) this.matchItems.remove(matchItems.size()-1);
	}
	
	public void removeAllMatches() {
		this.matchItems = new ArrayList<Tree>(); 
	}
	
	public abstract boolean match(SentenceWrapper sentence);
	
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	public void resetToken() {
		this.isOptionLeft = true;
		this.lastMatchPosition = 50000;
		this.removeAllMatches();
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	// debug
	public String getMatches() {
		String out = this.value+ "("+this.getClass().getSimpleName()+")";
		if (this.matchItems.size() != 0) {
			for (Tree matchItem : this.matchItems) {
				for (Tree leaf : matchItem.getLeaves()) {
					out += "|" + leaf.label().toString();
				}
			}
		} else {
			out += " NONE";
		}
		return "[" + out + "]";
	}

	public int getLastMatchPosition() {
		return lastMatchPosition;
	}
	
	public String getValue() {
		return this.value;
	}
	
}
