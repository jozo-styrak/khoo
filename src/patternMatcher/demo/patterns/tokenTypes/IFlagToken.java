package patternMatcher.demo.patterns.tokenTypes;

import patternMatcher.demo.wrappers.SentenceWrapper;

// interface that should all tokens, that can flag words, implements
public interface IFlagToken {
	
	public void flag(SentenceWrapper sentence);
	
}
