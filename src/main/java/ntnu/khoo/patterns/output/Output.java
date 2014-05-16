package ntnu.khoo.patterns.output;

import java.io.PrintStream;
import java.util.List;

import ntnu.khoo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

// parent for output objects
public abstract class Output {
	
	protected PrintStream out;
	
	public Output(PrintStream out) {
		this.out = out;
	}
	
	public abstract void printRelation(SentenceWrapper sentence, List<Tree> cause, List<Tree> effect);
	
	public void println(String line) {
		this.out.println(line);
	}
	
	public String treeListToString(List<Tree> nodes) {
		String s = "";
		for (Tree node : nodes) {
			s += node.label().toString() + " ";
		}
		return s.trim();
	}

}
