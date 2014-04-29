package ntnu.khoo.wrappers;

import edu.stanford.nlp.trees.Tree;

// wrapper for two sentences
public class SentencePairWrapper extends SentenceWrapper {

	private Tree ROOT_2;
	private int sentencePositionAppended;
	
	public SentencePairWrapper(Tree parseTree1, Tree parseTree2) {
		super();
		this.ROOT = parseTree1;
		this.ROOT_2 = parseTree2;
		this.processOrder = this.ROOT.preOrderNodeList();
		this.processOrder.addAll(this.ROOT_2.preOrderNodeList());
		this.sentencePositionAppended = -1;
		this.sentencePosition = -1;
		this.initialize();
	}
	
	// add new sentence to wrapper
	public void addSentence(Tree parseTree) {
		// remove first sentence
		for (int i = 0;  i < this.ROOT.size(); i++) {
			this.processOrder.remove(0);
			this.flagged.remove(0);
		}
		this.ROOT = this.ROOT_2;
		this.ROOT_2 = parseTree;
		// append new sentence
		for (Tree node : parseTree.preOrderNodeList()) {
			this.processOrder.add(node);
			this.flagged.add(false);
		}

		this.currentPosition = 1;
	}
	
	/*
	 * overriden methods from SentenceWrapper
	 */
	public String getTag(Tree tree) {
		try {	
			String tag = tree.parent(this.ROOT).label().toString();
			return tag;
		} catch (Exception e1) {
			try {
				String tag = tree.parent(this.ROOT_2).label().toString();
				return tag;
			} catch (Exception e2) {
				return "X";
			}
		}
	}
	
	public void resetProcessOrder() {
		processOrder = ROOT.preOrderNodeList();
		processOrder.addAll(ROOT_2.preOrderNodeList());
		this.currentPosition = 1;
	}
	
	public boolean containsLiteral(String value, boolean isWildcard) {
		return containsLiteral(this.ROOT, value, isWildcard) || containsLiteral(this.ROOT_2, value, isWildcard);
	}
	
	public String getSentencePairString() {
		String out = "";
		for (Tree tree : this.processOrder) {
			if (tree.isLeaf()) {
				out += tree.label().toString() + " ";
			}
		}
		return out;
	}
	
	public int getSentencePosition(Tree leaf) {
		int position = -1;
		for (Tree tree : this.ROOT.getLeaves()) {
			if (leaf == tree) {
				position = this.sentencePosition;
			}
		}
		if (position == -1) {
			for (Tree tree : this.ROOT_2.getLeaves()) {
				if (leaf == tree) {
					position = this.sentencePositionAppended;
				}
			}
		}
		return position;
	}

	public void setSentencePosition(int sentencePosition) {
		if (this.sentencePositionAppended != -1) {
			this.sentencePosition = this.sentencePositionAppended;
			this.sentencePositionAppended = sentencePosition;
		} else if (this.sentencePosition == -1) {
			this.sentencePosition = sentencePosition;
		} else if (this.sentencePositionAppended == -1) {
			this.sentencePositionAppended = sentencePosition;
		}
	}

//	public String getSentenceValue() {
//		return sentenceValue;
//	}
//
//	public void setSentenceValue(String sentenceValue) {
//		this.sentenceValue = sentenceValue;
//	}
	
	public Tree getRootTree(Tree leaf) {
		Tree root = null;
		for (Tree tree : this.ROOT.getLeaves()) {
			if (leaf == tree) {
				root = this.ROOT;
			}
		}
		if (root == null) {
			for (Tree tree : this.ROOT_2.getLeaves()) {
				if (leaf == tree) {
					root = this.ROOT_2;
				}
			}
		}
		return root;		
	}

}
