package patternMatcher.demo;

import patternMatcher.demo.patterns.PatternFactory;
import patternMatcher.demo.wrappers.TextWrapper;

/*
 * main method for running of some test
 */
public class Main {

	public static void main(String[] args) throws Exception {
		
		PatternFactory patternFactory = new PatternFactory();
//		patternFactory.loadSubpatterns("data/subpatterns/e1.e2.subpatterns.data");
//		patternFactory.loadVerbGroups("data/verb_groups/verb.groups.e1.e2.conj.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e1.patterns.edited.data");
//		patternFactory.loadPatternsDefinitions("data/patterns/e2.patterns.data");
		patternFactory.loadSubpatterns("data/subpatterns/e3.subpatterns.edited.data");
		patternFactory.loadVerbGroups("data/verb_groups/verb.groups.e3.ver03.data");
		patternFactory.loadPatternsDefinitions("data/patterns/e3.patterns.ver03.data");
		
		//String s4 = "It is slippery because it has rained.";
		//String s5 = "The fact that the aircraft could not be reached did not alarm the company flight following because it was not unusual for aircraft to be out of radio range of the flight watch facility.";
		//String s7 = "That is an example, how crucial weather is to the success of tournament.";
		//String s8 = "It is nice and therefore i like it.";
		//String s9 = "The lack of an effective means of tracking the flight progress led to delays in SAR action. ";
		//String s9 = "This is a test sentence.";
		//String s10 = "The 121.5 MHz emergency locator transmitter (ELT) was destroyed on impact and the antenna was severed from its connection; therefore, it could not fulfill its role in signaling the accident.";
		//String s11 = "Lack of adequate instrument redundancy increases the risk of loss of control in single-pilot instrument flight rules (IFR) aircraft operations.";
		//patternFactory.addPattern(true, "*** &S &\"_ to &ADV_ [v:2] &[2], [1] &._");
		TextWrapper text = new TextWrapper();
		//text.loadTextFileAndAnnotate("data/aviation_reports/brat/a08q0171.txt");
		text.loadAndParseTextFile("data/aviation_reports/demo.txt", true);
		//text.setOutputStream("data/output/a08q0110-e2.out.txt");
		//text.addSentence(s4);
		//text.addSentence(s5);
		//text.addSentence(s6);
		//text.addSentence(s7);
		//text.addSentence(s9);
		//text.addSentence(s10);
		//text.addSentence(s11);
		//text.extractRelations(patternFactory);
		text.extractRelationsLazy(patternFactory);
//		text.extractRelationsFromSentencePairs(patternFactory);
//		patternFactory.printUsageData(System.out);
		
	}

}
