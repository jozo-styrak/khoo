import org.sbtidea.SbtIdeaPlugin._

organization := "ntnu.khoo"

name := "khoo"

version := "0.7"

libraryDependencies ++= Seq(
	"edu.stanford.nlp" % "stanford-corenlp" % "3.3.1",
	"edu.stanford.nlp" % "stanford-corenlp" % "3.3.1" classifier "models")

ideaExcludeFolders := Seq(".idea", ".idea_modules")