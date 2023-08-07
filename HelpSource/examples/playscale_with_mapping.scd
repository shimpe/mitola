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
	var m = MtlMitola("1[4]_16 2 3 4 5 6 7 1[5]",
		scala_contents:scala,
		degree_mapping:Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);
	var r = MtlRootFrequencyCalculator(scala_contents:scala, degree_mapper:m.degree_mapper);
	var root_freq = r.get_root_frequency("6[4]", 440); // a4 to 440Hz (6 is a one-based score degree, not a scala degree!)
	var pattern = m.as_pbind(root_frequency:root_freq);
	var player = pattern.play;
});
)
