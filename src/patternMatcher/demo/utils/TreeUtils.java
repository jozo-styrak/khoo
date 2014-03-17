package patternMatcher.demo.utils;

import java.util.List;

import edu.stanford.nlp.trees.Tree;

/*
 * class for preprocessing of parse tree
 * in original thesis they used DRLINK phrase bracketer, this utility tries to emulate some phrase types
 */
public class TreeUtils {
	
	public static boolean isConjunctionOfNounPhrases(List<Tree> children) {
		return children.size() == 3 && (TreeUtils.isPhraseType(children.get(0), "NP") || TreeUtils.isLeafLabel(children.get(0), "NN")) && (TreeUtils.isPhraseType(children.get(2), "NP") || TreeUtils.isLeafLabel(children.get(2), "NN")) && TreeUtils.isLeafLabel(children.get(1), "CC");
	}
	
	public static boolean isPhraseType(Tree node, String type) {
		return node.isPhrasal() && node.label().toString().compareTo(type) == 0;
	}
	
	public static boolean isLeafLabel(Tree node, String type) {
		return node.isPreTerminal() && node.label().toString().compareTo(type) == 0;
	}
	
	public static void preprocessPhraseLabels(Tree tree) {
		for (Tree node : tree.preOrderNodeList()) {
			// tag M as conjunction of two noun phrases
			if (TreeUtils.isPhraseType(node, "NP") && TreeUtils.isConjunctionOfNounPhrases(node.getChildrenAsList())) {
				node.label().setValue("M");
			// tag G for phrase starting with verb in VBG
			} else if ((TreeUtils.isPhraseType(node, "NP") || TreeUtils.isPhraseType(node, "VP")) && node.getLeaves().get(0).parent(node).label().toString().compareTo("VBG") == 0) {
				node.label().setValue("G");
			// tag D for phrase starting with verb in VBN
			} else if ((TreeUtils.isPhraseType(node, "NP") || TreeUtils.isPhraseType(node, "VP")) && node.getLeaves().get(0).parent(node).label().toString().compareTo("VBN") == 0) {
			 	node.label().setValue("D");
			}
		}
	}

}
