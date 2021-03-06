package patternMatcher.demo.patterns;

import patternMatcher.demo.patterns.tokenTypes.Token;
import patternMatcher.demo.wrappers.SentenceWrapper;

/*
 * Only method to try to match some pattern on given sentence/pair
 */
public class PatternMatcher {
	
	public static boolean tryPattern(SentenceWrapper sentence, Pattern pattern) {
		
		// takes first token from stack to process
		Token currentToken = pattern.getNextToken();
		
		// either token to process is null or sentence end was reached
		while ((currentToken != null) && (!sentence.isConsumed())) {
			
			if (currentToken.isOptionLeft()) {
				
				// match operation for current token
				if (currentToken.match(sentence)) {
					pattern.consumeToken(currentToken);
					if (!pattern.isConsumed()) {
						currentToken = pattern.getNextToken();
					} else {
						// pattern is consumed
						currentToken = null;
					}
				} 
					
			} else {
				// backtracking in stack of processed tokens
				pattern.returnToken(currentToken);
				currentToken = pattern.backtrack(sentence);	
			} 
			
		}
		
		if (currentToken != null) pattern.returnToken(currentToken);
		return (currentToken == null && pattern.isConsumed());
		
	}

}
