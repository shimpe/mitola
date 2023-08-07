/*
[general]
title = "MtlMitolaParserTests"
summary = "tests for the MtlMitolaParser class"
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola"
description = '''
unit tests
'''
*/
MtlMitolaParserTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	pr_check {
		| result, expected |
		var ok = true;
		var props = [\what, \value, \propertyname, \notename, \notemodifier, \octave, \dur, \durdots, \durmultiplier, \durdivider];
		props.do({
			| prop |
			if (expected[prop].isNil.not) {
				if (expected[prop].isString) {
					if (result[prop].compare(expected[prop]) != 0)
					{
						var msg = ("" ++ prop + "mismatch: expected[\\" ++ prop ++ "] == " + expected[prop] + ", but result[\\" ++ prop ++ "] == " + result[prop]);
						this.assert(false, msg);
					}
				} {
					if (result[prop] != expected[prop]) {
						var msg = ("" ++ prop + "mismatch: expected[\\" ++ prop ++ "] == " + expected[prop] + ", but result[\\" ++ prop ++ "] == " + result[prop]).postln;
						this.assert(false, msg);
					};
				}
			};
		});
		if (ok) {
			this.assert(true, "ok");
		};
	}

	pr_checkemptylist {
		| what |
		if (what != []) {
			var msg = ("error! expected empty list, but got" + what);
			this.assert(false, msg);
		}{
			var msg = "ok";
			this.assert(true, msg);
		};
	}

	pr_checkeq {
		| result, expected |
		if (result != expected) {
			var msg = ("result " + result + "!= expected" + expected);
			this.assert(false, msg);
		} {
			var msg = "ok";
			this.assert(true, msg);
		}
	}

	test_language {
		var text = ();
		var result = ();

		text[\1] = "1{+100.0}[4]_4";
		result[\11] = MtlMitolaParser.pr_noteParser.run(text[\1]);
		// 1 (one-based human degree spec) is translated to 0 (zero-based computer degree spec)
		this.pr_check(result[\11].result, (\what: \notename, \value: 0 ));

		text[\2] = "{+50.0}";
		result[\21] = MtlMitolaParser.pr_noteModifier.run(text[\2]);
		this.assertEquals(result[\21].result, (
			'kind': 'cents',
			'what': 'notemodifier',
			'value': ( 'numerator': 50.0, 'kind': 'cents', 'what': 'pitch', 'denominator': 1 ),
			'direction': 'raise' ),
		"result21_result"
		);

		result[\22] = MtlMitolaParser.pr_noteModifier.run("{- |-2 2 1> }");
		this.assertEquals(result[\22].result, (
			'kind': 'primevector',
			'what': 'notemodifier',
			'value': [
				( 'numerator': -2, 'kind': 'ratio', 'what': 'primeexponent', 'denominator': 1 ),
				( 'numerator': 2, 'kind': 'ratio', 'what': 'primeexponent', 'denominator': 1 ),
				( 'numerator': 1, 'kind': 'ratio', 'what': 'primeexponent', 'denominator': 1 ) ],
			'direction': 'lower' ),
		"result22_result"
		);

		result[\23] = MtlMitolaParser.pr_noteModifier.run("{+6/7}");
		this.assertEquals(result[\23].result, (
			'kind': 'ratio',
			'what': 'notemodifier',
			'value': ( 'numerator': 6, 'kind': 'ratio', 'what': 'pitch', 'denominator': 7 ),
			'direction': 'raise' ),
		"result23_result"
		);

		result[\12] = MtlMitolaParser.pr_noteAndMod.run(text[\1]);
		this.assertEquals(result[\12].result[0], (\what: \notename, \value: 0), "result12_result[0]");
		this.assertEquals(result[\12].result[1], (
			'kind': 'cents',
			'what': 'notemodifier',
			'value': ( 'numerator': 100.0, 'kind': 'cents', 'what': 'pitch', 'denominator': 1 ),
			'direction': 'raise' ),
		"result12_result[1]"
		);

		text[\3] = "r_4";
		result[\31] = MtlMitolaParser.pr_noteAndMod.run(text[\3]);
		this.assertEquals(result[\31].result, (\what: \rest), "result31_result");

		result[\13] = MtlMitolaParser.pr_noteAndModAndOct.run(text[\1]);
		this.assertEquals(result[\13].result, (
			'what': 'note',
			'equivalenceinterval': 4,
			'notename': 0,
			'notemodifier': (
				'kind': 'cents',
				'what': 'notemodifier',
				'value': (
					'numerator': 100.0,
					'kind': 'cents',
					'what': 'pitch',
					'denominator': 1 ),
				'direction': 'raise')),
		"result13_result"
		);

		text[\4] = "3{-4/5}_4";
		result[\41] = MtlMitolaParser.pr_noteAndModAndOct.run(text[\4]);
		this.assertEquals(result[\41].result, (
			'what': 'note',
			'equivalenceinterval': 'previous',
			'notename': 2,
			'notemodifier': (
				'kind': 'ratio',
				'what': 'notemodifier',
				'value': (
					'numerator': 4,
					'kind': 'ratio',
					'what': 'pitch',
					'denominator': 5 ),
				'direction': 'lower' ) ),
		"result41_result");

		text[\5] = "5{-70.0}_4*8/3";
		result[\51] = MtlMitolaParser.pr_noteAndModAndOctAndDur.run(text[\5]);
		this.assertEquals(result[\51].result[\pitch], (
			'what': 'note',
			'equivalenceinterval': 'previous',
			'notename': 4,
			'notemodifier': (
				'kind': 'cents',
				'what': 'notemodifier',
				'value': (
					'numerator': 70.0,
					'kind': 'cents',
					'what':
					'pitch',
					'denominator': 1 ),
				'direction': 'lower' )), "result51_pitch");
		this.assertEquals(result[\51].result[\duration], (
			'durmultiplier': 8,
			'durdots': 0,
			'dur': 4.0,
			'durdivider': 3 ),
		"result51_duration");


		text[\6] = "r_2./3";
		result[\61] = MtlMitolaParser.pr_noteAndModAndOctAndDur.run(text[\6]);
		this.assertEquals(result[\61].result[\pitch], ( 'what': 'rest' ), "result61_pitch");
		this.assertEquals(result[\61].result[\duration], (
			'durmultiplier': 1,
			'durdots': 1,
			'dur': 2.0,
			'durdivider': 3 ),
		"result61_duration"
		);

		text[\7] = "18{-45.67}[5]_4@prop[34.4]@vol{5}";
		result[\71] = MtlMitolaParser.pr_noteAndModAndOctAndDurAndProp.run(text[\7]);
		this.assertEquals(result[\71].result,
			( 'info':
				('props': [
					( 'what': 'staticproperty', 'propertyname': "prop", 'value': 34.4 ),
					( 'what': 'animatedproperty', 'propertyname': "vol", 'value': 5.0 )],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 5,
						'notename': 17,
						'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': ( 'numerator': 45.67, 'kind': 'cents', 'what': 'pitch', 'denominator': 1 ),
							'direction': 'lower' )),
					'duration': (
						'durmultiplier': 'previous',
						'durdots': 0,
						'dur': 4.0,
						'durdivider': 'previous' ))),
				'what': 'singlenote' ),
			"result71_result"
		);

		text[\8] = "5{+2/1}[2]_4..*8";
		result[\81] = MtlMitolaParser.pr_noteAndModAndOctAndDurAndProp.run(text[\8]);
		this.assertEquals(result[\81].result, (
			'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 2,
						'notename': 4,
						'notemodifier': (
							'kind': 'ratio',
							'what': 'notemodifier',
							'value': (
								'numerator': 2,
								'kind': 'ratio',
								'what': 'pitch',
								'denominator': 1 ),
							'direction': 'raise' ) ),
					'duration': (
						'durmultiplier': 8,
						'durdots': 2,
						'dur': 4.0,
						'durdivider': 1 ))),
			'what': 'singlenote' ),
		"result81_result");

		text[\82] = "9";
		result[\82] = MtlMitolaParser.pr_noteAndModAndOctAndDurAndProp.run(text[\82]);
		this.assertEquals(result[\82].result, (
			'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 'previous',
						'notename': 8,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote'),
		"result82_result");

		text[\9] = "< 9[4]_3@ped{67} 8[5] 3>";
		result[\91] = MtlMitolaParser.pr_chordParser.run(text[\9]);
		this.assertEquals(result[\91].result, (
			'what': 'chord',
			'notes': [
				( 'info': (
					'props': [
						( 'what': 'animatedproperty', 'propertyname': "ped", 'value': 67) ],
					'note': (
						'pitch': (
							'what': 'note',
							'equivalenceinterval': 4,
							'notename': 8,
							'notemodifier': (
								'kind': 'natural',
								'direction' : 'none',
								'what': 'notemodifier' )),
						'duration': (
							'durmultiplier': 'previous',
							'durdots': 0,
							'dur': 3.0,
							'durdivider': 'previous' ))),
				'what': 'singlenote' ),
				( 'info': (
					'props': [  ],
					'note': (
						'pitch': (
							'what': 'note',
							'equivalenceinterval': 5,
							'notename': 7,
							'notemodifier': (
								'kind': 'natural',
								'direction' : 'none',
								'what': 'notemodifier' )),
						'duration': (
							'dur': 'previous',
							'durmultiplier': 'previous',
							'durdivider': 'previous',
							'durdots': 'previous' ))),
				'what': 'singlenote'),
				( 'info': (
					'props': [  ],
					'note': ( 'pitch':
						( 'what': 'note',
							'equivalenceinterval': 'previous',
							'notename': 2, 'notemodifier': (
								'kind': 'natural',
								'direction' : 'none',
								'what': 'notemodifier' )),
						'duration': (
							'dur': 'previous',
							'durmultiplier': 'previous',
							'durdivider': 'previous',
							'durdots': 'previous' ))),
				'what': 'singlenote')]),
		"result91_result");

		text[\10] = "6[4] 5{-100.0}[3]_2@vol{123} <8[2]_4.*2/3 2 7> 9[5]";
		result[\10] = MtlMitolaParser.pr_notelistParser.run(text[\10]);
		this.assertEquals(result[\10].result, [
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 4,
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
			'what': 'singlenote'),
			( 'info': (
				'props': [
					( 'what': 'animatedproperty', 'propertyname': "vol", 'value': 123.0 )],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 3,
						'notename': 4,
						'notemodifier': (
							'kind': 'cents',
							'what': 'notemodifier',
							'value': (
								'numerator': 100.0,
								'kind': 'cents',
								'what': 'pitch',
								'denominator': 1),
							'direction': 'lower' )),
					'duration': (
						'durmultiplier': 'previous',
						'durdots': 0,
						'dur': 2.0,
						'durdivider': 'previous' ))),
			'what': 'singlenote' ),
			( 'what': 'chord',
				'notes': [
					( 'info': (
						'props': [  ],
						'note': (
							'pitch': (
								'what': 'note',
								'equivalenceinterval': 2,
								'notename': 7,
								'notemodifier': (
									'kind': 'natural',
									'direction' : 'none',
									'what': 'notemodifier' )),
							'duration': (
								'durmultiplier': 2,
								'durdots': 1,
								'dur': 4.0,
								'durdivider': 3 ))),
					'what': 'singlenote'),
					( 'info': (
						'props': [  ],
						'note': (
							'pitch': (
								'what': 'note',
								'equivalenceinterval': 'previous',
								'notename': 1,
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
									'kind': 'natural',
									'direction' : 'none',
									'what': 'notemodifier' )),
							'duration': (
								'dur': 'previous',
								'durmultiplier': 'previous',
								'durdivider': 'previous',
								'durdots': 'previous' ))),
					'what': 'singlenote' )]),
			('info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 5,
						'notename': 8,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ))),
			'what': 'singlenote' )],
		"result10_result");

		text[\11] = "|: 8[4] 4{+6/83}_2@vol{123} :|*3";
		result[\11] = MtlMitolaParser.pr_repeatedNotelist.run(text[\11]);
		this.assertEquals(result[\11].result, [
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 4,
						'notename': 7,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier' )),
					'duration': (
						'dur': 'previous',
						'durmultiplier': 'previous',
						'durdivider': 'previous',
						'durdots': 'previous' ) ) ),
			'what': 'singlenote' ),
			( 'info': (
				'props': [
					( 'what': 'animatedproperty', 'propertyname': "vol", 'value': 123.0 ) ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval':
						'previous',
						'notename': 3,
						'notemodifier': (
							'kind': 'ratio',
							'what': 'notemodifier',
							'value': ( 'numerator': 6, 'kind': 'ratio', 'what': 'pitch', 'denominator': 83 ),
							'direction': 'raise' )),
					'duration': (
						'durmultiplier': 'previous',
						'durdots': 0,
						'dur': 2.0,
						'durdivider': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 4,
						'notename': 7,
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
				'props': [
					( 'what': 'animatedproperty', 'propertyname': "vol", 'value': 123.0 ) ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 'previous',
						'notename': 3,
						'notemodifier': (
							'kind': 'ratio',
							'what': 'notemodifier',
							'value': ( 'numerator': 6, 'kind': 'ratio', 'what': 'pitch', 'denominator': 83 ),
							'direction': 'raise' )),
					'duration': (
						'durmultiplier': 'previous',
						'durdots': 0,
						'dur': 2.0,
						'durdivider': 'previous' ))),
			'what': 'singlenote' ),
			( 'info': (
				'props': [  ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 4,
						'notename': 7,
						'notemodifier': (
							'kind': 'natural',
							'direction' : 'none',
							'what': 'notemodifier'  )),
						'duration': (
							'dur': 'previous',
							'durmultiplier': 'previous',
							'durdivider': 'previous',
							'durdots': 'previous' ))),
				'what': 'singlenote' ),
			( 'info': (
				'props': [
					( 'what': 'animatedproperty', 'propertyname': "vol", 'value': 123.0 ) ],
				'note': (
					'pitch': (
						'what': 'note',
						'equivalenceinterval': 'previous',
						'notename': 3,
						'notemodifier': (
							'kind': 'ratio',
							'what': 'notemodifier',
							'value': ( 'numerator': 6, 'kind': 'ratio', 'what': 'pitch', 'denominator': 83 ),
							'direction': 'raise' )),
					'duration': (
						'durmultiplier': 'previous',
						'durdots': 0, 'dur': 2.0,
						'durdivider': 'previous' ))),
			'what': 'singlenote' )],
		"result11_result");

		text[\12] = "1 |: 4 |: 6 7{+50.0} :|*2 :|*3 8{-20.0}>}";
		result[\12] = MtlMitolaParser.pr_mixedNotelist.run(text[\12]);
		this.assertEquals(result[\12].result, [
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
			'what': 'singlenote' ) ],
		"result12_result");
	}
}
