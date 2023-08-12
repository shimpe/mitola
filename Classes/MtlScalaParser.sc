/*
[general]
title = "MtlScalaParser"
summary = "a parser for scala files and strings"
categories = "Microtonal utils"
related = "Classes/MtlScalaCalculator, Classes/MtlMitola"
description = '''
MtlScalaParser implements a parser for the contents of a scala file
'''
*/
MtlScalaParser {
	/*
	[classmethod.new]
	description='''
	new creates a new MtlScalaParser
	'''
	[classmethod.new.returns]
	what="a MtlScalaParser"
	*/
	*new {
		^super.new.init();
	}

	/*
	[method.init]
	description='''
	init initializes a new MtlScalaParser
	'''
	[method.init.returns]
	what="a MtlScalaParser ready to do some parsing"
	*/
	init {

	}

	/*
	[classmethod.parse]
	description='''
	parse takes a scala string (= contents of scala file) and parses it to parse tree
	'''
	[classmethod.parse.args]
	scalastring = "a string containing a scala specification"
	[classmethod.parse.returns]
	what = '''
	if parsing fails: prints a message and returns nil
	if parsing succeeds: returns the parse tree
	'''
	*/
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

	/*
	[classmethod.parse_file]
	description='''
	parse takes a scala filename and parses its contents to a parse tree
	'''
	[classmethod.parse_file.args]
	filename = "a string containing a path to a scala file"
	[classmethod.parse_file.returns]
	what = '''
	if loading/parsing fails: prints a message and returns nil
	if parsing succeeds: returns the parse tree
	'''
	*/
	*parse_file {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^parse(contents.join("\n"));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
			^nil
		};
	}

	/*
	[classmethod.pr_restofline]
	description='''
	makes a ScpParser that eats the rest of a line (but not the newline)
	'''
	[classmethod.pr_restofline.returns]
	what = "a ScpParser"
	*/
	*pr_restofline {
		^ScpOptional(ScpRegexParser("[^\\r\\n]+"));
	}

	/*
	[classmethod.pr_nopoint]
	description='''
	makes a ScpParser that fails if the next token is a point
	'''
	[classmethod.pr_nopoint.returns]
	what = "a ScpParser"
	*/
	*pr_nopoint {
		^ScpNegativeLookAhead(ScpStrParser("."));
	}

	/*
	[classmethod.pr_comment]
	description='''
	makes a ScpParser that matches a comment in a scala file (not the newline)
	'''
	[classmethod.pr_comment.returns]
	what = "a ScpParser"
	*/
	*pr_comment {
		// store comment as an event (\what: \comment, \value: ...)
		^ScpSequenceOf([ScpStrParser("!"), this.pr_restofline]).map({|result| (\what: \comment, \value: result[1]) });
	}

	/*
	[classmethod.pr_commentLine]
	description='''
	makes a ScpParser that matches a comment in a scala file (including the newline)
	'''
	[classmethod.pr_commentLine.returns]
	what = "a ScpParser"
	*/
	*pr_commentLine {
		// throw away the newline
		^ScpSequenceOf([this.pr_comment, ScpParserFactory.makeNewlineParser]).map({|result| result[0] });
	}

	/*
	[classmethod.pr_pitchRatio]
	description='''
	makes a ScpParser that matches a pitch line specified as a ratio Integer/Integer. No sign is allowed.
	'''
	[classmethod.pr_pitchRatio.returns]
	what = "a ScpParser"
	*/
	*pr_pitchRatio {
		^ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpParserFactory.makeDigits,
			this.pr_nopoint,
			ScpOptional(ScpSequenceOf([
				ScpParserFactory.makeWs,
				ScpStrParser("/"),
				ScpParserFactory.makeWs,
				ScpParserFactory.makeDigits,
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

	/*
	[classmethod.pr_pitchPrimeVector]
	description='''
	makes a ScpParser that matches a pitch line specified in prime vector notation | exp1 exp2 ... >.
	'''
	[classmethod.pr_pitchPrimeVector.returns]
	what = "a ScpParser"
	*/
	*pr_pitchPrimeVector {
		var ratio = ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpParserFactory.makeIntegerParser,
			ScpParserFactory.makeWs,
			ScpStrParser("/"),
			ScpParserFactory.makeWs,
			ScpParserFactory.makeIntegerParser
		]);
		var exp = ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpChoice([
				ratio.map({
					|result|
					(\what: \primeexponent, \kind: \ratio, \numerator: result[1], \denominator: result[5])
				}),
				ScpParserFactory.makeIntegerParser.map({
					| result|
					(\what : \primeexponent, \kind: \ratio, \numerator: result, \denominator: 1)
				})
			])
		]).map({|result| result[1] });
		^ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpStrParser("|"),
			ScpManyOne(exp),
			ScpParserFactory.makeWs,
			ScpStrParser(">"),
			this.pr_restofline
		]).map({|result| (\kind: \primevector, \exponents: result[2]) });
	}

	/*
	[classmethod.pr_pitchCents]
	description='''
	makes a ScpParser that matches a pitch line specified in cents. Such line MUST include a decimal point, otherwise it's interpreted as a ratio.
	'''
	[classmethod.pr_pitchCents.returns]
	what = "a ScpParser"
	*/
	*pr_pitchCents {
		var ws = ScpParserFactory.makeWs;
		var d = ScpParserFactory.makePositiveFloatParser;
		^ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpParserFactory.makePositiveFloatParser,
			this.pr_restofline
		]).map({
			| result |
			(\what: \pitch, \kind: \cents, \numerator: result[1].asFloat, \denominator: 1);
		});
	}

	/*
	[classmethod.pr_pitchParser]
	description='''
	makes a ScpParser that matches a pitch line specified in one of the valid formats: cents, ratio or prime vector.
	'''
	[classmethod.pr_pitchParser.returns]
	what = "a ScpParser"
	*/
	*pr_pitchParser {
		^ScpChoice([
			this.pr_pitchPrimeVector,
			this.pr_pitchRatio,
			this.pr_pitchCents
		]);
	}


	/*
	[classmethod.pr_scalaParser]
	description='''
	makes a ScpParser that matches the contents of a scala file
	'''
	[classmethod.pr_scalaParser.returns]
	what = "a ScpParser"
	*/
	*pr_scalaParser {
		^ScpSequenceOf([
			// comments followed by description
			ScpSequenceOf([
				ScpMany(this.pr_commentLine),
				ScpSequenceOf([this.pr_restofline, ScpParserFactory.makeNewlineParser])
			]).map({ | result | (\what: \description, \value:result[1][0]) }),
			// number of notes
			ScpSequenceOf([
				ScpParserFactory.makeWs,
				ScpParserFactory.makePositiveIntegerParser,
				this.pr_restofline,
				ScpParserFactory.makeNewlineParser
			]).map({ |result| (\what: \numberofnotes, \value: result[1]) }).chain({
				| result |
				var no_of_notes = result[\value];
				var lst_of_parser = [ScpSucceedParser((\what: \pitch, \kind: \cents, \numerator:0, \denominator:1))]; // add implicitly present entry 0
				(no_of_notes - 1).do {
					lst_of_parser = lst_of_parser.add(ScpSequenceOf([
						ScpMany(this.pr_commentLine),
						ScpSequenceOf([
							this.pr_pitchParser,
							ScpParserFactory.makeNewlineParser])
					]).map({ | result | result[1][0] }))
				};
				ScpSequenceOf(lst_of_parser);
			}),
			this.pr_pitchRatio.map({ |result| (\what: \equivalenceinterval, \numerator: result[\numerator], \denominator: result[\denominator])})
		]).map({ | result |
			(\description: result[0][\value],
				\degrees: result[1],
				\equivalenceinterval: result[2])
		});
	}
}

/*
[examples]
what='''
(
var state;
var p = MtlScalaParser.pr_scalaParser;

var scala_txt = [
	"! meanquar.scl",
	"!",
	"1/4-comma meantone scale. Pietro Aarons temperament (1523)",
	" 12",
	"!",
	" 76.04900",
	" 193.15686",
	" 310.26471",
	" 5/4",
	" 503.42157",
	" 579.47057",
	" 696.57843",
	" 25/16",
	" 889.73529",
	" 1006.84314",
	" 1082.89214",
	" 2/1"
].join("\n");

state = p.run(scala_txt);
state.isError.debug("isError"); // expect: false
state.result[\description].debug("description"); // 1/4-comma meantone scale. Pietro Aarons temperament (1523)
state.result[\equivalenceinterval].debug("equave"); // ('what': 'equivalenceinterval', 'numerator': 2, 'denominator': 1)
state.result[\degrees].size.debug("size"); //12
state.result[\degrees][0].debug("first pitch"); // ('kind': 'cents', 'what': 'pitch', 'numerator': 0, 'denominator': 1)
)
'''
*/