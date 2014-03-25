package patternMatcher.demo.patterns.tokenTypes;

import java.util.List;

import patternMatcher.demo.wrappers.SentenceWrapper;

// verb group implementation
public class VerbGroupToken extends Token implements IFlagToken {

	private List<LiteralToken> verbs;
	private int index;
	
	public VerbGroupToken(String substring) {
		super(substring);
		this.isOptionLeft = true;
		this.index = 0;
	}
	
	public void setVerbs(List<LiteralToken> verbs) {
		this.verbs = verbs;
	}

	public boolean match(SentenceWrapper sentence) {
		boolean matched = false;
		if (this.index == 0) this.lastMatchPosition = sentence.getCurrentPosition();
		int currentPosition = sentence.getCurrentPosition();
		// look for matching verb
		while (!matched && this.index < this.verbs.size()) {
			matched = this.verbs.get(index).match(sentence);
			if (!matched) sentence.setCurrentPosition(currentPosition);
			this.index++;
		}
		if (this.index >= this.verbs.size()) isOptionLeft = false;
		//if (matched) this.lastMatchPosition = sentence.getCurrentPosition();
		return matched;
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	@Override
	public void resetToken() {
		this.isOptionLeft = true;
		this.removeAllMatches();
		this.index = 0;
		for (Token token : this.verbs) {
			token.removeAllMatches();
		}
		this.lastMatchPosition = 50000;
	}

	@Override
	public String toString() {
		return "Verb group: " + value;
	}

	@Override
	public void flag(SentenceWrapper sentence) {
		((IFlagToken)this.verbs.get(this.index-1)).flag(sentence);
	}
	
	public String getMatches() {
		return this.verbs.get(this.index -1).getMatches();
	}
	
	public String getMatchedVerbString() {
		return this.verbs.get(this.index-1).getValue();
	}
	
	public List<LiteralToken> getVerbs() {
		return this.verbs;
	} 
}
