/*
[general]
title = "Mitola"
summary = "a class to render MIcroTOnal LAnguage scores"
categories = "Microtonal utils"
related = "Classes/ScalaParser, Classes/MitolaParser, Classes/RootFrequencyCalculator"
description = '''
Mitola provides notation and utilities to write microtonal music in supercollider using a text based notation. Mitola is modeled after its sister quark Panola, which provides text based notation for "conventional" music. Mitola shares many ideas and syntax with Panola, but differs in some aspects where I thought Panola could be improved, or where things cannot be reused as-is.

STRONG::installation::

Mitola depends on another quark called scparco which is a library to generate parsers in supercollider. Both scparco and mitola need to be installed:

code::
(
Quarks.install("https://github.com/shimpe/scparco"); // parser combinator library
Quarks.install("https://github.com/shimpe/mitola"); // mitola implementation
)
::

STRONG::degrees::

A Mitola score consists of a list of notes and/or chords. Mitola does not use note names.
Instead it uses integer degrees 1,2,...N, where N is determined by the tuning in use.
Suppose you have a 12EDO tuning, then N can be at most 12.

code::
(
var valid_notes_12_edo = "1 2 3 4 5 6 7 8 9 10 11 12"; // 1-based degree names
)
::

STRONG::score degrees vs scala degrees::

What do to if the scala file defines more degrees than you want to use in your composition? (E.g. you want to select 12 out of 17) In such cases you have the choice to:
1. either make sure only to use the correct degree numbers, or
2. set up a degree mapping from score degrees to scala degrees

To set up a degree mapping, you can pass to Mitola a CODE::degree_mapping:: argument. This CODE::degree_mapping:: should be a CODE::Dictionary:: containing a mapping from score degree (1-based Integer) to scala degree (1-based integer).

Note that pitch modifiers (see next) calculate with score degrees, not with scala degrees. This may be important to understand the behavior in cases where the distance between different score degrees is different.

STRONG::degree modifiers::

Degrees can be modified with modifiers (in conventional music these would be sharps and flats). Modifiers in Mitola can be specified in two ways. First way: absolute modification by a number of cents. E.g. 1{+100.0} modifies score degree 1 by adding 100.0 cents. Note that you must include a decimal point for the number to be interpreted as cents. Similarly 4{-53.0} would lower the pitch represented by score degree 4 by 53.0 cents. The second way is to specify a ratio. E.g. 5{+2/3} will modify the pitch so that it sounds 2/3 between score degree 5 and score degree 6. Ratios with an abs value larger than 1 are reduced: 5{-4/3} is interpreted as 4{-1/3}. This is important to understand the behavior in case of tunings with different gaps between the different score degrees. An alternative way to specify ratios is using prime vector notation. In prime vector notation, a ratio is defined as a multiplication of prime factors. The exponents of the prime factors are between | and >. So this is a valid note: 3{+|1/12>}. It will raise score degree 3 with 2^(1/12).

code::
(
var modified_notes = "7{+50.0} 4{-34.0} 6{+1/4} 10{-3/4} 5{-|1/2 -2/3>}";
)
::

In addition to modifiers, score degreels also have an equivalence interval (aka equave). In traditional notation, this would be called an octave. The equivalence interval is indicated using square brackets. E.g. 1[4] is score degreel one in the fourth equave. If you do not specify an equave for a score degreel, the previous one is reused.

code::
(
var same_note_in_different_equaves = "3[2] 3[4] 3[5]";
)
::

STRONG::rhythm::

To indicate rhythm, a note can be decorated with an underscore (_) followed by a number, e.g. 1_4 indicates score degree one as a quarter note (crotchet), whereas 1_8 is the first score degree as an eighth note (quaver). If you do not specify a duration value, the last specified one is reused.

code::
(
var quarter_note_followed_by_two_eighths = "1_4 5_8 5";
)
::

The duration value optionally can be extended with one or more dots. Similar to traditional notation, 1_4 lasts one quarter note; 1_4. lasts 1+0.5 quarter notes (= 3 eighths) and 1_4.. lasts 1+0.5+0.25 quarter notes (=7 sixteenths (semi quavers)).

code::
(
var two_dotted_quarter_note_followed_by_a_regular_eighth = "6_4. 7 9_8";
)
::

In addition to the dots, one can also (optionally) specificy a multiplier and a divider. These can be used to define tuplets. E.g. "1[4]_8*2/3 4 7" consists of 3 score degrees in the fourth equave forming a triplet of eighth notes.

code::
(
var triplet_of_eighths = "1[4]_8*2/3 4 7";
)
::

Notes can also optionally be decorated with properties (anything you like really). These properties by default are included in the supercollider patterns that are derived from the score, where they can be used to drive synth arguments or control behavior of the patterns. The properties can be animated between notes (see example code below).

code::
(
var animated_prop_crescendo = "1[4]@amp{0.2} 2 3 4 5 6 7@amp{0.9}";
var static_prop_two_staccato_one_legato = "1[4]@legato[0.1] 5 1[5]@legato[1.0];
)
::

Notes can be grouped in angular brackets < > to make chords. Limitation: the properties of the first note iin the chord are used for the complete chord. "< 1[4]_8 4 1{+25.0}[5]>" is a chord consisting of score degrees 1[4] 4[4] and 1{+25.0}[5].

code::
(
var chord = < 1[4]_8 4 1{+25.0}[5]>;
)
::

STRONG::repeats::

Lastly, notes can be put between repeat brackets and the number of repeats can be indicated, e.g.
|: 1[4]_8 2 3 :|*5 repeats notes 1[4]_8 2 3 five times.

code::
(
var repeats = "|: 1[4]_8 2 3 :| * 5";
)
::

STRONG::tuning::

To convert Mitola scores to frequencies it is necessary to know in which tuning the score has to be interpreted. Tuning is indicated in the form of a scala definition and a root frequency. In order to pin a given score degree in your scala definition to a fixed frequency (e.g. ensure that A4 is 440Hz in a 12EDO tuning), a suitable root_frequency can be calculated using the RootFrequencyCalculator class.

From a Mitola score, one can get extract all kinds of information in the form of supercollider patterns.
See example code.
'''
*/

Mitola {
	/*
	[method.notation]
	description='''
	the notation as passed into the Mitola class constructor
	[method.notation.returns]
	what="a string"
	'''
	*/
	var <>notation;
	/*
	[method.mitola_parser]
	description='''
	A Parser for Mitola strings; initialized in the init method
	[method.mitola_parser.returns]
	what="a Parser"
	'''
	*/
	var <>mitola_parser;
	/*
	[method.scala_calculator]
	description='''
	A calculator object for doing tuning calculations based on scala information
	[method.scala_calculator.returns]
	what="a ScalaCalculator"
	'''
	*/
	var <>scala_calculator;

	/*
	[method.mitola_parse_result]
	description='''
	The parse tree that results from parsing the Mitola string
	[method.mitola_parse_result.returns]
	what="a parse tree"
	'''
	*/
	var <>mitola_parse_result;

	/*
	[method.gNOTEequivalenceinterval_DEFAULT]
	description='''
	the default equivalence interval of a note (think "octave" in traditional music), in case no equivalence interval was ever specified in one of the previous notes (typically "4")
	'''
	[method.gNOTEequivalenceinterval_DEFAULT.returns]
	what = "an Integer"
	*/
	var <>gNOTEequivalenceinterval_DEFAULT;
	/*
	[method.gDURATION_DEFAULT]
	description='''
	the default duration of a note in beats, in case no duration was ever specified in one of the previous notes (typically "4")
	'''
	[method.gDURATION_DEFAULT.returns]
	what = "an Integer"
	*/
	var <>gDURATION_DEFAULT;
	/*
	[method.gMODIFIER_DEFAULT]
	description='''
	default modifier (pitch alteration) of a note - typically the empty string
	'''
	[method.gMODIFIER_DEFAULT.returns]
	what = "a string"
	*/
	var <>gMODIFIER_DEFAULT;
	/*
	[method.gMULTIPLIER_DEFAULT]
	description='''
	default duration multiplier of a note (typically "1")
	'''
	[method.gMULTIPLIER_DEFAULT.returns]
	what = "an Integer"
	*/
	var <>gMULTIPLIER_DEFAULT;
	/*
	[method.gDIVIDER_DEFAULT]
	description='''
	default duration divider of a note (typically "1")
	'''
	[method.gDIVIDER_DEFAULT.returns]
	what = "a string"
	*/
	var <>gDIVIDER_DEFAULT;
	/*
	[method.gVOLUME_DEFAULT]
	description='''
	default volume of a note, between 0 and 1 (typically "0.5")
	'''
	[method.gVOLUME_DEFAULT.returns]
	what = "a Float"
	*/
	var <>gVOLUME_DEFAULT;
	/*
	[method.gLEGATO_DEFAULT]
	description='''
	default legato (indication for legato/staccato) of a note, between 0 and 1 (typically "0.9")
	'''
	[method.gLEGATO_DEFAULT.returns]
	what = "a Float"
	*/
	var <>gLEGATO_DEFAULT;
	/*
	[method.gLAG_DEFAULT]
	description='''
	default lag of a note (typically "0")
	'''
	[method.gLAG_DEFAULT.returns]
	what = "a Float"
	*/
	var <>gLAG_DEFAULT;
	/*
	[method.gDOTS_DEFAULT]
	description='''
	default number of dots after a note (typically an empty string) - like in traditional notation a dot adds half of the duration to the specified duration. Multiple dots are supported too.
	'''
	[method.gDOTS_DEFAULT.returns]
	what = "an Integer"
	*/
	var <>gDOTS_DEFAULT;
	/*
	[method.gTEMPO_DEFAULT]
	description='''
	default tempo (typically 80 bpm) - note that tempo is a special key in that it influences the tempo of the complete system (so all other voices running in parallel are affected too). For this reason, when deriving supercollider patterns from panola strings, the inclusion of the tempo-key is made optional.
	'''
	[method.gTEMPO_DEFAULT.returns]
	what = "a Float"
	*/
	var <>gTEMPO_DEFAULT;
	/*
	[method.customProperties]
	description='''
	a lookup table containing all properties specified in the Mitola input string
	'''
	[method.customProperties.returns]
	what = "a Dictionary"
	*/
	var <>customProperties;

	/*
	[method.previous_duration]
	description='''
	a variable maitaining the last specified duration, in case a note has not duration specified, in which the previous should be used
	'''
	[method.previous_duration.returns]
	what = "a Float"
	*/
	var<>previous_duration;
	/*
	[method.previous_dots]
	description='''
	a variable maitaining the last specified number of dots, in case a note has no duration specified, in which the previous should be used
	'''
	[method.previous_dots.returns]
	what = "a Float"
	*/
	var<>previous_dots;
	/*
	[method.previous_multiplier]
	description='''
	a variable maitaining the last specified multiplier, in case a note has no duration specified, in which the previous should be used
	'''
	[method.previous_multiplier.returns]
	what = "a Float"
	*/
	var<>previous_multiplier;
	/*
	[method.previous_divider]
	description='''
	a variable maitaining the last specified divider, in case a note has no duration specified, in which the previous should be used
	'''
	[method.previous_divider.returns]
	what = "a Float"
	*/
	var<>previous_divider;

	/*
	[classmethod.new]
	description='''
	new creates a new Mitola object
	'''
	[classmethod.new.args]
	notation = "Mitola string containing score"
	scala_contents = "string containing scala definition (you have to specify either this or scala_filename)"
	scala_filename = "string containing path to scala definition file (you have to specify either this or a scala_contents string)"
	degree_mapping = "optional Dictionary contain map from score degree (one-based Integer) to scala degree (one-based Integer)"
	note_equivalenceinterval_default= "default equave for a note if none was ever specified"
	dur_default = "default duration for note if none was ever specified"
	modifier_default = "default note modifier if none was ever specified"
	mult_default = "default duration multiplier if none was ever specified"
	div_default = "default duration divider if none was ever specified"
	amp_default = "default amp if none was every specifiefd"
	legato_default = "default legato if none was ever specified"
	lag_default = "default note lag if none was every specified"
	tempo_default = "default tempo if none was every specified (note: to extract tempo fro the score needs to be explicitly requested)"
	[classmethod.new.returns]
	what="new Mitola object"
	*/
	*new {
		|  notation=nil, scala_contents=nil, scala_filename=nil,
		degree_mapping = nil, note_equivalenceinterval_default=4, dur_default=4, modifier_default="",
		mult_default=1, div_default=1, amp_default=0.5,
		legato_default=0.9, lag_default=0, tempo_default=80 |

		^super.new.init(notation, scala_contents, scala_filename,
			degree_mapping, note_equivalenceinterval_default=4, dur_default=4, modifier_default="",
			mult_default=1, div_default=1, amp_default=0.5,
			legato_default=0.9, lag_default=0, tempo_default=80);
	}

	/*
	[method.init]
	description='''
	new creates a new Mitola object
	'''
	[method.init.args]
	notation = "Mitola string containing score"
	scala_contents = "string containing scala definition (you have to specify either this or scala_filename)"
	scala_filename = "string containing path to scala definition file (you have to specify either this or a scala_contents string)"
	degree_mapping = "optional Dictionary contain map from score degree (one-based Integer) to scala degree (one-based Integer)"
	note_equivalenceinterval_default= "default equave for a note if none was ever specified"
	dur_default = "default duration for note if none was ever specified"
	modifier_default = "default note modifier if none was ever specified"
	mult_default = "default duration multiplier if none was ever specified"
	div_default = "default duration divider if none was ever specified"
	amp_default = "default amp if none was every specifiefd"
	legato_default = "default legato if none was ever specified"
	lag_default = "default note lag if none was every specified"
	tempo_default = "default tempo if none was every specified (note: to extract tempo fro the score needs to be explicitly requested)"
	[method.init.returns]
	what="new Mitola object"
	*/
	init {
		| notation, scala_contents, scala_filename,
		degree_mapping, note_equivalenceinterval_default, dur_default, modifier_default,
		mult_default, div_default, amp_default,
		legato_default, lag_default, tempo_default |

		this.gNOTEequivalenceinterval_DEFAULT = note_equivalenceinterval_default;
		this.gDURATION_DEFAULT = dur_default;
		this.gMODIFIER_DEFAULT = modifier_default;
		this.gMULTIPLIER_DEFAULT = mult_default;
		this.gDIVIDER_DEFAULT = div_default;
		this.gVOLUME_DEFAULT = amp_default;
		this.gLEGATO_DEFAULT = legato_default;
		this.gLAG_DEFAULT = lag_default;
		this.gTEMPO_DEFAULT = tempo_default;

		this.previous_duration = this.gDURATION_DEFAULT;
		this.previous_dots = this.gDOTS_DEFAULT;
		this.previous_multiplier = this.gMULTIPLIER_DEFAULT;
		this.previous_divider = this.gDIVIDER_DEFAULT;

		this.notation = notation;
		if (this.notation.isNil) {
			"Error. Pass a notation string into the constructor.".postln;
			^nil;
		};
		this.mitola_parser = MitolaParser();
		this.mitola_parse_result = this.mitola_parser.parse(this.notation);

		if (scala_contents.notNil) {
			this.scala_calculator = ScalaCalculator();
			this.scala_calculator.parse(scala_contents);
			scala_calculator.max_scala_degree.debug("max scala degree");
			this.scala_calculator.degree_mapper = DegreeMapper(scala_calculator.max_scala_degree+1, degree_mapping);
		} {
			if (scala_filename.notNil) {
				this.scala_calculator = ScalaCalculator();
				this.scala_calculator.parse_file(scala_filename);
				scala_calculator.max_scala_degree.debug("max scala degree");
				this.scala_calculator.degree_mapper = DegreeMapper(scala_calculator.max_scala_degree+1, degree_mapping);
			} {
				"Error. Pass either a scala string or a scala file name into the constructor.".postln;
				^nil;
			}
		};
	}


	/*
	[method.degree_mapper]
	description='''
	An object that knows how to map between \score and \scala degrees.
	'''
	[method.degree_mapper.returns]
	what="a DegreeMapper, or nil if no mapping is needed (meaning \\score and \\scala degrees are the same)"
	*/
	degree_mapper {
		^this.scala_calculator.degree_mapper;
	}


	/*
	[method.frequency_pattern]
	description='''
	Extracts from the Mitola score only the frequency information in the form of a supercollider pattern that generates the frequencies.
	To make this possible a root_frequency needs to be passed in (and a valid scala definition must have been parsed).

	To calculate a root frequency that pins a given note in your scale to a desired frequency
	(e.g. to ensure that in 12EDO, A4 maps to 440Hz, you can use the RootFrequencyCalculator class.

	If you call .asStream.all on the result of this method, you get the frequencies from the score as a list of numbers.
	'''
	[method.frequency_pattern.args]
	root_frequency = "base frequency required to interpret scala information as frequencies"
	[method.frequency_pattern.returns]
	what="a supercollider pattern generating the frequencies in the score"
	*/
	frequency_pattern {
		| root_frequency |
		var freq_list = this.mitola_parse_result.collect({
			| el |
			if (el[\what] == \singlenote) {
				this.scala_calculator.pr_note_pitch_parse_tree_to_freq(el[\info][\note][\pitch], root_frequency);
			} {
				if (el[\what] == \chord) {
					el[\notes].collect({
						| note |
						this.scala_calculator.pr_note_pitch_parse_tree_to_freq(note[\info][\note][\pitch], root_frequency);
					});
				} {
					"Mitola.freq_pattern internal error. This shouldn't happen.".postln;
				}
			}
		});
		^Pseq(freq_list, 1);
	}

	/*
	[method.midi_note_pattern]
	description='''
	Extracts from the Mitola score only the frequency information in the form of a supercollider pattern that generates fractional midi note numbers as they would appear if the tuning was 12EDO.
	To make this possible a root_frequency needs to be passed in (and a valid scala definition must have been parsed).

	To calculate a root frequency that pins a given note in your scale to a desired frequency
	(e.g. to ensure that in 12EDO, A4 maps to 440Hz, you can use the RootFrequencyCalculator class.

	If you call .asStream.all on the result of this method, you get the frequencies from the score as a list of numbers.
	'''
	[method.midi_note_pattern.args]
	root_frequency = "base frequency required to interpret scala information as frequencies"
	[method.midi_note_pattern.returns]
	what="a supercollider pattern generating the 12EDO midi note numbers in the score"
	*/
	midi_note_pattern {
		| root_frequency |
		// frequencies recalculated to fractional midi notes in 12EDO tuning... ignore if this confuses you
		var midi_note_list = this.mitola_parse_result.collect({
			| el |
			if (el[\what] == \singlenote) {
				this.scala_calculator.pr_note_pitch_parse_tree_to_freq(el[\info][\note][\pitch], root_frequency).cpsmidi;
			} {
				if (el[\what] == \chord) {
					el[\notes].collect({
						| note |
						this.scala_calculator.pr_note_pitch_parse_tree_to_freq(note[\info][\note][\pitch], root_frequency).cpsmidi;
					});
				} {
					"Mitola.freq_pattern internal error. This shouldn't happen.".postln;
				}
			}
		});
		^Pseq(midi_note_list, 1);
	}

	/*
	[method.duration_pattern]
	description = "extracts from the current Mitola string a Pseq pattern containing only the midi note durations in the form of numbers corresponding to the durations in beats of the notes in the Mitola string"
	[method.duration_pattern.returns]
	what = "a pattern (Pseq)"
	*/
	duration_pattern {
		var durlist;
		this.previous_duration = this.gDURATION_DEFAULT;
		this.previous_dots = this.gDOTS_DEFAULT;
		this.previous_multiplier = this.gMULTIPLIER_DEFAULT;
		this.previous_divider = this.gDIVIDER_DEFAULT;
		durlist = this.mitola_parse_result.collect({
			| el |
			// for chords use first note properties for all chord
			var dur_el = if (el[\what] == \chord) { el[\notes][0][\info][\note][\duration]; } { el[\info][\note][\duration]; };
			var duration = dur_el[\dur];
			var num_of_dots = dur_el[\durdots];
			var multiplier = dur_el[\durmultiplier];
			var divider = dur_el[\durdivider];
			if (duration == \previous) {
				duration = this.previous_duration;
			} {
				this.previous_duration = duration;
			};
			if (num_of_dots == \previous) {
				num_of_dots = this.previous_dots;
			} {
				this.previous_dots = num_of_dots;
			};
			if (multiplier == \previous) {
				multiplier = this.previous_multiplier;
			} {
				this.previous_multiplier = multiplier;
			};
			if (divider == \previous) {
				divider = this.previous_divider;
			} {
				this.previous_divider = divider;
			};
			(4/duration)*(2-(1/(2.pow(num_of_dots))))*(multiplier/divider);
		});
		^Pseq(durlist, 1);
	}

	/*
	[method.total_duration]
	description = "calculates the total duration in beats of this Mitola string"
	[method.total_duration.returns]
	what = "a Float"
	*/
	total_duration {
		^this.duration_pattern.asStream.all.sum;
	}

	/*
	[method.amplitude_pattern]
	description = "calculates a pattern realizing the amplitudes in the Mitola score"
	[method.amplitude_pattern.returns]
	what = "a pattern"
	*/
	amplitude_pattern {
		^this.pr_animated_pattern("amp", \staticproperty, this.gVOLUME_DEFAULT);
	}

	/*
	[method.lag_pattern]
	description = "calculates a pattern realizing the lag values in the Mitola score"
	[method.lag_pattern.returns]
	what = "a pattern"
	*/
	lag_pattern {
		^this.pr_animated_pattern("lag", \staticproperty, this.gLAG_DEFAULT);
	}

	/*
	[method.legato_pattern]
	description = "calculates a pattern realizing the lag values in the Mitola score"
	[method.legato_pattern.returns]
	what = "a pattern"
	*/
	legato_pattern {
		^this.pr_animated_pattern("legato", \staticproperty, this.gLEGATO_DEFAULT);
	}

	/*
	[method.tempo_pattern]
	description = "calculates a pattern realizing the tempo values in the Mitola score; the tempo values are divided by 60 so they can be passed to a tempo clock"
	[method.tempo_pattern.returns]
	what = "a pattern"
	*/
	tempo_pattern {
		^(this.pr_animated_pattern("tempo", \staticproperty, this.gTEMPO_DEFAULT)/60.0);
	}

	/*
	[method.custom_property_pattern]
	description = "calculates a pattern realizing the tempo values in the Mitola score; the tempo values are divided by 60 so they can be passed to a tempo clock"
	[method.custom_property_pattern.args]
	custom_string = "name of the property to extract from the score"
	default_value  = "default value for the property if it has never been specified before"
	[method.custom_property_pattern.returns]
	what = "a pattern"
	*/
	custom_property_pattern {
		| custom_string, default_value=0 |
		^(this.pr_animated_pattern(custom_string, \staticproperty, default_value));
	}

	/*
	[method.as_pbind]
	description = "calculates a pattern containing all desired properties in the score"
	[method.as_pbind.args]
	instrument= "instrument that will play the pattern; should be a symbol that is used as name in a SynthDef (default=\\default)"
	root_frequency= "root frequency, required to map scala degrees to actual frequencies. Use RootFrequencyCalculator if you want to pin a given note to a desired frequency"
	include_custom_properties= "boolean, indicating if the pbind should just contain a few standard properties, or all properties specified in teh score"
	custom_property_defaults="Dictionary (may be nil) to define default values for custom properties"
	include_tempo="boolean to indicate if tempo should be part of extracted pattern (default: false). By extracting tempo from the pattern, you lose the ability to play the pattern against a different TempoClock."
	[method.as_pbind.returns]
	what = "a pattern"
	*/
	as_pbind {
		|instrument=\default, root_frequency=nil, include_custom_properties=true, custom_property_defaults=nil, include_tempo=false|
		if (root_frequency.isNil) {
			"Error! Must pass root frequency argument.".postln;
			^nil;
		};
		if (custom_property_defaults.isNil) {
			custom_property_defaults = Dictionary.newFrom([
				"amp", this.gVOLUME_DEFAULT,
				"lag", this.gLAG_DEFAULT,
				"legato", this.gLEGATO_DEFAULT
			]);
			if (include_tempo) {
				custom_property_defaults["tempo"] = this.gTEMPO_DEFAULT;
			};
		} {
			custom_property_defaults.put("amp", gVOLUME_DEFAULT);
			custom_property_defaults.put("lag", gLAG_DEFAULT);
			custom_property_defaults.put("legato", gLEGATO_DEFAULT);
			if (include_tempo) {
				custom_property_defaults.put("tempo", gTEMPO_DEFAULT);
			}
		};
		if (include_custom_properties.not) {
			if (include_tempo) {
				^Pbind(
					\instrument, instrument,
					//\midinote, this.midi_note_pattern(root_frequency), // fractional values
					\freq, this.frequency_pattern(root_frequency),
					\dur, this.duration_pattern,
					\lag, this.lag_pattern,
					\legato, this.legato_pattern,
					\amp, this.amplitude_pattern,
					\tempo, this.tempo_pattern
				);
			} {
				^Pbind(
					\instrument, instrument,
					//\midinote, this.midi_note_pattern(root_frequency), // fractional values
					\freq, this.frequency_pattern(root_frequency),
					\dur, this.duration_pattern,
					\lag, this.lag_pattern,
					\legato, this.legato_pattern,
					\amp, this.amplitude_pattern,
				);
			}
		} {
			var properties = this.pr_extract_all_property_name_sym;
			var mapped_props = [];
			var resulting_pattern;
			if (include_tempo.not) {
				properties.removeAt("tempo");
			};
			properties.keysValuesDo({
				| stringproperty, pbindkey |
				var default_val = 0.0;
				var scale = 1.0;
				if (custom_property_defaults[stringproperty] != nil) {
					default_val = custom_property_defaults[stringproperty];
				};
				if (stringproperty.compare("tempo") == 0) {
					scale = 1/60.0;
				};
				mapped_props = mapped_props.add([pbindkey, this.custom_property_pattern(stringproperty, default_val)*scale]);
			});
			mapped_props = mapped_props.flatten;
			resulting_pattern = Pbind(
				\instrument, instrument,
				//\midinote, this.midi_note_pattern(root_frequency),
				\freq, this.frequency_pattern(root_frequency),
				\dur, this.duration_pattern,
				*mapped_props
			);
			^resulting_pattern;
		}
	}


    /*
	[method.as_pmono]
	description = "method to return a pattern generating all the properties in the mitola string; intended for using with supercollider synths"
	[method.as_pmono.args]
	instrument = "name of the synthdef to use in the pattern's \\instrument key"
	root_frequency = "root frequency"
	include_custom_properties = "boolean to indicate if the pattern should contain user defined properties as well; if set to false only properties \\instrument, \\midinote, \\dur, \\lag, \\legato, \\amp and optionally \\tempo are extracted"
	custom_property_defaults = "a Dictionary specifying default values for used defined properties"
	include_tempo = "a boolean to indicate if tempo should be part of the Pbind. Note that the tempo key modifies the TempoClock and therefore influences all voices playing on that same TempoClock in the system (which may not be desired...)"
	[method.as_pmono.returns]
	what = "a pattern (Pmono) realizing the mitola string"
	*/
	as_pmono {
		| instrument=\default, root_frequency=nil, include_custom_properties=true, custom_property_defaults=nil, include_tempo=true|
		var result = Pmono(instrument);
		result.patternpairs_(this.as_pbind(instrument, root_frequency, include_custom_properties, custom_property_defaults, include_tempo).patternpairs);
		^result;
	}

    /*
	[method.as_pmonoartic]
	description = "method to return a pattern generating all the properties in the mitola string; intended for using with supercollider synths"
	[method.as_pmonoartic.args]
	instrument = "name of the synthdef to use in the pattern's \\instrument key"
	root_frequency = "root frequency"
	include_custom_properties = "boolean to indicate if the pattern should contain user defined properties as well; if set to false only properties \\instrument, \\midinote, \\dur, \\lag, \\legato, \\amp and optionally \\tempo are extracted"
	custom_property_defaults = "a Dictionary specifying default values for used defined properties"
	include_tempo = "a boolean to indicate if tempo should be part of the Pbind. Note that the tempo key modifies the TempoClock and therefore influences all voices playing on that same TempoClock in the system (which may not be desired...)"
	[method.as_pmonoartic.returns]
	what = "a pattern (Pmono) realizing the mitola string"
	*/
	as_pmonoartic {
		| instrument=\default, root_frequency=nil, include_custom_properties=true, custom_property_defaults=nil, include_tempo=true|
		var result = PmonoArtic(instrument);
		result.patternpairs_(this.as_pbind(instrument, root_frequency, include_custom_properties, custom_property_defaults, include_tempo).patternpairs);
		^result;
	}

	/*
	[method.pr_extract_all_properties]
	description='''
	internal method to extract a list of all properties that occur in a score
	'''
	[method.pr_extract_all_properties.returns]
	what = "a list of properties (may contain duplicates)"
	*/
	pr_extract_all_properties {
		var props = this.mitola_parse_result.collect({
			| el |
			if (el[\what] == \chord) {
				el[\notes][0][\info][\props];
			} {
				el[\info][\props];
			}
		});
		^props;
	}

	/*
	[method.pr_extract_all_property_name_sym]
	description='''
	internal method to extract a Dictionary of (propertyname -> propertyname.asSymbol)
	'''
	[method.pr_extract_all_property_name_sym.returns]
	what = "a Dictionary"
	*/
	pr_extract_all_property_name_sym {
		var custom_properties = Dictionary.newFrom([
			"amp", \amp,
			"tempo", \tempo,
			"lag", \lag,
			"legato", \legato
		]);
		var props = this.pr_extract_all_properties.flatten;
		props.do({
			|prop|
			custom_properties.put(prop[\propertyname], prop[\propertyname].asSymbol);
		});
		^custom_properties;
	}

	/*
	[method.pr_animated_pattern]
	description = "internal method to return a pattern generating the values of a Mitola property, also taking into account the defined automations - this is a generic method that is used by practically all other pattern extraction functions"
	[method.pr_animated_pattern.returns]
	what = "a pattern (Pseq)"
	*/
	pr_animated_pattern {
		| prop_name="vol", default_type = \staticproperty, default_propval = 0.5 |
		var currval = default_propval;
		var patlist = [];

		// extract only property information
		// example of a property: ( 'what': 'staticproperty', 'propertyname': "vol", 'value': 0.7 )
		var proplist = this.pr_extract_all_properties;

		// filter out properties with name prop_name only + add distance between current and previous occurrence
		var props_for_propname = [];
		var distance = 0;
		var clumped = [];
		var clumpedsize = 0;
		proplist.do({
			|propsfornote|
			var foundVol = false;
			propsfornote.do({
				|singleprop|
				if (singleprop[\propertyname].compare(prop_name) == 0) {
					var copyprop = singleprop.copy();
					distance = distance + 1;
					copyprop[\distance] = distance;
					props_for_propname = props_for_propname.add(copyprop);
					currval = singleprop[\value];
					foundVol = true;
					distance = 0;
				};
			});
			if (foundVol.not) {
				distance = distance + 1;
			};
		});
		// finalize property list for property prop_name with a staticproperty for the final value
		props_for_propname = props_for_propname.add(( 'what': 'staticproperty', 'propertyname': prop_name, 'value': currval, 'distance':distance ));

		// now turn into patterns
		clumped = props_for_propname.slide(2, 1).clump(2); // turn [p1, p2, p3] into [[p1,p2],[p2,p3]] to iterate by two with overlap
		clumpedsize = clumped.size;
		if (clumped.size == 0){
			patlist = patlist.add(Pseq([default_propval], proplist.size));
		} {
			if (clumped[0][0][\distance].asInteger != 1) {
				patlist = patlist.add(Pseq([default_propval], clumped[0][0][\distance]-1));
			};
			clumped.do({
				| pair, idx |
				var type = pair[0][\what];
				var beginval = pair[0][\value];
				var endval = pair[1][\value];
				var length = pair[1][\distance];
				var number = length;
				if (idx == (clumped.size-1)) {
					number = number + 1;
				};
				if (type == \animatedproperty) {
					patlist = patlist.add(Pseries(beginval, ((endval - beginval)/(length)), number));
				} {
					patlist = patlist.add(Pseq([beginval], number));
				};
			});
		};
		^Pseq(patlist, 1);
	}

}

/*
[examples]
what = '''
// Mitola is a way to extract Pbind keys from a concise specification for microtonal music.
// First things first. To install Mitola (you need to do this only once) we need two quarks:

Quarks.install("https://github.com/shimpe/scparco"); // parser combinator library
Quarks.install("https://github.com/shimpe/mitola"); // mitola implementation

// Let's start with the "Hello world" of Mitola: a simple scale.
// In microtonal music, we need to define the scale degrees first. I'm here inlining the scala contents,
// but you can also load them from file by passing a scala_filename instead of scala_contents.
(
s.waitForBoot({
	var tuning = [
		"! 12edo.scl",
		"!",
		"12 edo",
		" 12",
		"!",
		" | 1/12 >",
		" | 2/12 >",
		" | 3/12 >",
		" | 4/12 >",
		" | 5/12 >",
		" | 6/12 >",
		" | 7/12 >",
		" | 8/12 >",
		" | 9/12 >",
		" | 10/12 >",
		" | 11/12 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp[0.6] 2 3 4 5 6 7 8 9 10 11 12 1[5]", scala_contents: tuning);
	// find out which root frequency to use to get degree 10 in octave 4 (A4) to map to 440Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("10[4]", 440);
	var player = score.as_pbind(root_frequency:root_freq).play; // listen to the score with the default instrument
});
)

// Then a diatonic major scale selected from a 12EDO tuning by using note mapping:
(
s.waitForBoot({
	var tuning = [
		"! 12EDO.scl",
		"!",
		"12 EDO",
		" 12",
		"!",
		" | 1/12 >",
		" | 2/12 >",
		" | 3/12 >",
		" | 4/12 >",
		" | 5/12 >",
		" | 6/12 >",
		" | 7/12 >",
		" | 8/12 >",
		" | 9/12 >",
		" | 10/12 >",
		" | 11/12 >",
		" 2/1"
	].join("\n");
	var m = Mitola("1[4]_16 2 3 4 5 6 7 1[5]",
		scala_contents:tuning,
		degree_mapping:Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);
	var r = RootFrequencyCalculator(scala_contents:tuning, degree_mapper:m.degree_mapper);
	var root_freq = r.get_root_frequency("6[4]", 440); // a4 to 440Hz (6 is a one-based score degree, not a scala degree!)
	var pattern = m.as_pbind(root_frequency:root_freq);
	var player = pattern.play;
});
)

// another example of playing two lines simultaneously in 7EDO tuning
// (note: nothing prevents you from writing the second line in a different tuning of course...)
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp[0.6] 2 3_32 4 5_16 6 7 1[5]_4", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.6] 5 1 5 1 5 1 5 1", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var player = Ppar([
		score.as_pbind(root_frequency:root_freq),
		score2.as_pbind(root_frequency:root_freq)
	]).play; // listen to score and score2 simultaneously with the default instrument
});
)

// if you define synths in supercollider, you can use them instead of the default instrument to play
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp[0.6] 2 3_32 4 5_16 6 7 1[5]_4", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.6] 5 1 5 1 5 1 5 1", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var pattern = Ppar([
		score.as_pbind(\sawSynth, root_frequency:root_freq),
		score2.as_pbind(\sawSynth, root_frequency:root_freq)
	]);
	var player;

	SynthDef(\sawSynth, { arg freq = 440, amp = 0.1, att = 0.1, rel = 2, lofreq = 1000, hifreq = 3000, pan = 0;
		var env, snd;
		env = Env.perc(
			attackTime: att,
			releaseTime: rel,
			level: amp
		).kr(doneAction: 2);
		snd = Saw.ar(freq: freq * [0.99, 1, 1.001, 1.008], mul: env);
		snd = LPF.ar(
			in: snd,
			freq: LFNoise2.kr(1).range(lofreq, hifreq)
		);
		snd = Mix.ar(snd);
		snd = Pan2.ar(snd, pan);
		Out.ar(0, snd);
		// Basic saw synth
		//By Bruno Ruviaro
		//http://sccode.org/1-54H
	}).add;

	s.sync;

	player = pattern.play;
});
)

// some other Mitola syntax
(
var degree = "1"; // a degree without modifier in default equave, default duration, no properties attached
var degree_modified = "1{+50.0}"; // degree 1 augmented with 50.0 cents
var degree_modified2 = "2{-1/3}"; // degree 2 lowered by 1/3 of the gap between degree 2 and degree 1
var degree_modified3 = "4{+4/3}"; // note 4/3 = 3/3 + 1/3 -> degree 5 augmented by 1/3 of the gap between 5 and 6
var degree_equave = "2[5]"; // degree 2 in the 5th equivalence interval (equave, think "octave" in trad. notation)
var degree_mod_equave = "2{+56.0}[3]"; // degree 2 in 3rd equave, augmented by 56.0 cents
var degree_duration = "2_4"; // degree 2 as a quarter note
var degree_duration2 = "2[3]_16"; // degree 2 in third equave as a sixteenth note
var degree_duration3 = "2[3]_16."; // degree 2 in third equave as a dotted sixteenth note
var degree_duration4 = "2[3]_16*2/3"; // degree 2 in third equave as sixteenth*2/3 note (sixteenth triplet)
var degree_duration = "1_8 3 5_16 7 3_8"; // rhythmic values are reused until a new one is specified: here 1,3 are eighth notes; 5,7 are 16th notes and 3 is again an eighth note
var rest = "r_4"; // quarter rest
var rest2 = "r_32"; // 32nd rest
var chord = "< 1[4]_4 5 1[5] >"; // chord made of notes 1[4], 5 and 1[5] lasting one quarter note -> properties of first note are used for the chord
var repeats = " |: 1 2 3 4 :|*3"; // notes 1,2,3 and 4 played 3 times in a row
var props = "1@amp[0.3]@legato{0.1} 2 3 4@amp[0.5] 5 6@amp[0.6]@legato{0.9}"; // notes 1,2,3 have amp=0.3; note 4,5 have amp=0.5 and note 6 has amp=0.6. Square brackets keep value constant until new value is specified.
var props2 = "1@amp[0.3]@legato{0.1} 2 3 4@amp[0.5] 5 6@amp[0.6]@legato{0.9}"; // note 1 has property legato=0.1 which gradually increases to note 6 with legato 0.9. Curly brackets linearly animate property values.

//Properties added in the score appear as keys in the supercollider pattern that is extracted from the score so you can use them for anything you'd like by postprocessing the pattern extracted from the score with a Pbindf.

)

// using properties, we can now even change synth parameters
// Wrapping property values in {} animates them in the notes between, so here we make a crescendo in the melody
// between amp=0.2 and amp=0.6. To understand better what happens: "amp" appears in the score as a property, and in the SynthDef as an argument. This shared name is how the score is linked to the SynthDef.
// If you wrap the property values in [], the values remain static, so in the accompaniment, the amplitude stays
// at 0.3, until the last note where it becomes 0.6.
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp{0.2} 2 3_32 4 5_16 6 7 1[5]_4@amp{0.6}", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.3] 5 1 5 1 5 1 5 1@amp[0.6]", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var pattern = Ppar([
		score.as_pbind(\sawSynth, root_frequency:root_freq),
		score2.as_pbind(\sawSynth, root_frequency:root_freq)
	]);
	var player;

	SynthDef(\sawSynth, { arg freq = 440, amp = 0.1, att = 0.1, rel = 2, lofreq = 1000, hifreq = 3000, pan = 0;
		var env, snd;
		env = Env.perc(
			attackTime: att,
			releaseTime: rel,
			level: amp
		).kr(doneAction: 2);
		snd = Saw.ar(freq: freq * [0.99, 1, 1.001, 1.008], mul: env);
		snd = LPF.ar(
			in: snd,
			freq: LFNoise2.kr(1).range(lofreq, hifreq)
		);
		snd = Mix.ar(snd);
		snd = Pan2.ar(snd, pan);
		Out.ar(0, snd);
		// Basic saw synth
		//By Bruno Ruviaro
		//http://sccode.org/1-54H
	}).add;

	s.sync;

	player = pattern.play;
});
)

'''
*/
