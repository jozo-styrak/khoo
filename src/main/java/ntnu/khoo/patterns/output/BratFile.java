package ntnu.khoo.patterns.output;

import java.io.PrintStream;
import java.util.List;

import ntnu.khoo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

public class BratFile {
	
	private int phraseIndex;
	private int relationIndex;
	private PrintStream out;
	
	public BratFile(PrintStream out) {
		this.out = out;
		this.phraseIndex = 1;
		this.relationIndex = 1;
	}
	
	public void addRelation(SentenceWrapper sentence, List<Tree> cause, List<Tree> effect) {
		// create strings from tree objects
		String causeString = this.treeListToString(cause);
		String effectString = this.treeListToString(effect);
		
		// get positions
		int causeOffsetStart = sentence.getSentencePosition(cause.get(0)) + this.getOffset(sentence, cause.get(0));
		int causeOffsetEnd = sentence.getSentencePosition(cause.get(cause.size()-1)) + this.getOffset(sentence, cause.get(cause.size()-1)) + cause.get(cause.size()-1).label().toString().length();
		int effectOffsetStart = sentence.getSentencePosition(effect.get(0)) + this.getOffset(sentence, effect.get(0));
		int effectOffsetEnd = sentence.getSentencePosition(effect.get(effect.size()-1)) + this.getOffset(sentence, effect.get(effect.size()-1)) + effect.get(effect.size()-1).label().toString().length();
		
		// print out
		this.out.println("T" + this.phraseIndex  + "\tCause " + causeOffsetStart + " " + causeOffsetEnd + "\t" + causeString);
		this.phraseIndex++;
		this.out.println("T" + this.phraseIndex  + "\tEffect " + effectOffsetStart + " " + effectOffsetEnd + "\t" + effectString);
		this.phraseIndex++;
		this.out.println("E" + this.relationIndex + "\tCause:T" + (this.phraseIndex-2) + " Effect:T" + (this.phraseIndex-1));
		this.relationIndex++;
	}
	
	private int getOffset(SentenceWrapper sentence, Tree leaf) {
		
		int count = 0;
		List<Tree> leaves = sentence.getRootTree(leaf).getLeaves();
		
		int i = (leaves.get(0).label().toString().compareTo(".") == 0) ? 1 : 0;
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
	
	private String treeListToString(List<Tree> nodes) {
		String s = "";
		for (Tree node : nodes) {
			s += node.label().toString() + " ";
		}
		return s.trim();
	}	

}
