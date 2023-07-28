ScalaParserTests : UnitTest {
	*new {
		^super.new.init();
	}

	test_parse_comment {
		var p = ScalaParser.pr_comment;
		var state = p.run("! blblHBLAHBLA.SCL  \n");
		this.assertEquals(state.isError, false);
		this.assertEquals(state.result, (\what : \comment, \value : " blblHBLAHBLA.SCL  "));
	}

	test_parse_comment_line {
		var p = ScalaParser.pr_commentLine;
		var state = p.run("! blblHBLAHBLA.SCL  \n");
		this.assertEquals(state.isError, false);
		this.assertEquals(state.result, (\what : \comment, \value : " blblHBLAHBLA.SCL  "));
	}

	test_parse_pitch_ratio {
		var state, state2, state3, state4, state5, state6, state7, state8;
		var p = ScalaParser.pr_pitchRatio;

		state = p.run("23/123");
		this.assertEquals(state.isError, false, "state_error");
		this.assertEquals(state.result[\what], \pitch, "state_what");
		this.assertEquals(state.result[\kind], \ratio, "state_kind");
		this.assertEquals(state.result[\numerator], 23, "state_numerator");
		this.assertEquals(state.result[\denominator], 123, "state_denominator");

		state2 = p.run("-23/123");
		this.assertEquals(state2.isError, true, "state2_error");

		state3 = p.run("23.5/123");
		this.assertEquals(state3.isError, true, "state3_error");

		state4 = p.run(" 23    / 7875   blahblahblah");
		this.assertEquals(state4.isError, false, "state4_error");
		this.assertEquals(state4.result[\what], \pitch, "state4_what");
		this.assertEquals(state4.result[\kind], \ratio, "state4_kind");
		this.assertEquals(state4.result[\numerator], 23, "state4_numerator");
		this.assertEquals(state4.result[\denominator], 7875, "state4_denominator");

		// should be interpreted as an integer followed by comment because ratios cannot be mixed with floats
		state5 = p.run(" 23    / 7875.5   blahblahblah");
		this.assertEquals(state5.isError, false, "state5_error");
		this.assertEquals(state5.result[\what], \pitch, "state5_what");
		this.assertEquals(state5.result[\kind], \ratio, "state5_kind");
		this.assertEquals(state5.result[\numerator], 23, "state5_numerator");
		this.assertEquals(state5.result[\denominator], 1, "state5_denominator");

		state6 = p.run(" 23.5 / 6 lbahbalhb");
		this.assertEquals(state6.isError, true, "state6_error");

		state7 = p.run(" 56 blabhblabh");
		this.assertEquals(state7.isError, false, "state7_error");
		this.assertEquals(state7.result[\what], \pitch, "state7_what");
		this.assertEquals(state7.result[\kind], \ratio, "state7_kind");
		this.assertEquals(state7.result[\numerator], 56, "state7_numerator");
		this.assertEquals(state7.result[\denominator], 1, "state7_denominator");

		state8 = p.run(" 23 /  78 .9 blabhblabh"); // here .9 is part of the rest of the line -> to be accepted
		this.assertEquals(state8.isError, false, "state8_error");
		this.assertEquals(state8.result[\what], \pitch, "state8_what");
		this.assertEquals(state8.result[\kind], \ratio, "state8_kind");
		this.assertEquals(state8.result[\numerator], 23, "state8_numerator");
		this.assertEquals(state8.result[\denominator], 78, "state8_denominator");
	}

	test_parse_pitch_cents {
		var state, state2;
		var p = ScalaParser.pr_pitchCents;
		state = p.run("   100.0 blaj;ldajb");
		this.assertEquals(state.isError, false, "state_error");
		this.assertEquals(state.result[\what], \pitch, "state_what");
		this.assertEquals(state.result[\kind], \cents, "state_kind");
		this.assertEquals(state.result[\numerator], 100.0, "state_numerator");
		this.assertEquals(state.result[\denominator], 1, "state_denominator");

		state2 = p.run(" 23.5 / 6 lbahbalhb");
		this.assertEquals(state2.isError, false, "state2_error");
		this.assertEquals(state2.result[\what], \pitch, "state2_what");
		this.assertEquals(state2.result[\kind], \cents, "state2_kind");
		this.assertEquals(state2.result[\numerator], 23.5, "state2_numerator");
		this.assertEquals(state2.result[\denominator], 1, "state2_denominator");
	}

	test_parse_prime_vector {
		var state, state2;
		var p = ScalaParser.pr_pitchPrimeVector;
		state = p.run("|-2 2 3/2>");
		this.assertEquals(state.isError, false, "state_isError");
		this.assertEquals(state.result[\kind], \primevector, "state_result0_kind");
		this.assertEquals(state.result[\exponents].size, 3, "state_result0_numofresults");
		this.assertEquals(state.result[\exponents][0][\what], \primeexponent, "state_result0_primeexponent");
		this.assertEquals(state.result[\exponents][0][\kind], \ratio, "state_result0_ratio");
		this.assertEquals(state.result[\exponents][0][\numerator], -2, "state_result0_numerator");
		this.assertEquals(state.result[\exponents][0][\denominator], 1, "state_result0_denominator");
		this.assertEquals(state.result[\exponents][1][\what], \primeexponent, "state_result1_primeexponent");
		this.assertEquals(state.result[\exponents][1][\kind], \ratio, "state_result1_ratio");
		this.assertEquals(state.result[\exponents][1][\numerator], 2, "state_result1_numerator");
		this.assertEquals(state.result[\exponents][1][\denominator], 1, "state_result1_denominator");
		this.assertEquals(state.result[\exponents][2][\what], \primeexponent, "state_result2_primeexponent");
		this.assertEquals(state.result[\exponents][2][\kind], \ratio, "state_result2_ratio");
		this.assertEquals(state.result[\exponents][2][\numerator], 3, "state_result2_numerator");
		this.assertEquals(state.result[\exponents][2][\denominator], 2, "state_result2_denominator");
		state2 = p.run(" |  -4 3/ -4      -7/8 > 42 and other cruft to be ignored");
		this.assertEquals(state2.isError, false, "state2_isError");
		this.assertEquals(state2.result[\kind], \primevector, "state2_result0_kind");
		this.assertEquals(state2.result[\exponents].size, 3, "state2_result0_numofresults");
		this.assertEquals(state2.result[\exponents][0][\what], \primeexponent, "state2_result0_primeexponent");
		this.assertEquals(state2.result[\exponents][0][\kind], \ratio, "state2_result0_ratio");
		this.assertEquals(state2.result[\exponents][0][\numerator], -4, "state2_result0_numerator");
		this.assertEquals(state2.result[\exponents][0][\denominator], 1, "state2_result0_denominator");
		this.assertEquals(state2.result[\exponents][1][\what], \primeexponent, "state2_result1_primeexponent");
		this.assertEquals(state2.result[\exponents][1][\kind], \ratio, "state2_result1_ratio");
		this.assertEquals(state2.result[\exponents][1][\numerator], 3, "state2_result1_numerator");
		this.assertEquals(state2.result[\exponents][1][\denominator], -4, "state2_result1_denominator");
		this.assertEquals(state2.result[\exponents][2][\what], \primeexponent, "state2_result2_primeexponent");
		this.assertEquals(state2.result[\exponents][2][\kind], \ratio, "state2_result2_ratio");
		this.assertEquals(state2.result[\exponents][2][\numerator], -7, "state2_result2_numerator");
		this.assertEquals(state2.result[\exponents][2][\denominator], 8, "state2_result2_denominator");
	}

	test_parse_pitch {
		var state, state2, state3, state4, state5, state6, state7;
		var p = ScalaParser.pr_pitchParser;
		state = p.run("  234.231 comments to be ignored");
		this.assertEquals(state.isError, false, "state_isError");
		this.assertEquals(state.result, (\what: \pitch, \kind: \cents, \numerator: 234.231, \denominator:1), "state_result");
		state2 = p.run(" 34.6/23 comments to be ignored"); // looks like ratio, but is cents because of floating point
		this.assertEquals(state2.isError, false, "state_isError");
		this.assertEquals(state2.result, (\what: \pitch, \kind: \cents, \numerator: 34.6, \denominator:1), "state2_result");
		state3 = p.run(" 5/6 23.53 "); // ratio  5/6 with 23.53 as comment
		this.assertEquals(state3.isError, false, "state3_iserror");
		this.assertEquals(state3.result, (\what: \pitch, \kind: \ratio, \numerator: 5, \denominator:6), "state3_result");
		state4 = p.run(" -5/6 23.53"); // negative ratios not allowed
		this.assertEquals(state4.isError, true, "state4_iserror");
		state5 = p.run(" -23.53"); // negative cents not allowed
		this.assertEquals(state5.isError, true, "state5_iserror");
		state6 = p.run(" | -4/5 0.234 > ignoreme"); // exponents can only be integers, rationals but not floats
		this.assertEquals(state6.isError, true, "state6_iserror");
		state7 = p.run(" | 4/ -5 2 -1> ignoreme"); // in prime vector notation, negative numbers are allowed
		this.assertEquals(state7.isError, false, "state7_iserror");
		this.assertEquals(state7.result[\exponents][0][\what], \primeexponent, "state7_result0_primeexponent");
		this.assertEquals(state7.result[\exponents][0][\kind], \ratio, "state7_result0_ratio");
		this.assertEquals(state7.result[\exponents][0][\numerator], 4, "state7_result0_numerator");
		this.assertEquals(state7.result[\exponents][0][\denominator], -5, "state7_result0_denominator");
		this.assertEquals(state7.result[\exponents][1][\what], \primeexponent, "state7_result1_primeexponent");
		this.assertEquals(state7.result[\exponents][1][\kind], \ratio, "state7_result1_ratio");
		this.assertEquals(state7.result[\exponents][1][\numerator], 2, "state7_result1_numerator");
		this.assertEquals(state7.result[\exponents][1][\denominator], 1, "state7_result1_denominator");
		this.assertEquals(state7.result[\exponents][2][\what], \primeexponent, "state7_result2_primeexponent");
		this.assertEquals(state7.result[\exponents][2][\kind], \ratio, "state7_result2_ratio");
		this.assertEquals(state7.result[\exponents][2][\numerator], -1, "state7_result2_numerator");
		this.assertEquals(state7.result[\exponents][2][\denominator], 1, "state7_result2_denominator");
	}

	test_parse_scala {
		var state, result2;
		var p = ScalaParser.pr_scalaParser;
		var scala_txt = [
			"! meanquar.scl",
			"!",
			"1/4-comma meantone scale. Pietro Aaron's temperament (1523)",
			" 12",
			"!",
			" 76.04900",
			" 193.15686",
			" 310.26471",
			" 5/4",
			" 503.42157",
			" 579.47057",
			" 696.57843",
			" 25/16",
			" 889.73529",
			" 1006.84314",
			" 1082.89214",
			" 2/1"
		].join("\n");
		state = p.run(scala_txt);
		this.assertEquals(state.isError, false);
		this.assert(state.result[\description].compare("1/4-comma meantone scale. Pietro Aaron's temperament (1523)") == 0, "result_description");
		this.assertEquals(state.result[\repeatinterval], ('what': 'intervalrepeat', 'numerator': 2, 'denominator': 1), "result_repeatinterval");
		this.assertEquals(state.result[\degrees].size, 12, "result_noofdegrees");
		this.assertEquals(state.result[\degrees][0], ('kind': 'cents', 'what': 'pitch', 'numerator': 0, 'denominator': 1), "result_deg0");
		this.assertEquals(state.result[\degrees][1], ('numerator': 76.049, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg1");
		this.assertEquals(state.result[\degrees][2], ('numerator': 193.15686, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg2");
		this.assertEquals(state.result[\degrees][3], ('numerator': 310.26471, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg3");
		this.assertEquals(state.result[\degrees][4], ('numerator': 5, 'kind': 'ratio', 'what': 'pitch', 'denominator': 4), "result_deg4");
		this.assertEquals(state.result[\degrees][5], ('numerator': 503.42157, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg5");
		this.assertEquals(state.result[\degrees][6], ('numerator': 579.47057, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg6");
		this.assertEquals(state.result[\degrees][7], ('numerator': 696.57843, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg7");
		this.assertEquals(state.result[\degrees][8], ('numerator': 25, 'kind': 'ratio', 'what': 'pitch', 'denominator': 16), "result_deg8");
		this.assertEquals(state.result[\degrees][9], ('numerator': 889.73529, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg9");
		this.assertEquals(state.result[\degrees][10], ('numerator': 1006.84314, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg10");
		this.assertEquals(state.result[\degrees][11], ('numerator': 1082.89214, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "result_deg11");

		result2 = ScalaParser.parse(scala_txt);
		this.assertEquals(result2.isNil, false);
		this.assert(result2[\description].compare("1/4-comma meantone scale. Pietro Aaron's temperament (1523)") == 0, "2_result_description");
		this.assertEquals(result2[\repeatinterval], ('what': 'intervalrepeat', 'numerator': 2, 'denominator': 1), "2_result_repeatinterval");
		this.assertEquals(result2[\degrees].size, 12, "result_noofdegrees");
		this.assertEquals(result2[\degrees][0], ('kind': 'cents', 'what': 'pitch', 'numerator': 0, 'denominator': 1), "2_result_deg0");
		this.assertEquals(result2[\degrees][1], ('numerator': 76.049, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg1");
		this.assertEquals(result2[\degrees][2], ('numerator': 193.15686, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg2");
		this.assertEquals(result2[\degrees][3], ('numerator': 310.26471, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg3");
		this.assertEquals(result2[\degrees][4], ('numerator': 5, 'kind': 'ratio', 'what': 'pitch', 'denominator': 4), "2_result_deg4");
		this.assertEquals(result2[\degrees][5], ('numerator': 503.42157, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg5");
		this.assertEquals(result2[\degrees][6], ('numerator': 579.47057, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg6");
		this.assertEquals(result2[\degrees][7], ('numerator': 696.57843, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg7");
		this.assertEquals(result2[\degrees][8], ('numerator': 25, 'kind': 'ratio', 'what': 'pitch', 'denominator': 16), "2_result_deg8");
		this.assertEquals(result2[\degrees][9], ('numerator': 889.73529, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg9");
		this.assertEquals(result2[\degrees][10], ('numerator': 1006.84314, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg10");
		this.assertEquals(result2[\degrees][11], ('numerator': 1082.89214, 'kind': 'cents', 'what': 'pitch', 'denominator': 1), "2_result_deg11");
	}

	test_prime_def {
		var scala_txt = [
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
		var state;
		var p = ScalaParser.pr_scalaParser;
		state = p.run(scala_txt);
		this.assertEquals(state.isError, false);
	}

	init {

	}
}
