/*
[general]
title = "MtlMitolaParser"
summary = "a parser for mitola scores - not for end users, use MtlMitola instead."
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola"
description = '''
MtlMitola parser can parse a string or a file containing a valid MtlMitola score and convert it to a parse tree.
'''
*/
MtlMitolaParser : ScpParser {
	/*
	[classmethod.new]
	description = "New creates a new MtlMitolaParser"
	[classmethod.new.returns]
	what = "a new MtlMitolaParser"
	*/
	*new {
		^super.new.init();
	}

	/*
	[method.init]
	description = "initializes a new MtlMitolaParser"
	[method.init.returns]
	what = "an initialized MtlMitolaParser object"
	*/
	init {

	}

	/*
	[method.parse]
	description = "parses a MtlMitola score; upon failure displays an error msg and returns nil"
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
		var state = MtlMitolaParser.pr_mixedNotelist.run(mitolastring);
		if (state.isError) {
			state.prettyprint; // show error message and return nil
			^nil
		} {
			^state.result;
		}
	}

	/*
	[method.parse_file]
	description = "parses a MtlMitola score from file; upon failure displays an error msg and returns nil"
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
	what = "a ScpParser"
	*/
	*pr_noteParser {
		^ScpParserFactory.makePositiveIntegerParser.map({|result| (\what: \notename, \value: result-1) }); // use one-based counting
	}

	/*
	[classmethod.pr_plusminParser]
	description = "internal method that creates a parser that can parse a +/- sign as part of a pitch modifier (similar to a flat or sharp in traditional notation) and markup the result"
	[classmethod.pr_plusminParser.returns]
	what = "a ScpParser"
	*/
	*pr_plusminParser {
		^ScpChoice([
			ScpStrParser("+").map({|result| (\notemodifier:\raise) }),
			ScpStrParser("-").map({|result| (\notemodifier:\lower) }),
		]);
	}

	/*
	[classmethod.pr_betweenCurlyBrackets]
	description = "internal method that creates a function that can create a parser to parse 'something' between curly brackets"
	[classmethod.pr_betweenCurlyBrackets.returns]
	what = "a function expecting a ScpParser for 'something' as argument, to create a ScpParser that parses 'something' between curly brackets"
	*/
	*pr_betweenCurlyBrackets {
		^ScpParserFactory.makeBetween(ScpStrParser("{"), ScpSequenceOf([ScpParserFactory.makeWs, ScpStrParser("}")]));
	}

	/*
	[classmethod.pr_pitchRatio]
	description = "internal method that creates a ScpParser to parse a pitch modifier numerical value specified as a ratio and markup the result"
	[classmethod.pr_pitchRatio.returns]
	what = "a ScpParser parsing a pitch modifier numerical value ratio [Integer]/[Integer]"
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
	description = "internal method that creates a ScpParser that fails if the next token is a point"
	[classmethod.pr_nopoint.returns]
	what = "a ScpParser that fails if the next token is a point"
	*/
	*pr_nopoint {
		^ScpNegativeLookAhead(ScpStrParser("."));
	}

	/*
	[classmethod.pr_pitchPrimeVector]
	description = "internal method that creates a ScpParser that parses a pitch modifier numerical value specified as a prime vector"
	[classmethod.pr_pitchPrimeVector.returns]
	what = '''
	a ScpParser that matches a pitch modifier numerical value specified as a prime vector, i.e. | exp1 exp2 ... expN >
	The exp things are positive/negative integers or ratios which indicate prime factor exponents, e.g.
	| 1/2 > is 2^(1/2) and | -1/2 2/3 > is 2^(-1/2)*3^(2/3).

	The result is marked up.
	'''
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
		]).map({|result| result[2] });
	}


	/*
	[classmethod.pr_pitchCents]
	description = "internal method that creates a ScpParser that can parse a pitch modifier numerical value specified in cents"
	[classmethod.pr_pitchCents.returns]
	what = "a ScpParser that parses a pitch modifier numerical value specified in cents and marks up the result"
	*/
	*pr_pitchCents {
		var ws = ScpParserFactory.makeWs;
		var d = ScpParserFactory.makePositiveFloatParser;
		^ScpSequenceOf([
			ScpParserFactory.makeWs,
			ScpParserFactory.makePositiveFloatParser
		]).map({
			| result |
			(\what: \pitch, \kind: \cents, \numerator: result[1].asFloat, \denominator: 1);
		});
	}

	/*
	[classmethod.pr_pitchParser]
	description = "internal method that creates a ScpParser that can parse the pitch part of a pitch modifier in any format (ratio, primevector or cents)"
	[classmethod.pr_pitchParser.returns]
	what = "a ScpParser that parses a pitch modifier in any format (ratio, primevector or cents) and marks up the result"
	*/
	*pr_pitchParser {
		^ScpChoice([
			this.pr_pitchPrimeVector.map(ScpMapFactory.keyvalue(\primevector)),
			this.pr_pitchRatio.map(ScpMapFactory.keyvalue(\ratio)),
			this.pr_pitchCents.map(ScpMapFactory.keyvalue(\cents))
		]);
	}

	/*
	[classmethod.pr_noteModifier]
	description = "internal method that creates a ScpParser that can parse a pitch modifier in any format (ratio, primevector or cents)"
	[classmethod.pr_noteModifier.returns]
	what = "a ScpParser that parses a pitch modifier and marks up the result; pitch modifiers are optional"
	*/
	*pr_noteModifier {
		^ScpOptional(
			this.pr_betweenCurlyBrackets.(
				ScpSequenceOf([
					this.pr_plusminParser,
					ScpParserFactory.makeWs,
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
	description = "internal method that creates a ScpParser that can parse a rest in a MtlMitola string"
	[classmethod.pr_restParser.returns]
	what = "a ScpParser that parses a rest and marks up the result"
	*/
	*pr_restParser {
		^ScpRegexParser("[rR]").map({|result| (\what: \rest) });
	}

	/*
	[classmethod.pr_noteAndMod]
	description = "internal method that creates a ScpParser that can parse a degree followed by an (optional) pitch modifier or a rest"
	[classmethod.pr_noteAndMod.returns]
	what = "a ScpParser that parses a mitola degree followed by an (optional) pitch modifier or a rest"
	*/
	*pr_noteAndMod {
		^ScpChoice([
			ScpSequenceOf([this.pr_noteParser, this.pr_noteModifier]),
			this.pr_restParser
		]);
	}

	/*
	[classmethod.pr_noteAndModAndOct]
	description = "internal method that creates a ScpParser that can parse a degree followed by an (optional) pitch modifier or a rest and an (optional) equivalence interval (think: octave)"
	[classmethod.pr_noteAndModAndOct.returns]
	what = "a ScpParser"
	*/
	*pr_noteAndModAndOct {
		^ScpChoice([
			ScpSequenceOf([this.pr_noteParser, this.pr_noteModifier, this.pr_equivalenceintervalParser]).map({
				|result|
				(\what: \note,
					\notename: result[0][\value],
					\notemodifier: result[1],
					\equivalenceinterval: result[2][\value])
			}),
			this.pr_restParser
		]);
	}

	/*
	[classmethod.pr_betweenSquareBrackets]
	description = "internal method that creates a function that can create a ScpParser when called with a ScpParser for 'something'. The ScpParser thus created will parse 'something' between square brackets."
	[classmethod.pr_betweenSquareBrackets.returns]
	what = "a ScpParser"
	*/
	*pr_betweenSquareBrackets {
		^ScpParserFactory.makeBetween(ScpStrParser("["), ScpStrParser("]"));
	}

	/*
	[classmethod.pr_equivalenceintervalParser]
	description = "internal method that creates a ScpParser that matches a equivalence interval (think: octave) between square brackets"
	[classmethod.pr_equivalenceintervalParser.returns]
	what = "a ScpParser"
	*/
	*pr_equivalenceintervalParser {
		^ScpOptional(
			this.pr_betweenSquareBrackets.(ScpParserFactory.makeDigits).map({
				|result|
				(\what: \equivalenceinterval, \value: result.asInteger)
			})
		).map({|result| result ? (\what: \equivalenceinterval, \value: \previous) }); // map missing equivalence interval to \previous
	}

	/*
	[classmethod.pr_noteAndModAndOctAndDur]
	description = "internal method that creates a ScpParser that matches a degree followed by pitch modifier, equivalenceinterval (think: octave) and duration, or a rest and duration"
	[classmethod.pr_noteAndModAndOctAndDur.returns]
	what = "a ScpParser"
	*/
	*pr_noteAndModAndOctAndDur {
		^ScpSequenceOf([
			this.pr_noteAndModAndOct,
			this.pr_durationParser
		]).map({|result| (\pitch : result[0], \duration: result[1] ) })
	}

	/*
	[classmethod.pr_durationParser]
	description = "internal method that creates a ScpParser that matches duration specification (i.e. a length and modifiers like dots, muiltiplier and divider)"
	[classmethod.pr_durationParser.returns]
	what = "a ScpParser"
	*/
	*pr_durationParser {
		^ScpOptional(ScpSequenceOf([
			ScpStrParser("_"),
			ScpParserFactory.makeFloatParser.map({|result| (\what: \duration, \value: result)}),
			ScpMany(ScpStrParser(".")).map({|result| (\what: \durdots, \value: result.size)}),
			ScpOptional(ScpSequenceOf([ScpStrParser("*"), ScpParserFactory.makeIntegerParser]).map({|result| (\what: \durmultiplier, \value: result[1])})),
			ScpOptional(ScpSequenceOf([ScpStrParser("/"), ScpParserFactory.makeIntegerParser]).map({|result| (\what: \durdivider, \value: result[1])}))
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
	description = "internal method that creates a ScpParser that matches a property name. Properties can be attached to MtlMitola degrees and end up as keys in the pbind."
	[classmethod.pr_propertyNameParser.returns]
	what = "a ScpParser"
	*/
	*pr_propertyNameParser {
		^ScpRegexParser("@[a-zA-z][a-zA-Z0-9]*").map({|result| (\what: \propertyname, \value: result.drop(1))});
	}

	/*
	[classmethod.pr_propertiesParser]
	description = "internal method that creates a ScpParser that matches list of property names and values as attached to a mitola degree"
	[classmethod.pr_propertiesParser.returns]
	what = "a ScpParser"
	*/
	*pr_propertiesParser {
		^ScpMany(
			ScpChoice([
				ScpSequenceOf([
					this.pr_propertyNameParser,
					ScpStrParser("{"),
					ScpParserFactory.makeFloatParser,
					ScpStrParser("}")
				]).map({|result| (\propertyname: result[0][\value], \what: \animatedproperty, \value: result[2])}),
				ScpSequenceOf([
					this.pr_propertyNameParser,
					ScpStrParser("["),
					ScpParserFactory.makeFloatParser,
					ScpStrParser("]")
				]).map({|result| (\propertyname: result[0][\value], \what: \staticproperty, \value: result[2])})
		]));
	}

	/*
	[classmethod.pr_noteAndModAndOctAndDurAndProp]
	description = "internal method that creates a ScpParser that matches a mitola degree with all possible markup (modifiers, equivalenceinterval durations, properties)"
	[classmethod.pr_noteAndModAndOctAndDurAndProp.returns]
	what = "a ScpParser"
	*/
	*pr_noteAndModAndOctAndDurAndProp {
		^ScpSequenceOf([
			this.pr_noteAndModAndOctAndDur,
			this.pr_propertiesParser]).map({|result| (\what: \singlenote, \info : ( \note : result[0], \props : result[1] ) ); });
	}

	/*
	[classmethod.pr_chordParser]
	description = "internal method that creates a ScpParser that matches a group of degrees grouped into a chord."
	[classmethod.pr_chordParser.returns]
	what = "a ScpParser"
	*/
	*pr_chordParser {
		^this.pr_betweenChordBrackets.(
			ScpManyOne(
				ScpSequenceOf([
					this.pr_noteAndModAndOctAndDurAndProp,
					ScpParserFactory.makeWs
				]).map({|result| result[0] }); // remove whitespace from result
		)).map({|result| (\what: \chord, \notes : result) });
	}

	/*
	[classmethod.pr_betweenChordBrackets]
	description = "internal method that creates a function that can create a ScpParser that parses ;something' between chord brackets"
	[classmethod.pr_betweenChordBrackets.returns]
	what = "a function that makes a parser for 'something' between chord brackets if you call it with a ScpParser that parses 'something'"
	*/
	*pr_betweenChordBrackets {
		^ScpParserFactory.makeBetween(
			ScpSequenceOf([ScpStrParser("<"), ScpParserFactory.makeWs]),
			ScpStrParser(">"));
	}

	/*
	[classmethod.pr_notelistParser]
	description = "internal method that creates a ScpParser that parses a list of notes"
	[classmethod.pr_notelistParser.returns]
	what = "a ScpParser"
	*/
	*pr_notelistParser {
		^ScpManyOne(ScpChoice([
			ScpSequenceOf([this.pr_chordParser, ScpParserFactory.makeWs]).map({|result| result[0]}), // eat whitespace
			ScpSequenceOf([this.pr_noteAndModAndOctAndDurAndProp, ScpParserFactory.makeWs]).map({|result| result[0] }) // eat whitespace
		]));
	}

	/*
	[classmethod.pr_betweenRepeatBrackets]
	description = "internal method that creates a function that can create a ScpParser that parses a list of notes"
	[classmethod.pr_betweenRepeatBrackets.returns]
	what = "a function"
	*/
	*pr_betweenRepeatBrackets {
		^ScpParserFactory.makeBetween(
			ScpSequenceOf([ScpStrParser("|:"), ScpParserFactory.makeWs]),
			ScpStrParser(":|");
		);
	}

	/*
	[classmethod.pr_mixedNotelist]
	description = "internal method that creates a (recursive) ScpParser for a list of chords and notes with nested repeat brackets"
	[classmethod.pr_mixedNotelist.returns]
	what = "a ScpParser"
	*/
	*pr_mixedNotelist {
		^ScpParserFactory.forwardRef(Thunk({
			ScpManyOne(ScpChoice([this.pr_repeatedNotelist, this.pr_notelistParser])).map({|result| result.flatten(1); });
		}));
	}

	/*
	[classmethod.pr_repeatedNotelist]
	description = "internal method that creates ScpParser to parse chords and notes between repeat brackets e.g. |: 1 2 3 4 :|*2"
	[classmethod.pr_repeatedNotelist.returns]
	what = "a ScpParser"
	*/
	*pr_repeatedNotelist {
		^ScpSequenceOf([
			this.pr_betweenRepeatBrackets.(this.pr_mixedNotelist),
			ScpParserFactory.makeWs,
			ScpStrParser("*"),
			ScpParserFactory.makeWs,
			ScpParserFactory.makeIntegerParser,
			ScpParserFactory.makeWs
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
var result = MtlMitolaParser.pr_mixedNotelist.run(score);
// should be true:
result.result == [
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval':
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
							'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
					'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
						'equivalenceinterval': 'previous',
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
