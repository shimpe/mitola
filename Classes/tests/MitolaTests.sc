MitolaTests : UnitTest {
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
		var m = Mitola("1[4]_8@vol[0.3]@legato{0.1} <2@vol[0.7] 3> 1[5]_2@legato[0.9]", scala_contents: scala_text);
		var r = RootFrequencyCalculator();
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
}
