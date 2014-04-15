package ntnu.khoo.patterns.token;

import java.util.List;

import edu.stanford.nlp.trees.Tree;

// interface for slot type tokens
public interface IPatternSlot {

	public List<Tree> getCause();
	
	public List<Tree> getEffect();
	
}
