/*
[general]
title = "DegreeMapperTests"
summary = "tests for the DegreeMapper class"
categories = "Microtonal utils"
related = "Classes/ScalaParser, Classes/Mitola"
description = '''
unit tests
'''
*/
DegreeMapperTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_conversion {
		var m = DegreeMapper(12, Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);

		this.assertEquals(m.to_score(Degree(12, 4, \scala, \onebased)), Degree(7, 4, \score, \onebased), "scala_to_score");
		this.assertEquals(m.to_score(Degree(13, 4, \scala, \onebased)), nil, "scala_to_score_impossible");
		this.assertEquals(m.to_score(Degree(7, 4, \score, \onebased)), Degree(7, 4, \score, \onebased), "score_to_score");
		this.assertEquals(m.to_score(Degree(8, 4, \score, \onebased)), nil, "score_to_score_impossible");

		this.assertEquals(m.to_scala(Degree(4, 4, \score, \onebased)), Degree(6, 4, \scala, \onebased), "score_to_scala");
		this.assertEquals(m.to_scala(Degree(8, 4, \score, \onebased)), nil, "score_to_scala_impossible");
		this.assertEquals(m.to_scala(Degree(12, 4, \scala, \onebased)), Degree(12, 4, \scala, \onebased), "scala_to_scala");
		this.assertEquals(m.to_scala(Degree(13, 4, \scala, \onebased)), nil, "scala_to_scala_impossible");
	}

	test_prevnext {
		var m = DegreeMapper(12, Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);
		var d1 = Degree(1, 4, \score, \onebased);
		var d2 = Degree(7, 6, \score, \onebased);
		var d3 = Degree(5, 3, \score, \onebased);
		var d4 = Degree(1, 4, \scala, \onebased);
		var d5 = Degree(12, 6, \scala, \onebased);
		var d6 = Degree(5, 3, \scala, \onebased);

		this.assertEquals(m.next_degree(d1), Degree(2, 4, \score, \onebased), "next_d1");
		this.assertEquals(m.next_degree(d2), Degree(1, 7, \score, \onebased), "next_d2");
		this.assertEquals(m.next_degree(d3), Degree(6, 3, \score, \onebased), "next_d3");
		this.assertEquals(m.next_degree(d4), Degree(2, 4, \scala, \onebased), "next_d4");
		this.assertEquals(m.next_degree(d5), Degree(1, 7, \scala, \onebased), "next_d5");
		this.assertEquals(m.next_degree(d6), Degree(6, 3, \scala, \onebased), "next_d6");

		this.assertEquals(m.previous_degree(d1), Degree(7, 3, \score, \onebased), "next_d1");
		this.assertEquals(m.previous_degree(d2), Degree(6, 6, \score, \onebased), "next_d2");
		this.assertEquals(m.previous_degree(d3), Degree(4, 3, \score, \onebased), "next_d3");
		this.assertEquals(m.previous_degree(d4), Degree(12, 3, \scala, \onebased), "next_d4");
		this.assertEquals(m.previous_degree(d5), Degree(11, 6, \scala, \onebased), "next_d5");
		this.assertEquals(m.previous_degree(d6), Degree(4, 3, \scala, \onebased), "next_d6");
	}

}