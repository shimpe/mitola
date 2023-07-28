ScalaCalculatorTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_degree_to_freq {
		var scala = [
			"! 12edo.scl",
			"!",
			"12 edo",
			" 12",
			"!",
			" 100.0",
			" 200.0",
			" 300.0",
			" 400.0",
			" 500.0",
			" 600.0",
			" 700.0",
			" 800.0",
			" 900.0",
			" 1000.0",
			" 1100.0",
			" 2/1"
		].join("\n");
		var calc = ScalaCalculator();
		calc.parse(scala);
		this.assertEquals(calc.no_of_degrees, 12, "no_of_degrees");
		this.assertEquals(calc.max_degree, 11, "max_degree");
		this.assertEquals(calc.note_to_freq("1[0]", 27.5), 27.5, "note_to_freq_1[0]"); // note a0
		this.assertFloatEquals(calc.note_to_freq("9[0]", 27.5), 43.65, "note_to_freq_9[0]", 0.005); // note f1
		this.assertEquals(calc.note_to_freq("1[1]", 27.5), 55, "note_to_freq_1[1]");
		this.assertEquals(calc.note_to_freq("1{+1200.0}[0]", 27.5), 55, "note_to_freq_1{+1200.0}[0]"); // raise an octave
		this.assertFloatEquals(calc.note_to_freq("1{+1/1}[0]", 27.5), 29.14, "note_to_freq_1{+1/1}[0]", 0.005); // modify to next degree
		this.assertFloatEquals(calc.note_to_freq("1{+1/2}[0]", 27.5), 28.31, "note_to_freq_1{+1/2}[0]", 0.005); // modify to half of next degree
		this.assertFloatEquals(calc.note_to_freq("1[4]", 27.5), 440, "note_to_freq_1[4]", 0.005);
		this.assertFloatEquals(calc.note_to_freq("1{-1/1}[4]", 27.5), 415.30, "note_to_freq_1{-1/1}[4]", 0.005); // descend one degree
		this.assertFloatEquals(calc.note_to_freq("1{-1/2}[4]", 27.5), 427.47, "note_to_freq_1{-1/1}[4]", 0.005); // descend half degree = quarter tone
		this.assertFloatEquals(calc.note_to_freq("1{+|-1>}[4]", 27.5), 452.89, "note_to_freq_1{+1/2}[0]", 0.005); // modify to half (2^-1) of next degree
	}

	test_degree_to_freq_gaps {
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
		var calc = ScalaCalculator();
		calc.parse(scala);
		this.assertFloatEquals(calc.note_to_freq("1[4]", 27.5), 440, "note_to_freq_1[4]", 0.005); // 1st degree is a4
		this.assertFloatEquals(calc.note_to_freq("2[4]", 27.5), 493.88, "note_to_freq_2[4]", 0.005); // 2nd degree is b4
		this.assertFloatEquals(calc.note_to_freq("1{+1/2}[4]", 27.5), 466.16, "note_to_freq_1{+1/2}[4]", 0.005); // 1+1/2 degree is a#4
		this.assertFloatEquals(calc.note_to_freq("1{+3/2}[4]", 27.5), 523.25, "note_to_freq_1{+3/2}", 0.005); // degree (1 + 3/2) = degree (2 + 1/2) = c5
		// maybe a bit surprising: halfway between 3rd (C#5) and 4rth (D5) degree is a quartertone above C#5
		this.assertFloatEquals(calc.note_to_freq("3{+1/2}[4]", 27.5), 570.61, "note_to_freq_3{+1/2}[4]", 0.005);
		// but halfway between 3rd (C#5) and 2nd (B4) degree is a C5
		this.assertFloatEquals(calc.note_to_freq("3{-1/2}[4]", 27.5), 523.25, "note_to_freq_3{-1/2}[4]", 0.005);
		// same test with prime vector ratio notation
		this.assertFloatEquals(calc.note_to_freq("3{+|-1>}[4]", 27.5), 570.61, "note_to_freq_3{+1/2}[4]", 0.005);
		// but halfway between 3rd (C#5) and 2nd (B4) degree is a C5
		this.assertFloatEquals(calc.note_to_freq("3{-|-1>}[4]", 27.5), 523.25, "note_to_freq_3{-1/2}[4]", 0.005);
	}

	test_scala_prime_vector_definitions {
		var scala = [
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
		var calc = ScalaCalculator();
		calc.parse(scala);
		this.assertEquals(calc.no_of_degrees, 12, "no_of_degrees");
		this.assertEquals(calc.max_degree, 11, "max_degree");
		this.assertEquals(calc.note_to_freq("1[0]", 27.5), 27.5, "note_to_freq_1[0]"); // note a0
		this.assertFloatEquals(calc.note_to_freq("9[0]", 27.5), 43.65, "note_to_freq_9[0]", 0.005); // note f1
		this.assertEquals(calc.note_to_freq("1[1]", 27.5), 55, "note_to_freq_1[1]");
		this.assertEquals(calc.note_to_freq("1{+1200.0}[0]", 27.5), 55, "note_to_freq_1{+1200.0}[0]"); // raise an octave
		this.assertFloatEquals(calc.note_to_freq("1{+1/1}[0]", 27.5), 29.14, "note_to_freq_1{+1/1}[0]", 0.005); // modify to next degree
		this.assertFloatEquals(calc.note_to_freq("1{+1/2}[0]", 27.5), 28.31, "note_to_freq_1{+1/2}[0]", 0.005); // modify to half of next degree
		this.assertFloatEquals(calc.note_to_freq("1[4]", 27.5), 440, "note_to_freq_1[4]", 0.005);
		this.assertFloatEquals(calc.note_to_freq("1{-1/1}[4]", 27.5), 415.30, "note_to_freq_1{-1/1}[4]", 0.005); // descend one degree
		this.assertFloatEquals(calc.note_to_freq("1{-1/2}[4]", 27.5), 427.47, "note_to_freq_1{-1/1}[4]", 0.005); // descend half degree = quarter tone
		this.assertFloatEquals(calc.note_to_freq("1{+|-1>}[4]", 27.5), 452.89, "note_to_freq_1{+1/2}[0]", 0.005); // modify to half
	}
}