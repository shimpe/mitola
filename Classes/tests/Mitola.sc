Mitola {
	var <>notation;
	var <>mitola_parser;
	var <>scala_calculator;
	var <>mitola_parse_result;

	/*
	[method.gNOTEREPEATINTERVAL_DEFAULT]
	description='''
	the default repeat interval of a note (in many scales this would be called the "octave"). in case no repeat interval was ever specified in one of the previous notes
	(typically "4")
	'''
	[method.gNOTEREPEATINTERVAL_DEFAULT.returns]
	what = "a string"
	*/
	var <>gNOTEREPEATINTERVAL_DEFAULT;
	/*
	[method.gDURATION_DEFAULT]
	description='''
	the default duration of a note in beats, in case no duration was ever specified in one of the previous notes (typically "4")
	'''
	[method.gDURATION_DEFAULT.returns]
	what = "a string"
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
	what = "a string"
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
	what = "a string"
	*/
	var <>gVOLUME_DEFAULT;
	/*
	[method.gLEGATO_DEFAULT]
	description='''
	default playdur (indication for legato/staccato) of a note, between 0 and 1 (typically "0.9")
	'''
	[method.gLEGATO_DEFAULT.returns]
	what = "a string"
	*/
	var <>gLEGATO_DEFAULT;
	/*
	[method.gLAG_DEFAULT]
	description='''
	default lag of a note (typically "0")
	'''
	[method.gLAG_DEFAULT.returns]
	what = "a string"
	*/
	var <>gLAG_DEFAULT;
	/*
	[method.gDOTS_DEFAULT]
	description='''
	default number of dots after a note (typically an empty string) - like in traditional notation a dot adds half of the duration to the specified duration. Multiple dots are supported too.
	'''
	[method.gDOTS_DEFAULT.returns]
	what = "a string"
	*/
	var <>gDOTS_DEFAULT;
	/*
	[method.gTEMPO_DEFAULT]
	description='''
	default tempo (typically 80 bpm) - note that tempo is a special key in that it influences the tempo of the complete system (so all other voices running in parallel are affected too). For this reason, when deriving supercollider patterns from panola strings, the inclusion of the tempo-key is made optional.
	'''
	[method.gTEMPO_DEFAULT.returns]
	what = "a string"
	*/
	var <>gTEMPO_DEFAULT;
	/*
	[method.customProperties]
	description='''
	a lookup table containing all properties specified in the panola input string
	'''
	[method.customProperties.returns]
	what = "a Dictionary"
	*/
	var <>customProperties;

	var<>previous_duration;
	var<>previous_dots;
	var<>previous_multiplier;
	var<>previous_divider;

	*new {
		|  notation=nil, scala_contents=nil, scala_filename=nil,
		note_repeatinterval_default=4, dur_default=4, modifier_default="",
		mult_default=1, div_default=1, vol_default=0.5,
		playdur_default=0.9, lag_default=0, tempo_default=80 |

		^super.new.init(notation, scala_contents, scala_filename,
			note_repeatinterval_default=4, dur_default=4, modifier_default="",
			mult_default=1, div_default=1, vol_default=0.5,
			playdur_default=0.9, lag_default=0, tempo_default=80);
	}

	init {
		| notation, scala_contents, scala_filename,
		note_repeatinterval_default, dur_default, modifier_default,
		mult_default, div_default, vol_default,
		playdur_default, lag_default, tempo_default |

		this.gNOTEREPEATINTERVAL_DEFAULT = note_repeatinterval_default;
		this.gDURATION_DEFAULT = dur_default;
		this.gMODIFIER_DEFAULT = modifier_default;
		this.gMULTIPLIER_DEFAULT = mult_default;
		this.gDIVIDER_DEFAULT = div_default;
		this.gVOLUME_DEFAULT = vol_default;
		this.gLEGATO_DEFAULT = playdur_default;
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
		} {
			if (scala_filename.notNil) {
				this.scala_calculator = ScalaCalculator();
				this.scala_calculator.parseFile(scala_filename);
			} {
				"Error. Pass either a scala string or a scala file name into the constructor.".postln;
				^nil;
			}
		};
	}

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
	[method.durationPattern]
	description = "extracts from the current panola string a Pseq pattern containing only the midi note durations in the form of numbers corresponding to the durations in beats of the notes in the panola string"
	[method.durationPattern.returns]
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

	total_duration {
		^this.duration_pattern.asStream.all.sum;
	}

	amplitude_pattern {
		^this.pr_animated_pattern("amp", \staticproperty, this.gVOLUME_DEFAULT);
	}

	lag_pattern {
		^this.pr_animated_pattern("lag", \staticproperty, this.gLAG_DEFAULT);
	}

	legato_pattern {
		^this.pr_animated_pattern("legato", \staticproperty, this.gLEGATO_DEFAULT);
	}

	tempo_pattern {
		^(this.pr_animated_pattern("tempo", \staticproperty, this.gTEMPO_DEFAULT)/60.0);
	}

	custom_property_pattern {
		| custom_string, default_value=0 |
		^(this.pr_animated_pattern(custom_string, \staticproperty, default_value));
	}

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


				stringproperty.debug("prop");
				(this.custom_property_pattern(stringproperty, default_val)*scale).asStream.all.postcs;

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
	[method.pr_animatedPattern]
	description = "internal method to return a pattern generating the values of a panola property, also taking into account the defined automations - this is a generic method that is used by practically all other pattern extraction functions"
	[method.pr_animatedPattern.returns]
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