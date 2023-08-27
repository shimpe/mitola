/*
[general]
title = "MtlMitolaTests"
summary = "tests for the MtlMitolaParser class"
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola"
description = '''
unit tests
'''
*/
MtlMitolaTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_freq_pattern {
		var scala_text = [
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
		var m = MtlMitola("1[4]_8@vol[0.3]@legato{0.1} <2@vol[0.7] 3> 1[5]_2@legato[0.9]", scala_contents: scala_text);
		var r = MtlRootFrequencyCalculator();
		var root_freq, freqs, durs, vol_props, rez_props, legato_props;
		r.parse(scala_text);
		// in C 12edo (= c chromatic), degree 10 in octave 4 (i.e. A4) should map to 440Hz
		root_freq = r.get_root_frequency("10[4]", 440);
		freqs = m.frequency_pattern(root_freq).asStream.all;
		this.assertFloatEquals(freqs[0], 261.63, "freqs[0]", 0.005);
		this.assertArrayFloatEquals(freqs[1], [ 277.18, 293.66], "freqs[1]", 0.005);
		this.assertFloatEquals(freqs[2], 523.25, "freqs[2]", 0.005);

		durs = m.duration_pattern.asStream.all;
		this.assertArrayFloatEquals(durs, [0.5, 0.5, 2.0], "durations", 0.005);

		vol_props = m.pr_animated_pattern("vol").asStream.all;
		this.assertArrayFloatEquals(vol_props, [0.3, 0.7, 0.7], "vol_props", 0.005);

		rez_props = m.pr_animated_pattern("rez").asStream.all; // default values since not present in pattern
		this.assertArrayFloatEquals(rez_props, [0.5, 0.5, 0.5], "rez_props", 0.005);

		legato_props = m.pr_animated_pattern("legato").asStream.all;
		this.assertArrayFloatEquals(legato_props, [0.1, 0.5, 0.9], "legato_props", 0.005);
	}

	test_dur_pattern {
		var	tuning = [
			"! homemadeflute.scl",
			"!",
			"tuning of an (imaginary) home made flute",
			" 12",
			"!",
			" 1/1",            // degree 2
			" 156.000 cents",  // degree 3
			" 240.000 cents",  // degree 4
			" 276.000 cents",  // degree 5
			" 312.000 cents",  // degree 6
			" 480.000 cents",  // degree 7
			" 678.000 cents",  // degree 8
			" 720.000 cents",  // degree 9
			" 777.000 cents",  // degree 10
			" 834.000 cents",  // degree 11
			" 960.000 cents",  // degree 12
			"2/1"].join("\n");

		// to keep the score somewhat editable, separate notes from properties
		var track = [
			["2[5]_4",                 "@pan[0]@legato[0.97]@fallofftime[0.25]@falloff[0.1875]@amp{0.5}"],
			["6_2 5_4 6_2 8_4",        ""],
			["9_2",                    "@amp{1.0}"],
			["8_8*2/3 9 8",            ""],
			["6_2",                    "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
			["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
			["5_2 4_4 2_4. 2_8",       "@legato[0.8]@fallofftime[0.3]@falloff[0.2]"],
			["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
			["5_2 4_8*2/3 6 4 2_2",    "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
			["2_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]@amp{0.5}"],
			["6_2 5_4 6_2 8_4 9_2",    "@amp{1.0}"],
			["8_8*2/3 9 8 6_2",        "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
			["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
			["5_2 4_4 2_2",            "@legato[0.5]"],
			["10[4]_4",                "@legato[1.0]"],
			["2[5]_2.",                 "@fallofftime[0.7]@falloff[0.5]"],
		];

		var score = track.collect({
			| line |
			line[0].stripWhiteSpace ++ line[1].stripWhiteSpace
		}).join(" ");

		var durations = MtlMitola(score, scala_contents:tuning).duration_pattern.asStream.all;
		var defaultdurations;
		this.assertArrayFloatEquals(durations, [ 1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 0.33333333333333, 0.33333333333333, 0.33333333333333, 2.0, 1.0, 2.0, 1.0, 1.5, 0.5, 1.0, 2.0, 0.33333333333333, 0.33333333333333, 0.33333333333333, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 0.33333333333333, 0.33333333333333, 0.33333333333333, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0, 3.0 ], "durations with reset", 0.005);

		defaultdurations = MtlMitola("1[4]", scala_contents:tuning).duration_pattern.asStream.all;
		this.assertArrayFloatEquals(defaultdurations, 1.0, "default duration", 0.005);

	}
}
