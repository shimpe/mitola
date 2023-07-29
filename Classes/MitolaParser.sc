/*
[general]
title = "MitolaParser"
summary = "a parser for mitola scores - not for end users, use Mitola instead."
categories = "Microtonal utils"
related = "Classes/ScalaParser, Classes/Mitola"
description = '''
Mitola parser can parse a string or a file containing a valid Mitola score and convert it to a parse tree.
'''
*/
MitolaParser : Parser {
	/*
	[classmethod.new]
	description = "New creates a new MitolaParser"
	[classmethod.new.returns]
	what = "a new MitolaParser"
	*/
	*new {
		^super.new.init();
	}

	/*
	[method.init]
	description = "initializes a new MitolaParser"
	[method.init.returns]
	what = "an initialized MitolaParser object"
	*/
	init {

	}

	/*
	[method.parse]
	description = "parses a Mitola score; upon failure displays an error msg and returns nil"
	[method.parse.args]
	mitolastring = "a string containing a valid mitola score"
	[method.parse.returns]
	what = '''
	if successful, parse returns the parse tree
	if unsuccessful, a message is printed and nil is returned
	'''
	*/
	parse {
		| mitolastring |
		var state = MitolaParser.pr_mixedNotelist.run(mitolastring);
		if (state.isError) {
			state.prettyprint; // show error message and return nil
			^nil
		} {
			^state.result;
		}
	}

	/*
	[method.parse_file]
	description = "parses a Mitola score from file; upon failure displays an error msg and returns nil"
	[method.parse_file.args]
	filename = "a filename of a file containing a valid mitola score"
	[method.parse_file.returns]
	what='''
	if successful, parse returns the parse tree
	if unsuccessful, a message is printed and nil is returned
	'''
	*/
	parse_file {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^this.parse(contents.join(" "));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
			^nil
		};
	}

	/*
	[classmethod.pr_noteParser]
	description = "internal method that creates a parser that can parse a single mitola degree and markup the result"
	[classmethod.pr_noteParser.returns]
	what = "a Parser"
	*/
	*pr_noteParser {
		^ParserFactory.makePositiveIntegerParser.map({|result| (\what: \notename, \value: result-1) }); // use one-based counting
	}

	/*
	[classmethod.pr_plusminParser]
	description = "internal method that creates a parser that can parse a +/- sign as part of a pitch modifier (similar to a flat or sharp in traditional notation) and markup the result"
	[classmethod.pr_plusminParser.returns]
	what = "a Parser"
	*/
	*pr_plusminParser {
		^Choice([
			StrParser("+").map({|result| (\notemodifier:\raise) }),
			StrParser("-").map({|result| (\notemodifier:\lower) }),
		]);
	}

	/*
	[classmethod.pr_betweenCurlyBrackets]
	description = "internal method that creates a function that can create a parser to parse 'something' between curly brackets"
	[classmethod.pr_betweenCurlyBrackets.returns]
	what = "a function expecting a Parser for 'something' as argument, to create a Parser that parses 'something' between curly brackets"
	*/
	*pr_betweenCurlyBrackets {
		^ParserFactory.makeBetween(StrParser("{"), SequenceOf([ParserFactory.makeWs, StrParser("}")]));
	}

	/*
	[classmethod.pr_pitchRatio]
	description = "internal method that creates a Parser to parse a pitch modifier numerical value specified as a ratio and markup the result"
	[classmethod.pr_pitchRatio.returns]
	what = "a Parser parsing a pitch modifier numerical value ratio [Integer]/[Integer]"
	*/
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
			]))]).map({
			| result |
			if (result[3].notNil) {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: result[3][3].asInteger);
			} {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: 1);
			};
		});
	}

	/*
	[classmethod.pr_nopoint]
	description = "internal method that creates a Parser that fails if the next token is a point"
	[classmethod.pr_nopoint.returns]
	what = "a Parser that fails if the next token is a point"
	*/
	*pr_nopoint {
		^NegativeLookAhead(StrParser("."));
	}

	/*
	[classmethod.pr_pitchPrimeVector]
	description = "internal method that creates a Parser that parses a pitch modifier numerical value specified as a prime vector"
	[classmethod.pr_pitchPrimeVector.returns]
	what = '''
	a Parser that matches a pitch modifier numerical value specified as a prime vector, i.e. | exp1 exp2 ... expN >
	The exp things are positive/negative integers or ratios which indicate prime factor exponents, e.g.
	| 1/2 > is 2^(1/2) and | -1/2 2/3 > is 2^(-1/2)*3^(2/3).

	The result is marked up.
	'''
	*/
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
		]).map({|result| result[2] });
	}


	/*
	[classmethod.pr_pitchCents]
	description = "internal method that creates a Parser that can parse a pitch modifier numerical value specified in cents"
	[classmethod.pr_pitchCents.returns]
	what = "a Parser that parses a pitch modifier numerical value specified in cents and marks up the result"
	*/
	*pr_pitchCents {
		var ws = ParserFactory.makeWs;
		var d = ParserFactory.makePositiveFloatParser;
		^SequenceOf([
			ParserFactory.makeWs,
			ParserFactory.makePositiveFloatParser
		]).map({
			| result |
			(\what: \pitch, \kind: \cents, \numerator: result[1].asFloat, \denominator: 1);
		});
	}

	/*
	[classmethod.pr_pitchParser]
	description = "internal method that creates a Parser that can parse the pitch part of a pitch modifier in any format (ratio, primevector or cents)"
	[classmethod.pr_pitchParser.returns]
	what = "a Parser that parses a pitch modifier in any format (ratio, primevector or cents) and marks up the result"
	*/
	*pr_pitchParser {
		^Choice([
			this.pr_pitchPrimeVector.map(MapFactory.keyvalue(\primevector)),
			this.pr_pitchRatio.map(MapFactory.keyvalue(\ratio)),
			this.pr_pitchCents.map(MapFactory.keyvalue(\cents))
		]);
	}

	/*
	[classmethod.pr_noteModifier]
	description = "internal method that creates a Parser that can parse a pitch modifier in any format (ratio, primevector or cents)"
	[classmethod.pr_noteModifier.returns]
	what = "a Parser that parses a pitch modifier and marks up the result; pitch modifiers are optional"
	*/
	*pr_noteModifier {
		^Optional(
			this.pr_betweenCurlyBrackets.(
				SequenceOf([
					this.pr_plusminParser,
					ParserFactory.makeWs,
					this.pr_pitchParser])).map({
				|result|
				(\what : \notemodifier,
					\kind: result[2][\key],
					\direction: result[0][\notemodifier],
					\value: result[2][\value])
			})
		).map({|result| result ? (\what : \notemodifier, \kind : \natural, \direction: \none) });
	}

	/*
	[classmethod.pr_restParser]
	description = "internal method that creates a Parser that can parse a rest in a Mitola string"
	[classmethod.pr_restParser.returns]
	what = "a Parser that parses a rest and marks up the result"
	*/
	*pr_restParser {
		^RegexParser("[rR]").map({|result| (\what: \rest) });
	}

	/*
	[classmethod.pr_noteAndMod]
	description = "internal method that creates a Parser that can parse a degree followed by an (optional) pitch modifier or a rest"
	[classmethod.pr_noteAndMod.returns]
	what = "a Parser that parses a mitola degree followed by an (optional) pitch modifier or a rest"
	*/
	*pr_noteAndMod {
		^Choice([
			SequenceOf([this.pr_noteParser, this.pr_noteModifier]),
			this.pr_restParser
		]);
	}

	/*
	[classmethod.pr_noteAndModAndOct]
	description = "internal method that creates a Parser that can parse a degree followed by an (optional) pitch modifier or a rest and an (optional) repeat interval (think: octave)"
	[classmethod.pr_noteAndModAndOct.returns]
	what = "a Parser"
	*/
	*pr_noteAndModAndOct {
		^Choice([
			SequenceOf([this.pr_noteParser, this.pr_noteModifier, this.pr_repeatIntervalParser]).map({
				|result|
				(\what: \note,
					\notename: result[0][\value],
					\notemodifier: result[1],
					\repeatinterval: result[2][\value])
			}),
			this.pr_restParser
		]);
	}

	/*
	[classmethod.pr_betweenSquareBrackets]
	description = "internal method that creates a function that can create a Parser when called with a Parser for 'something'. The Parser thus created will parse 'something' between square brackets."
	[classmethod.pr_betweenSquareBrackets.returns]
	what = "a Parser"
	*/
	*pr_betweenSquareBrackets {
		^ParserFactory.makeBetween(StrParser("["), StrParser("]"));
	}

	/*
	[classmethod.pr_repeatIntervalParser]
	description = "internal method that creates a Parser that matches a repeat interval (think: octave) between square brackets"
	[classmethod.pr_repeatIntervalParser.returns]
	what = "a Parser"
	*/
	*pr_repeatIntervalParser {
		^Optional(
			this.pr_betweenSquareBrackets.(ParserFactory.makeDigits).map({
				|result|
				(\what: \repeatinterval, \value: result.asInteger)
			})
		).map({|result| result ? (\what: \repeatinterval, \value: \previous) }); // map missing repeat interval to \previous
	}

	/*
	[classmethod.pr_noteAndModAndOctAndDur]
	description = "internal method that creates a Parser that matches a degree followed by pitch modifier, repeatinterval (think: octave) and duration, or a rest and duration"
	[classmethod.pr_noteAndModAndOctAndDur.returns]
	what = "a Parser"
	*/
	*pr_noteAndModAndOctAndDur {
		^SequenceOf([
			this.pr_noteAndModAndOct,
			this.pr_durationParser
		]).map({|result| (\pitch : result[0], \duration: result[1] ) })
	}

	/*
	[classmethod.pr_durationParser]
	description = "internal method that creates a Parser that matches duration specification (i.e. a length and modifiers like dots, muiltiplier and divider)"
	[classmethod.pr_durationParser.returns]
	what = "a Parser"
	*/
	*pr_durationParser {
		^Optional(SequenceOf([
			StrParser("_"),
			ParserFactory.makeFloatParser.map({|result| (\what: \duration, \value: result)}),
			Many(StrParser(".")).map({|result| (\what: \durdots, \value: result.size)}),
			Optional(SequenceOf([StrParser("*"), ParserFactory.makeIntegerParser]).map({|result| (\what: \durmultiplier, \value: result[1])})),
			Optional(SequenceOf([StrParser("/"), ParserFactory.makeIntegerParser]).map({|result| (\what: \durdivider, \value: result[1])}))
		])).map({
			|result|
			if (result.isNil) {
				(\dur : \previous, \durmultiplier: \previous, \durdivider: \previous, \durdots: \previous);
			} {
				var dur = ( \dur : result[1][\value], \durdots : result[2][\value]);
				// treat divider and multiplier as one: specifying only one of the two affects the other one
				if (result[3].isNil && result[4].isNil) {
					dur[\durmultiplier] = \previous;
					dur[\durdivider] = \previous;
				} {
					if (result[3].isNil) {
						// only dividider specified ->reset multplier to 1
						dur[\durmultiplier] = 1;
						dur[\durdivider] = result[4][\value];
					} {
						if (result[4].isNil) {
							// only multiplier specified -> reset divider to 1
							dur[\durmultiplier] = result[3][\value];
							dur[\durdivider] = 1;
						} {
							// everything specified
							dur[\durmultiplier] = result[3][\value];
							dur[\durdivider] = result[4][\value];
						}
					};
				};
				dur;
			};
		});
	}

	/*
	[classmethod.pr_propertyNameParser]
	description = "internal method that creates a Parser that matches a property name. Properties can be attached to Mitola degrees and end up as keys in the pbind."
	[classmethod.pr_propertyNameParser.returns]
	what = "a Parser"
	*/
	*pr_propertyNameParser {
		^RegexParser("@[a-zA-z][a-zA-Z0-9]*").map({|result| (\what: \propertyname, \value: result.drop(1))});
	}

	/*
	[classmethod.pr_propertiesParser]
	description = "internal method that creates a Parser that matches list of property names and values as attached to a mitola degree"
	[classmethod.pr_propertiesParser.returns]
	what = "a Parser"
	*/
	*pr_propertiesParser {
		^Many(
			Choice([
				SequenceOf([
					this.pr_propertyNameParser,
					StrParser("{"),
					ParserFactory.makeFloatParser,
					StrParser("}")
				]).map({|result| (\propertyname: result[0][\value], \what: \animatedproperty, \value: result[2])}),
				SequenceOf([
					this.pr_propertyNameParser,
					StrParser("["),
					ParserFactory.makeFloatParser,
					StrParser("]")
				]).map({|result| (\propertyname: result[0][\value], \what: \staticproperty, \value: result[2])})
		]));
	}

	/*
	[classmethod.pr_noteAndModAndOctAndDurAndProp]
	description = "internal method that creates a Parser that matches a mitola degree with all possible markup (modifiers, repeatinterval durations, properties)"
	[classmethod.pr_noteAndModAndOctAndDurAndProp.returns]
	what = "a Parser"
	*/
	*pr_noteAndModAndOctAndDurAndProp {
		^SequenceOf([
			this.pr_noteAndModAndOctAndDur,
			this.pr_propertiesParser]).map({|result| (\what: \singlenote, \info : ( \note : result[0], \props : result[1] ) ); });
	}

	/*
	[classmethod.pr_chordParser]
	description = "internal method that creates a Parser that matches a group of degrees grouped into a chord."
	[classmethod.pr_chordParser.returns]
	what = "a Parser"
	*/
	*pr_chordParser {
		^this.pr_betweenChordBrackets.(
			ManyOne(
				SequenceOf([
					this.pr_noteAndModAndOctAndDurAndProp,
					ParserFactory.makeWs
				]).map({|result| result[0] }); // remove whitespace from result
		)).map({|result| (\what: \chord, \notes : result) });
	}

	/*
	[classmethod.pr_betweenChordBrackets]
	description = "internal method that creates a function that can create a Parser that parses ;something' between chord brackets"
	[classmethod.pr_betweenChordBrackets.returns]
	what = "a function that makes a parser for 'something' between chord brackets if you call it with a Parser that parses 'something'"
	*/
	*pr_betweenChordBrackets {
		^ParserFactory.makeBetween(
			SequenceOf([StrParser("<"), ParserFactory.makeWs]),
			StrParser(">"));
	}

	/*
	[classmethod.pr_notelistParser]
	description = "internal method that creates a Parser that parses a list of notes"
	[classmethod.pr_notelistParser.returns]
	what = "a Parser"
	*/
	*pr_notelistParser {
		^ManyOne(Choice([
			SequenceOf([this.pr_chordParser, ParserFactory.makeWs]).map({|result| result[0]}), // eat whitespace
			SequenceOf([this.pr_noteAndModAndOctAndDurAndProp, ParserFactory.makeWs]).map({|result| result[0] }) // eat whitespace
		]));
	}

	/*
	[classmethod.pr_betweenRepeatBrackets]
	description = "internal method that creates a function that can create a Parser that parses a list of notes"
	[classmethod.pr_betweenRepeatBrackets.returns]
	what = "a function"
	*/
	*pr_betweenRepeatBrackets {
		^ParserFactory.makeBetween(
			SequenceOf([StrParser("|:"), ParserFactory.makeWs]),
			StrParser(":|");
		);
	}

	/*
	[classmethod.pr_mixedNotelist]
	description = "internal method that creates a (recursive) Parser for a list of chords and notes with nested repeat brackets"
	[classmethod.pr_mixedNotelist.returns]
	what = "a Parser"
	*/
	*pr_mixedNotelist {
		^ParserFactory.forwardRef(Thunk({
			ManyOne(Choice([this.pr_repeatedNotelist, this.pr_notelistParser])).map({|result| result.flatten(1); });
		}));
	}

	/*
	[classmethod.pr_repeatedNotelist]
	description = "internal method that creates Parser to parse chords and notes between repeat brackets e.g. |: 1 2 3 4 :|*2"
	[classmethod.pr_repeatedNotelist.returns]
	what = "a Parser"
	*/
	*pr_repeatedNotelist {
		^SequenceOf([
			this.pr_betweenRepeatBrackets.(this.pr_mixedNotelist),
			ParserFactory.makeWs,
			StrParser("*"),
			ParserFactory.makeWs,
			ParserFactory.makeIntegerParser,
			ParserFactory.makeWs
		]).map({
			// unroll the loop already - not sure if this is a good idea (memory consumption!)
			// but it's easier to evaluate later on
			|result|
			var parseRes = [];
			var repeat = result[4];
			repeat.do({
				parseRes = parseRes.addAll(result[0]);
			});
			parseRes.flatten(1);
		})
	}
/*
[examples]
what = '''
(
var score = "1 |: 4 |: 6 7{+50.0} :|*2 :|*3 8{-20.0}>}";
var result = MitolaParser.pr_mixedNotelist.run(score);
// should be true:
result.result == [
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval':
						'previous', 'notename': 0,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 3,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 5,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier':
						'previous',
						'durdivider':
						'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote'),
			( 'info':
				( 'props': [  ],
					'note': (
						'pitch': (
							'what': 'note',
							'repeatinterval': 'previous',
							'notename': 6, 'notemodifier': (
								'kind': 'cents',
								'what': 'notemodifier',
								'value': (
									'numerator': 50.0,
									'kind': 'cents',
									'what': 'pitch',
									'denominator': 1 ),
								'direction': 'raise' )),
						'duration': (
							'dur': 'previous',
							'durmultiplier': 'previous',
							'durdivider': 'previous',
							'durdots': 'previous' ))),
				'what': 'singlenote'),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 5,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 6,
						'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 50.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote'),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 3, 'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': ( 'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 5,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 6, 'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 50.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' ) ),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': ( 'pitch': (
					'what': 'note',
					'repeatinterval': 'previous',
					'notename': 5,
					'notemodifier': (
						'kind': 'natural',
						'direction' : 'none',
						'what': 'notemodifier' )),
				'duration': (
					'dur': 'previous',
					'durmultiplier':
					'previous',
					'durdivider':
					'previous',
					'durdots': 'previous' ))),
			'what': 'singlenote'),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 6, 'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 50.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote'),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 3,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 5,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 6,
						'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 50.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 5,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 6,
						'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 50.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'repeatinterval': 'previous',
						'notename': 7, 'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 20.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'lower' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' ) ];
)
'''
*/
}
