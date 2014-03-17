package patternMatcher.demo.patterns.tokenTypes;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import patternMatcher.demo.wrappers.SentenceWrapper;

// pos tag token implementation, @V, [v:1], [v:2]
public class POSTagToken extends Token implements IPatternSlot, IFlagToken {

	private int slotType;
	private boolean flagged;
	
	public POSTagToken(String substring) {
		super(substring);
		this.flagged = (substring.endsWith("~"));
		this.value = (this.flagged) ? this.value.substring(0, this.value.length()-1) : this.value;
		this.isOptionLeft = true;
		slotType = SlotToken.SlotType.NONE;
	}
	
	// constructor for slot POS tokens
	public POSTagToken(String value, int slotType) {
		super(value.toUpperCase());
		this.slotType = slotType;
		this.isOptionLeft = true;
	}

	@Override
	public boolean match(SentenceWrapper sentence) {
		Tree matchItem = sentence.getNextLeaf();
		isOptionLeft = false;
		this.lastMatchPosition = sentence.getCurrentPosition();
		
		if (matchItem != null && sentence.getTag(matchItem).startsWith(this.value) && (this.flagged || (this.slotType != SlotToken.SlotType.NONE) || !sentence.isFlagged(matchItem))) {
			setMatchItem(matchItem);
			//this.lastMatchPosition = sentence.getCurrentPosition();
			return true;
		} else {
			sentence.backtrack();
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
		return "POS tag: " + value + slotTypeStr; 
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

	@Override
	public void flag(SentenceWrapper sentence) {
		if (!this.flagged && (this.slotType == SlotToken.SlotType.NONE)) {
			for (Tree tree : this.matchItems) {
				if (tree != null) sentence.flagLeaf(tree);
			}
		}		
	}
}
