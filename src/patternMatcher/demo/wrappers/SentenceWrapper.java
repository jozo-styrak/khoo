package patternMatcher.demo.wrappers;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/*
 * holds one sentence
 */
public class SentenceWrapper {
	
	// for prefix purposes - suplies '.' tree for beginning of sentence
	public static String DOT_SENTENCE = "A .";
	protected Tree dotSentence;
	
	// whole parse tree
	protected Tree ROOT;
	
	// list order of processing
	protected List<Tree> processOrder;
	
	// whether given element of sentence is flagged
	protected List<Boolean> flagged;
		
	// index representing current position in sentence 
	protected int currentPosition;
	
	public SentenceWrapper() {}
	
	public SentenceWrapper(String sentence, LexicalizedParser parser) {
		this.ROOT = parser.parse(sentence);
		
		// based on original thesis '.' should be added to the beginning of sentence
		// added to already parsed tree, because in sentence pair there would be two connected sentences by two dots
		this.dotSentence = parser.parse(DOT_SENTENCE);
		this.ROOT.addChild(0, this.dotSentence.getLeaves().get(1).parent(this.dotSentence));
		
		// order left child first, better than list of children because of usage of phrase matching tokens
		this.processOrder = ROOT.preOrderNodeList();
		this.initialize();
	}
	
	// alternative constructor
	public SentenceWrapper(Tree root, LexicalizedParser parser) {
		this.ROOT = root;
		if (this.ROOT.getLeaves().get(0).label().toString().compareTo(".") != 0) {
			this.dotSentence = parser.parse(DOT_SENTENCE);
			this.ROOT.addChild(0, this.dotSentence.getLeaves().get(1).parent(this.dotSentence));
		}
		this.processOrder = ROOT.preOrderNodeList();
		this.initialize();
	}
	
	// initialize indexes and flag list
	protected void initialize() {
		this.currentPosition = 1;
		this.flagged = new ArrayList<Boolean>();
		for (int i = 0; i < this.processOrder.size(); i++) {
			this.flagged.add(false);
		}
	}
	
	// returns list of phrases of given type in curent branch
	public List<Tree> getCurrentBranchPhrasesWithTag(String tag) {
		List<Tree> phrases = new ArrayList<Tree>();
		int i = this.currentPosition;
		while ((i < processOrder.size()) && !processOrder.get(i).isLeaf()) {
			//if (processOrder.get(i).isPhrasal() && (processOrder.get(i).label().value().compareTo(tag) == 0)) {
			if ((processOrder.get(i).isPhrasal() || processOrder.get(i).isPreTerminal()) && processOrder.get(i).label().value().startsWith(tag)) {
				phrases.add(processOrder.get(i));
			} 
			i++;
		}
		return phrases;
	}
	
	// update of sentence index position after matching of phrase
	public void updateIndex(Tree phrase) {
		int i = this.currentPosition;
		boolean phraseFound = false;
		while ((i < processOrder.size()) && !phraseFound) {
			if (processOrder.get(i) == phrase) {
				phraseFound = true;
			} else {
				i++;
			}
		}
		if (phraseFound) {
			this.currentPosition = i + phrase.size();
		}
	}
	
	// returns closest leaf from process order
	// means, next word
	public Tree getNextLeaf() {
		int i = this.currentPosition;
		Tree leaf = null;
		while ((i < processOrder.size()) && !processOrder.get(i).isLeaf()) {
			i++;
		}
		if (i < processOrder.size()) {
			leaf = processOrder.get(i);
			this.currentPosition = i + 1;
		}
		return leaf;
	}
	
	// returns tag for given word
	public String getTag(Tree tree) {
		return tree.parent(ROOT).label().toString();
	}
	
	// method currently unused
	public void incrementIndex() {
		this.currentPosition++;
	}
	
	// method currently unused
	public void decrementIndex() {
		this.currentPosition--;
	}
	
	// returns to the position of previous leaf
	public void backtrack() {
		int i = this.currentPosition - 2;
		while (i > 0 && !this.processOrder.get(i).isLeaf()) {
			i--;
		}
		this.currentPosition = i + 1;
	}
	
	// flag given word
	public void flagLeaf(Tree leaf) {
		int i = 0;
		Tree tree = null;
		while ((tree == null) && (i < this.processOrder.size())) {
			if (this.processOrder.get(i) == leaf) {
				tree = this.processOrder.get(i);
			}
			i++;
		}
		if (tree != null) {
			this.flagged.set(i-1, true);
		}
	}
	
	// reinitialization of process index position
	public void resetProcessOrder() {
		processOrder = ROOT.preOrderNodeList();
		this.currentPosition = 1;
	}
	
	// is given word flagged?
	public boolean isFlagged(Tree leaf) {
		int i = 0;
		Tree tree = null;
		while ((tree == null) && (i < this.processOrder.size())) {
			if (this.processOrder.get(i) == leaf) {
				tree = this.processOrder.get(i);
			}
			i++;
		}
		if (tree != null) {
			return this.flagged.get(i-1);
		} else {
			return false;
		}
	}
	
	// something left in sentence?
	public boolean isConsumed() {
		return (this.currentPosition > (this.processOrder.size()-1));
	}
	
	// debug method
	public void printProcessOrder() {
		System.out.println("SENTENCE PROCESS ORDER");
		for (Tree node : processOrder) {
			System.out.println(node + "->" + node.label().getClass().getSimpleName());
		}
		System.out.println();
	}
	
	// one line print
	public void printProcessOrderInline() {
		System.out.println(this.processOrder);
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	/*
	 * filter methods - whether given sentence contains given literal
	 */
	public boolean containsLiteral(String value, boolean isWildcard) {
		return containsLiteral(this.ROOT, value, isWildcard);
	}
	
	protected boolean containsLiteral(Tree root, String value, boolean isWildcard) {
		boolean contains = false;
		for (Tree leaf : root.getLeaves()) {
			if (isWildcard && leaf.label().toString().toLowerCase().startsWith(value.toLowerCase())) {
				contains = true;
			} else if (!isWildcard && (leaf.label().toString().toLowerCase().compareTo(value.toLowerCase()) == 0)) {
				contains = true;
			}
		}
		return contains;		
	}
}
