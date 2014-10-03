Implementation of the causality extraction system used in

Sizov, G., Öztürk, P., & Štyrák, J. (2014). Acquisition and Reuse of Reasoning Knowledge from Textual Cases for Automated Analysis. In Case-Based Reasoning Research and Development (pp. 465-479). Springer International Publishing.

and originally described in:

Khoo, C.S.G.: Automatic identifcation of causal relations in text and their use for improving precision in information retrieval. Ph.D. thesis, The University of Arizona (1995)

The system uses Stanford Core NLP parser (3.3.1) that should be added as a dependency to the project.

Two main classes:

	patternMatcher.demo.Main - mainly used for debugging

	patternMatcher.demo.ExtractRelations - used for extracting relations from sets of texts
		two arguments: -f [directory_from] -t [output_directory] -n [number of threads, default 1]
