package ntnu.khoo.patterns.output;

import java.io.PrintStream;
import java.util.List;

import ntnu.khoo.wrappers.SentenceWrapper;
import edu.stanford.nlp.trees.Tree;

// output where cause and effect are separated on tabulator
public class PlainOutput extends Output {

	public PlainOutput(PrintStream out) {
		super(out);
	}
	
	@Override
	public void printRelation(SentenceWrapper sentence, List<Tree> cause, List<Tree> effect) {
		this.out.println(this.treeListToString(cause) + "\t" + this.treeListToString(effect));
	}	

}
