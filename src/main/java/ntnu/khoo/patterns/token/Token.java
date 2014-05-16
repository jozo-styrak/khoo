package ntnu.khoo.patterns.token;

import java.util.ArrayList;
import java.util.List;

import ntnu.khoo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

/*
 * abstract parent
 */
public abstract class Token {

	// string value of token
	protected String value;
	
	// matched part of parse tree - each is represented by item in list
	// may include leaf or subtree for phrase
	protected List<Tree> matchItems;
	
	// if there is option left for this token
	protected boolean isOptionLeft;
	
	// position in sentence for backtracking
	// sentence index should return to this position and try another option of this token 
	protected int lastMatchPosition;
	
	public Token() {
		this.matchItems = new ArrayList<>();
		this.isOptionLeft = true;
	}
	
	public Token(String value) {
		this.value = value;
		this.matchItems = new ArrayList<>();
	}
	
	public void setMatchItem(Tree match) {
		this.matchItems.add(match);
	}
	
	public void removeMatchItem() {
		if (this.matchItems.size() != 0) this.matchItems.remove(matchItems.size()-1);
	}
	
	public void removeAllMatches() {
		this.matchItems = new ArrayList<>();
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
	
	// debug method
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
