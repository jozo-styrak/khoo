package ntnu.khoo;

import ntnu.khoo.patterns.PatternFactory;
import ntnu.khoo.patterns.output.PlainOutput;
import ntnu.khoo.wrappers.TextWrapper;

/*
 * main method for running of some tests and development
 * all settings of pattern files and other things is done by commenting or commenting out 
 */
public class Main {

	public static void main(String[] args) throws Exception {
		
		PatternFactory patternFactory = new PatternFactory();
		patternFactory.loadSubpatterns("data/subpatterns/e1.e2.subpatterns.edited.data");
		patternFactory.loadVerbGroups("data/verb_groups/verb.groups.e1.e2.conj.data");
		patternFactory.loadPatternsDefinitions("data/patterns/e1.patterns.edited.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e2.patterns.data");
//		patternFactory.loadSubpatterns("data/subpatterns/e3.subpatterns.edited.data");
//		patternFactory.loadVerbGroups("data/verb_groups/verb.groups.shortened.02.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e3.patterns.ver03.data");
		
//		patternFactory.loadSubpatterns("data/subpatterns/e1e2.subpatterns.original.data");
//		patternFactory.loadVerbGroups("data/verb_groups/e1e2.verbs.original.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e1.original.data");
//		patternFactory.loadSubpatterns("data/subpatterns/e3.subpatterns.original.data");
//		patternFactory.loadVerbGroups("data/verb_groups/e3.verbs.original.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e3.original.data");		
		
		TextWrapper text = new TextWrapper();
//		text.loadAndParseTextFile("data/test/test.txt", true);
		text.addOutputStream(new PlainOutput(System.out));
		text.loadAndParseTextFileWithPositions("data/test/a01p0061.txt");
		text.extractRelationsLazy(patternFactory);
//		text.extractRelationsFromSentencePairs(patternFactory);
//		patternFactory.printUsageData(System.out);
		
	}

}
