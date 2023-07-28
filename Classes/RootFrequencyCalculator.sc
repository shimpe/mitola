RootFrequencyCalculator {
	classvar epsilon;

	var <>scala_calculator;

	*new {
		| scala_contents=nil, scala_filename=nil |
		^super.new.init(scala_contents, scala_filename);
	}

	*initClass {
		epsilon = 0.001;
	}

	init {
		|scala_contents, scala_filename|
		if (scala_contents.notNil) {
			this.parse(scala_contents);
		} {
			if (scala_filename.notNil) {
				this.parseFile(scala_filename);
			}
		}
		^this;
	}

	parse {
		| scala_contents |
		this.scala_calculator = ScalaCalculator();
		this.scala_calculator.parse(scala_contents);
		^this.scala_calculator;
	}

	parseFile {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^this.parse(contents.join("\n"));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
		};
		^this.scala_calculator;
	}

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
}