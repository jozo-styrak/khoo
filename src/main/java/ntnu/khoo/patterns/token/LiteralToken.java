package ntnu.khoo.patterns.token;

import edu.stanford.nlp.trees.Tree;
import ntnu.khoo.wrappers.SentenceWrapper;

// literal token implementation
public class LiteralToken extends Token implements IFlagToken {

	// if token is ended with *
	private boolean wildcard = false;
	
	// tag to match
	private String tag = null;
	
	private boolean flagged;
	
	// formats retrieve, retrieve*, retriev*|N
	public LiteralToken(String value) {
		this.value = value;
		if (this.value.endsWith("~")) {
			this.flagged = true;
			this.value = this.value.substring(0, this.value.length()-1);
		} else {
			this.flagged = false;
		}
		if (this.value.contains("|")) {
			this.value = this.value.substring(0, this.value.indexOf("|"));
			this.tag = value.substring(value.indexOf("|")+1);
		}
		if (this.value.endsWith("*")) {
			this.wildcard = true;
			this.value = this.value.substring(0, this.value.length()-1);
		}
		isOptionLeft = true;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {
		Tree matchItem = sentence.getNextLeaf();
		
		// match criterias
		if (matchItem != null
			&& ((wildcard && matchItem.label().toString().toLowerCase().startsWith(value.toLowerCase()))
			|| (!wildcard && matchItem.label().toString().toLowerCase().compareTo(value.toLowerCase()) == 0))
			&& ((tag == null)
			|| (tag != null && sentence.getTag(matchItem).startsWith(tag)))
			&& (this.flagged || !sentence.isFlagged(matchItem))
			) {
			
			setMatchItem(matchItem);
			isOptionLeft = false;
			this.lastMatchPosition = sentence.getCurrentPosition();
			return true;
		} else {
			if (matchItem != null) sentence.backtrack();
			isOptionLeft = false;
			this.lastMatchPosition = sentence.getCurrentPosition();
			return false;
		}
	}
	
	@Override
	public String toString() {
		String wildcardStr = (wildcard) ? "*" : "";
		String tagStr = (tag != null) ? ", tag:" + tag : "";
		return "Literal token: " + this.value + wildcardStr + tagStr;
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}

	@Override
	public void flag(SentenceWrapper sentence) {
		if (!this.flagged) {
			for (Tree tree : this.matchItems) {
				if (tree != null) sentence.flagLeaf(tree);
			}			
		}
	}
	
	public boolean isWildcard() {
		return this.wildcard;
	}

}
