package ntnu.khoo.patterns.token;

import ntnu.khoo.wrappers.SentenceWrapper;

// interface that should all tokens, that can flag words, implements
public interface IFlagToken {
	
	public void flag(SentenceWrapper sentence);
	
}
