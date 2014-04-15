package ntnu.khoo.patterns.token;

import ntnu.khoo.wrappers.SentenceWrapper;

// empty subpattern implementation
public class EmptyToken extends Token {

	public EmptyToken(String value) {
		super(value);
		this.isOptionLeft = true;
	}
	
	@Override
	public boolean match(SentenceWrapper sentence) {
		isOptionLeft = false;
		this.lastMatchPosition = sentence.getCurrentPosition();
		return true;
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}

}
