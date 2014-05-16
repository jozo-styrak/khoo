package ntnu.khoo.patterns;

import ntnu.khoo.patterns.token.Token;
import ntnu.khoo.wrappers.SentenceWrapper;

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
	
	// newer version
	// better ending conditions - but bit buggy and necessary just for applying patterns to find conjunctions, therefore not used
//	public static boolean tryPattern(SentenceWrapper sentence, Pattern pattern) {
//		
//		// takes first token from stack to process
//		Token currentToken = pattern.getNextToken();
//		boolean finished = false;
//		
//		// either token to process is null or sentence end was reached
//		while (!finished) {
//		
//			if (currentToken.isOptionLeft()) {
//				
//				// match operation for current token
//				if (currentToken.match(sentence)) {
//					pattern.consumeToken(currentToken);
//					if (!pattern.isConsumed()) {
//						currentToken = pattern.getNextToken();
//					} else {
//						// pattern is consumed
//						currentToken = null;
//						finished = true;
//					}
//				} 
//					
//			} else {
//				// backtracking in stack of processed tokens
//				pattern.returnToken(currentToken);
//				currentToken = pattern.backtrack(sentence);
//				if (currentToken == null) {
//					finished = true;
//				}
//			} 
//			
//		}
//		
//		if (currentToken != null) pattern.returnToken(currentToken);
//		return (currentToken == null && pattern.isConsumed());
//		
//	}

}
