MitolaParser : Parser {
	*new {
		^super.new.init();
	}

	init {

	}

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

	parseFile {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^this.parse(contents.join(" "));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
			^nil
		};
	}

	*pr_noteParser {
		^ParserFactory.makePositiveIntegerParser.map({|result| (\what: \notename, \value: result-1) }); // use one-based counting
	}

	*pr_plusminParser {
		^Choice([
			StrParser("+").map({|result| (\notemodifier:\raise) }),
			StrParser("-").map({|result| (\notemodifier:\lower) }),
		]);
	}

	*pr_betweenCurlyBrackets {
		^ParserFactory.makeBetween(StrParser("{"), SequenceOf([ParserFactory.makeWs, StrParser("}")]));
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
			]))]).map({
			| result |
			if (result[3].notNil) {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: result[3][3].asInteger);
			} {
				(\what: \pitch, \kind: \ratio, \numerator: result[1].asInteger, \denominator: 1);
			};
		});
	}

	*pr_nopoint {
		^NegativeLookAhead(StrParser("."));
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
		]).map({|result| result[2] });
	}

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

	*pr_pitchParser {
		^Choice([
			this.pr_pitchPrimeVector.map(MapFactory.keyvalue(\primevector)),
			this.pr_pitchRatio.map(MapFactory.keyvalue(\ratio)),
			this.pr_pitchCents.map(MapFactory.keyvalue(\cents))
		]);
	}

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

	*pr_restParser {
		^RegexParser("[rR]").map({|result| (\what: \rest) });
	}

	*pr_noteAndMod {
		^Choice([
			SequenceOf([this.pr_noteParser, this.pr_noteModifier]),
			this.pr_restParser
		]);
	}

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

	*pr_betweenSquareBrackets {
		^ParserFactory.makeBetween(StrParser("["), StrParser("]"));
	}

	*pr_repeatIntervalParser {
		^Optional(
			this.pr_betweenSquareBrackets.(ParserFactory.makeDigits).map({
				|result|
				(\what: \repeatinterval, \value: result.asInteger)
			})
		).map({|result| result ? (\what: \repeatinterval, \value: \previous) }); // map missing repeat interval to \previous
	}

	*pr_noteAndModAndOctAndDur {
		^SequenceOf([
			this.pr_noteAndModAndOct,
			this.pr_durationParser
		]).map({|result| (\pitch : result[0], \duration: result[1] ) })
	}

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

	*pr_propertyNameParser {
		^RegexParser("@[a-zA-z][a-zA-Z0-9]*").map({|result| (\what: \propertyname, \value: result.drop(1))});
	}

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

	*pr_noteAndModAndOctAndDurAndProp {
		^SequenceOf([
			this.pr_noteAndModAndOctAndDur,
			this.pr_propertiesParser]).map({|result| (\what: \singlenote, \info : ( \note : result[0], \props : result[1] ) ); });
	}

	*pr_chordParser {
		^this.pr_betweenChordBrackets.(
			ManyOne(
				SequenceOf([
					this.pr_noteAndModAndOctAndDurAndProp,
					ParserFactory.makeWs
				]).map({|result| result[0] }); // remove whitespace from result
		)).map({|result| (\what: \chord, \notes : result) });
	}

	*pr_betweenChordBrackets {
		^ParserFactory.makeBetween(
			SequenceOf([StrParser("<"), ParserFactory.makeWs]),
			StrParser(">"));
	}

	*pr_notelistParser {
		^ManyOne(Choice([
			SequenceOf([this.pr_chordParser, ParserFactory.makeWs]).map({|result| result[0]}), // eat whitespace
			SequenceOf([this.pr_noteAndModAndOctAndDurAndProp, ParserFactory.makeWs]).map({|result| result[0] }) // eat whitespace
		]));
	}

	*pr_betweenRepeatBrackets {
		^ParserFactory.makeBetween(
			SequenceOf([StrParser("("), ParserFactory.makeWs]),
			StrParser(")");
		);
	}

	*pr_mixedNotelist {
		^ParserFactory.forwardRef(Thunk({
			ManyOne(Choice([this.pr_repeatedNotelist, this.pr_notelistParser])).map({|result| result.flatten(1); });
		}));
	}

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
}

