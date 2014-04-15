Khoo
====

causality extraction system based on Khoo's thesis

currently no dependency management, however, system uses Stanford Core NLP parser (3.3.1)

simple usage:

two main classed

	patternMatcher.demo.Main - mainly used for debugging
	
	patternMatcher.demo.ExtractRelations - used for extracting relations from sets of texts
		two arguments: -f [directory_from] -t [output_directory] -n [number of threads, default 1]	
