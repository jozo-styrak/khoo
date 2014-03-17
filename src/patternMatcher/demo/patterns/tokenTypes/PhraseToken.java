package patternMatcher.demo.patterns.tokenTypes;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import patternMatcher.demo.wrappers.SentenceWrapper;

// phrase token implementation, [N:0], [N:1], [N:2]
public class PhraseToken extends Token implements IPatternSlot {

	private List<Tree> phrases;
	private int slotType;
	
	public PhraseToken(String value, int slotType) {
		super(value);
		isOptionLeft = true;
		phrases = null;
		this.slotType = slotType;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {
		// first call of method - get matching phrases from sentence and set backtrack position
		if (phrases == null) {
			phrases = sentence.getCurrentBranchPhrasesWithTag(value);
			this.lastMatchPosition = sentence.getCurrentPosition();
		}
		// trying phrases in tree order
		if (phrases.size() > 0) {
			Tree matchItem = phrases.get(0);
			// if this token was already matched, matches are removed
			if (matchItems.size() != 0) {
				removeAllMatches();
				//sentence.decrementIndex();
				//sentence.setCurrentPosition(this.lastMatchPosition);
			} else {
				//this.lastMatchPosition = sentence.getCurrentPosition();
			}
			setMatchItem(matchItem);
			phrases.remove(0);
			sentence.updateIndex(matchItem);
			//this.lastMatchPosition = sentence.getCurrentPosition();
			return true;
		} else {
			isOptionLeft = false;
			return false;
		}
	}
	
	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	@Override
	public String toString() {
		String slotTypeStr = (slotType == SlotToken.SlotType.NONE) ? "" : ", slot: ["+slotType+"]";
		return "Phrase: " + value + slotTypeStr; 
	}
	
	@Override
	public List<Tree> getCause() {
		if (this.slotType == SlotToken.SlotType.CAUSE_SLOT) {
			return this.matchItems;
		} else return null;
	}

	@Override
	public List<Tree> getEffect() {
		if (this.slotType == SlotToken.SlotType.EFFECT_SLOT) {
			return this.matchItems;
		} else return null;
	}
	
	public void resetToken() {
		super.resetToken();
		this.phrases = null;
	}

}
