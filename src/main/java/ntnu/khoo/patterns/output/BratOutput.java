package ntnu.khoo.patterns.output;

import java.io.PrintStream;
import java.util.List;

import ntnu.khoo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

// brat output - marking phrases position in text
public class BratOutput extends Output {
	
	private int phraseIndex;
	private int relationIndex;
	
	public BratOutput(PrintStream out) {
		super(out);
		this.phraseIndex = 1;
		this.relationIndex = 1;
	}
	
	@Override
	public void printRelation(SentenceWrapper sentence, List<Tree> cause, List<Tree> effect) {
		
		List<Tree> causeAdj = (cause.get(0).label().toString().compareTo(".") == 0) ? cause.subList(1, cause.size()) : cause;
		List<Tree> effectAdj = (effect.get(0).label().toString().compareTo(".") == 0) ? effect.subList(1, effect.size()) : effect;
		
		// create strings from tree objects
		String causeString = this.treeListToString(causeAdj);
		String effectString = this.treeListToString(effectAdj);
		
		// get positions
		int causeOffsetStart = sentence.getSentencePosition(causeAdj.get(0)) + this.getOffset(sentence, causeAdj.get(0));
		int causeOffsetEnd = sentence.getSentencePosition(causeAdj.get(causeAdj.size()-1)) + this.getOffset(sentence, causeAdj.get(causeAdj.size()-1)) + causeAdj.get(causeAdj.size()-1).label().toString().length();
		int effectOffsetStart = sentence.getSentencePosition(effectAdj.get(0)) + this.getOffset(sentence, effectAdj.get(0));
		int effectOffsetEnd = sentence.getSentencePosition(effectAdj.get(effectAdj.size()-1)) + this.getOffset(sentence, effectAdj.get(effectAdj.size()-1)) + effectAdj.get(effectAdj.size()-1).label().toString().length();
		
		// print out
		this.out.println("T" + this.phraseIndex  + "\tCause " + causeOffsetStart + " " + causeOffsetEnd + "\t" + causeString);
		this.phraseIndex++;
		this.out.println("T" + this.phraseIndex  + "\tEffect " + effectOffsetStart + " " + effectOffsetEnd + "\t" + effectString);
		this.phraseIndex++;
		this.out.println("E" + this.relationIndex + "\tCause:T" + (this.phraseIndex-2) + " Effect:T" + (this.phraseIndex-1));
		this.relationIndex++;
	}
	
	// get the position of given leaf within scope of sentence
	private int getOffset(SentenceWrapper sentence, Tree leaf) {
		
		int count = 0;
		List<Tree> leaves = sentence.getRootTree(leaf).getLeaves();
		
		int i = (leaves.get(0).label().toString().compareTo(".") == 0) ? 1 : 0;
//		int count = (leaves.get(0).label().toString().compareTo(".") == 0) ? -2 : 0;
//		int i = 0;
		boolean found = false;
		
		while (i < leaves.size() && !found) {
			if (leaf == leaves.get(i)) {
				found = true;
				if (leaf.label().toString().compareTo(",") == 0) {
					count--;
				}
			} else {
				count += leaves.get(i).label().toString().length();
				if (!(leaves.get(i).label().toString().compareTo(".") == 0 || leaves.get(i).label().toString().compareTo(",") == 0 || leaves.get(i).label().toString().compareTo("\"") == 0)) {
					count++;
				}
				i++;
			}
		}
		
		return count;
		
	}	

}
