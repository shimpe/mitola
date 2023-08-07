(
s.waitForBoot({
	var scala = [
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
	var m = MtlMitola("1[4]_16 2 3 4 5 6 7 8 9 10 11 12 r 1[5]", scala_contents:scala);
	var r = MtlRootFrequencyCalculator(scala_contents:scala);
	var root_freq = r.get_root_frequency("10[4]", 440);
	var pattern = m.as_pbind(root_frequency:root_freq);
	var scala2 = [
		"! 13EDO.scl",
		"!",
		"13 EDO",
		" 13",
		"!",
		" | 1/13 >",
		" | 2/13 >",
		" | 3/13 >",
		" | 4/13 >",
		" | 5/13 >",
		" | 6/13 >",
		" | 7/13 >",
		" | 8/13 >",
		" | 9/13 >",
		" | 10/13 >",
		" | 11/13 >",
		" | 12/13 >",
		" 2/1"
	].join("\n");
	var m2 = MtlMitola("1[4]_16 2 3 4 5 6 7 8 9 10 11 12 13 1[5]", scala_contents:scala2);
	var r2 = MtlRootFrequencyCalculator(scala_contents:scala2);
	var root_freq2 = r2.get_root_frequency("10[4]", 440);
	var pattern2 = m2.as_pbind(root_frequency:root_freq);
	var player = Pseq([
		pattern,   // play 12 edo
		pattern2,  // play 13 edo
		Ppar([     // play both in parallel
			pattern,
			pattern2])],
	1).play;

});
)
