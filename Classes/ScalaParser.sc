ScalaParser {

	*new {
		^super.new.init();
	}

	init {

	}

	*parse {
		| scalastring |
		var state = this.pr_scalaParser.run(scalastring);
		if (state.isError) {
			state.prettyprint; // show error message and return nil
			^nil;
		} {
			^state.result;
		}
	}

	*parseFile {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^parse(contents.join("\n"));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
			^nil
		};
	}

	*pr_restofline {
		^Optional(RegexParser("[^\\r\\n]+"));
	}

	*pr_nopoint {
		^NegativeLookAhead(StrParser("."));
	}

	*pr_comment {
		// store comment as an event (\what: \comment, \value: ...)
		^SequenceOf([StrParser("!"), this.pr_restofline]).map({|result| (\what: \comment, \value: result[1]) });
	}

	*pr_commentLine {
		// throw away the newline
		^SequenceOf([this.pr_comment, ParserFactory.makeNewlineParser]).map({|result| result[0] });
	}

	*pr_pitchRatio {
		^SequenceOf([
			ParserFactory.makeWs,
			ParserFactory.makeDigits,
			this.pr_nopoint,
			Optional(SequenceOf([
				ParserFactory.makeWs,
				StrParser("/"),
				ParserFactory.makeWs,
				ParserFactory.makeDigits,
				this.pr_nopoint
			])),
			this.pr_restofline]).map({
			| result |
			if (result[3].notNil) {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: result[3][3].asInteger);
			} {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: 1);
			};
		});
	}

	*pr_pitchPrimeVector {
		var ratio = SequenceOf([
			ParserFactory.makeWs,
			ParserFactory.makeIntegerParser,
			ParserFactory.makeWs,
			StrParser("/"),
			ParserFactory.makeWs,
			ParserFactory.makeIntegerParser
		]);
		var exp = SequenceOf([
			ParserFactory.makeWs,
			Choice([
				ratio.map({
					|result|
					(\what: \primeexponent, \kind: \ratio, \numerator: result[1], \denominator: result[5])
				}),
				ParserFactory.makeIntegerParser.map({
					| result|
					(\what : \primeexponent, \kind: \ratio, \numerator: result, \denominator: 1)
				})
			])
		]).map({|result| result[1] });
		^SequenceOf([
			ParserFactory.makeWs,
			StrParser("|"),
			ManyOne(exp),
			ParserFactory.makeWs,
			StrParser(">"),
			this.pr_restofline
		]).map({|result| (\kind: \primevector, \exponents: result[2]) });
	}

	*pr_pitchCents {
		var ws = ParserFactory.makeWs;
		var d = ParserFactory.makePositiveFloatParser;
		^SequenceOf([
			ParserFactory.makeWs,
			ParserFactory.makePositiveFloatParser,
			this.pr_restofline
		]).map({
			| result |
			(\what: \pitch, \kind: \cents, \numerator: result[1].asFloat, \denominator: 1);
		});
	}

	*pr_pitchParser {
		^Choice([
			this.pr_pitchPrimeVector,
			this.pr_pitchRatio,
			this.pr_pitchCents
		]);
	}

	*pr_scalaParser {
		^SequenceOf([
			// comments followed by description
			SequenceOf([
				Many(this.pr_commentLine),
				SequenceOf([this.pr_restofline, ParserFactory.makeNewlineParser])
			]).map({ | result | (\what: \description, \value:result[1][0]) }),
			// number of notes
			SequenceOf([
				ParserFactory.makeWs,
				ParserFactory.makePositiveIntegerParser,
				this.pr_restofline,
				ParserFactory.makeNewlineParser
			]).map({ |result| (\what: \numberofnotes, \value: result[1]) }).chain({
				| result |
				var no_of_notes = result[\value];
				var lst_of_parser = [SucceedParser((\what: \pitch, \kind: \cents, \numerator:0, \denominator:1))]; // add implicitly present entry 0
				(no_of_notes - 1).do {
					lst_of_parser = lst_of_parser.add(SequenceOf([
						Many(this.pr_commentLine),
						SequenceOf([
							this.pr_pitchParser,
							ParserFactory.makeNewlineParser])
					]).map({ | result | result[1][0] }))
				};
				SequenceOf(lst_of_parser);
			}),
			this.pr_pitchRatio.map({ |result| (\what: \intervalrepeat, \numerator: result[\numerator], \denominator: result[\denominator])})
		]).map({ | result |
			(\description: result[0][\value],
				\degrees: result[1],
				\repeatinterval: result[2])
		});
	}
}
