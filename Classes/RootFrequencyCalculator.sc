/*
[general]
title = "RootFrequencyCalculator"
summary = "a calculator for determining the root frequency that ensures a given mitola degree has a certain frequency"
categories = "Microtonal utils"
related = "Classes/Mitola"
description = '''
RootFrequencyCalculator calculates the root frequency to ensure a given degree in your scala scale is pinned to a desired frequency, e.g. calculate the root frequency so that 10[4] in 12EDO ("a4" in a c chromatic scale is the 10th degree) equals 440Hz.
'''
*/
RootFrequencyCalculator {
	classvar epsilon;

	/*
	[method.scala_calculator]
	description='''
	a variable to store an initialized scala calculator
	'''
	[method.scala_calculator.returns]
	what="a ScalaCalculator"
	*/
	var <>scala_calculator;

	/*
	[classmethod.new]
	description = "New creates a new RootFrequencyCalculator. Either a scala_contents or a scala_filename must be specified."
	[classmethod.new.args]
	scala_contents = "a string containing a scala definition"
	scala_filename = "a string containing a path to a scala definition file"
	[classmethod.new.returns]
	what = "a new RootFrequencyCalculator"
	*/
	*new {
		| scala_contents=nil, scala_filename=nil |
		^super.new.init(scala_contents, scala_filename);
	}

	/*
	[classmethod.initClass]
	description = "initializes the class variable epsilon to 0.001"
	[classmethod.initClass.returns]
	what = "initialized class variable epsilon"
	*/
	*initClass {
		epsilon = 0.001;
	}

	/*
	[method.init]
	description = "initializes the RootFrequencyCalculator from either a string or a file"
	[method.init.args]
	scala_contents = "a string containing a valid scala definition"
	scala_filename= "a string containing a path to a scala file"
	[method.init.returns]
	what = "initialized RootFrequencyCalculator"
	*/
	init {
		|scala_contents, scala_filename|
		if (scala_contents.notNil) {
			this.parse(scala_contents);
		} {
			if (scala_filename.notNil) {
				this.parse_file(scala_filename);
			}
		}
		^this;
	}

	/*
	[method.parse]
	description = "parses a scala definition specified in a string"
	[method.parse.args]
	scala_contents = "a string containing a valid scala definition"
	[method.parse.returns]
	what = "a ScalaCalculator initialized with the information from the string"
	*/
	parse {
		| scala_contents |
		this.scala_calculator = ScalaCalculator();
		this.scala_calculator.parse(scala_contents);
		^this.scala_calculator;
	}

	/*
	[method.parse_file]
	description = "parses a scala definition specified in a file"
	[method.parse_file.args]
	filename = "a filename containing a valid scala definition"
	[method.parse_file.returns]
	what = "a ScalaCalculator initialized with the information from the file"
	*/
	parse_file {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^this.parse(contents.join("\n"));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
		};
		^this.scala_calculator;
	}

	/*
	[method.pr_bisect]
	description = "internal method performing an iterative search for a root_frequency that pins a given degree to a given frequency"
	[method.pr_bisect.args]
	mitola_note_string = "string containing a valid mitola note (= degree decorated with optional modifier, equivalence interval, duration, properties)"
	desired_frequency = "the frequency that should be assigned to the mitola_note_string"
	freq_lowerbound = "current lower bound for frequency"
	freq_upperbound = "current upper bound for frequency"
	[method.pr_bisect.returns]
	what = "a Float (root frequency)"
	*/
	pr_bisect {
		| mitola_note_string, desired_frequency, freq_lowerbound, freq_upperbound |
		var freql = this.scala_calculator.note_to_freq(mitola_note_string, freq_lowerbound);
		var freqh =this.scala_calculator.note_to_freq(mitola_note_string, freq_upperbound);
		var mean = (freq_lowerbound + freq_upperbound)/2.0;
		var freqmean = this.scala_calculator.note_to_freq(mitola_note_string, mean);

		if ((freqh-freql) < epsilon) {
			^mean;
		};

		if (freqmean > desired_frequency) {
			^this.pr_bisect(mitola_note_string, desired_frequency, freq_lowerbound, mean);
		} {
			if (freqmean < desired_frequency) {
				^this.pr_bisect(mitola_note_string, desired_frequency, mean, freq_upperbound);
			} {
				^mean;
			}
		}
	}

	/*
	[method.get_root_frequency]
	description = "main method of this class: calculates a root frequency so that the mitola_note_string passed in is mapped to desired_frequency. This can be used to ensure that e.g. A4 = degree 10 in a C chromatic scale = 10[4] in mitola notation maps to 440Hz"
	[method.get_root_frequency.args]
	mitola_note_string = "string containing a valid mitola note (= degree decorated with optional modifier, equivalence interval, duration, properties)"
	desired_frequency = "the frequency that should be assigned to the mitola_note_string"
	[method.get_root_frequency.returns]
	what = "a Float (root frequency)"
	*/
	get_root_frequency {
		| mitola_note_string, desired_frequency |
		var freqlow = 0.01;
		var freqhigh = desired_frequency*1.1;
		var root_freq = this.pr_bisect(mitola_note_string, desired_frequency, freqlow, freqhigh);
		if (desired_frequency <= 0.01) {
			"Error. Must use frequency > 0.01.".postln;
			^0;
		};
		^root_freq; // would it be possible to derive a closed form solution instead?
	}

/*
[examples]
what = '''
(
var r = RootFrequencyCalculator();
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
r.get_root_frequency("6[4]", 440).debug("root frequency:"); // expected: 16.35
r.get_root_frequency("6{+56.0}[4]", 432).debug("root frequency:"); // expected: 15.54
)
	'''
*/
}