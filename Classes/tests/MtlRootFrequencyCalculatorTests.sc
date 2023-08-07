/*
[general]
title = "MtlRootFrequencyCalculatorTests"
summary = "tests for the MtlRootFrequencyCalculator class"
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola"
description = '''
unit tests
'''
*/
MtlRootFrequencyCalculatorTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_calculate_root_frequency {
		var r = MtlRootFrequencyCalculator();

		var scala = [
			"! major_diatonic.scl",
			"!",
			"major diatonic",
			" 7",
			"!",
			" 200.0",
			" 400.0",
			" 500.0",
			" 700.0",
			" 900.0",
			" 1100.0",
			" 2/1"
		].join("\n");
		r.parse(scala);
		// what is the root frequency to ensure "1[4]" maps to 440 Hz, given the above scala definition?
		this.assertFloatEquals(r.get_root_frequency("1[4]", 440), 27.5, "rootfreq_1", 0.005);
		// what is the root frequency to ensure "6[4]" maps to 440 Hz, given the above scala definition?
		this.assertFloatEquals(r.get_root_frequency("6[4]", 440), 16.35, "rootfreq_2", 0.005);
		// what is the root frequency to ensure "1{-1200.0}[4]" maps to 440 Hz, given the above scala definition?
		this.assertFloatEquals(r.get_root_frequency("1{-1200.0}[4]", 440), 55, "rootfreq_3", 0.005);
		// what is the root frequency to ensure "5{-69.0}[2]" maps to 432Hz, given the above scala definition?
		this.assertFloatEquals(r.get_root_frequency("5{-69.0}[2]", 440), 76.40, "rootfreq_4", 0.005);
		// what is the root frequency to ensure "1{+1/2}[4]" maps to 554.37 (=C#5) -> should be double of rootfreq_2 test result
		this.assertFloatEquals(r.get_root_frequency("1{+1/2}[4]", 554.37), 32.70, "rootfreq_5", 0.005);
	}
}