package ntnu.khoo.patterns.token;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import ntnu.khoo.wrappers.SentenceWrapper;

// simple slot token implementation [1] [2]
public class SlotToken extends Token implements IPatternSlot {

	private int slotType;
	
	// last in pattern order
	private boolean lastToProcess;
	
	private String punctuation = ".,-:()?!;";
	
	public interface SlotType {
		public int NONE = 0;
		public int CAUSE_SLOT = 1;
		public int EFFECT_SLOT = 2;
	}
	
	public SlotToken(int slotType) {
		super("");
		this.slotType = slotType;
		this.isOptionLeft = true;
		this.lastToProcess = false;
	}
	
	@Override
	public boolean match(SentenceWrapper sentence) {
//		int checkpoint = sentence.getCurrentPosition();
		if (!this.lastToProcess) {
			Tree matchItem = sentence.getNextLeaf();
			if (matchItem != null) {
				// has to match more than punctuation
				if ((this.matchItems.size() == 0) && (this.punctuation.indexOf(matchItem.label().value()) != -1)) {
					Tree nextItem = sentence.getNextLeaf();
					if (nextItem != null) { 
						setMatchItem(matchItem);
						setMatchItem(nextItem);
						this.lastMatchPosition = sentence.getCurrentPosition();
						return true;
					} else {
						sentence.backtrack();
//						sentence.setCurrentPosition(checkpoint);
						this.isOptionLeft = false;
						return false;
					}				
				} else {
					setMatchItem(matchItem);
					this.lastMatchPosition = sentence.getCurrentPosition();
					return true;
				}
			} else {
				this.isOptionLeft = false;
				return false;
			}
		} else {
			// last to process matches everything left in sentence
			Tree matchItem = sentence.getNextLeaf();
			while ((matchItem != null) && !sentence.isConsumed()) {
				setMatchItem(matchItem);
				matchItem = sentence.getNextLeaf();
			}
			if ((this.matchItems.size() == 1) && (this.punctuation.contains(matchItem.label().value()))) {
				this.removeMatchItem();
				sentence.backtrack();
//				sentence.setCurrentPosition(checkpoint);
				return false;
			}
			return true;
		}
	}

	@Override
	public boolean isOptionLeft() {
		return isOptionLeft;
	}
	
	@Override
	public String toString() {
		return "Slot: ["+slotType+"]"; 
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

	public void setLastToProcess(boolean lastToProcess) {
		this.lastToProcess = lastToProcess;
	}

}
