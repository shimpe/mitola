/*
[general]
title = "MtlDegreeMapperTests"
summary = "tests for the MtlDegreeMapper class"
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola"
description = '''
unit tests
'''
*/
MtlDegreeMapperTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_conversion {
		var m = MtlDegreeMapper(12, Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);

		this.assertEquals(m.to_score(MtlDegree(12, 4, \scala, \onebased)), MtlDegree(7, 4, \score, \onebased), "scala_to_score");
		this.assertEquals(m.to_score(MtlDegree(13, 4, \scala, \onebased)), nil, "scala_to_score_impossible");
		this.assertEquals(m.to_score(MtlDegree(7, 4, \score, \onebased)), MtlDegree(7, 4, \score, \onebased), "score_to_score");
		this.assertEquals(m.to_score(MtlDegree(8, 4, \score, \onebased)), nil, "score_to_score_impossible");

		this.assertEquals(m.to_scala(MtlDegree(4, 4, \score, \onebased)), MtlDegree(6, 4, \scala, \onebased), "score_to_scala");
		this.assertEquals(m.to_scala(MtlDegree(8, 4, \score, \onebased)), nil, "score_to_scala_impossible");
		this.assertEquals(m.to_scala(MtlDegree(12, 4, \scala, \onebased)), MtlDegree(12, 4, \scala, \onebased), "scala_to_scala");
		this.assertEquals(m.to_scala(MtlDegree(13, 4, \scala, \onebased)), nil, "scala_to_scala_impossible");
	}

	test_prevnext {
		var m = MtlDegreeMapper(12, Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);
		var d1 = MtlDegree(1, 4, \score, \onebased);
		var d2 = MtlDegree(7, 6, \score, \onebased);
		var d3 = MtlDegree(5, 3, \score, \onebased);
		var d4 = MtlDegree(1, 4, \scala, \onebased);
		var d5 = MtlDegree(12, 6, \scala, \onebased);
		var d6 = MtlDegree(5, 3, \scala, \onebased);

		this.assertEquals(m.next_degree(d1), MtlDegree(2, 4, \score, \onebased), "next_d1");
		this.assertEquals(m.next_degree(d2), MtlDegree(1, 7, \score, \onebased), "next_d2");
		this.assertEquals(m.next_degree(d3), MtlDegree(6, 3, \score, \onebased), "next_d3");
		this.assertEquals(m.next_degree(d4), MtlDegree(2, 4, \scala, \onebased), "next_d4");
		this.assertEquals(m.next_degree(d5), MtlDegree(1, 7, \scala, \onebased), "next_d5");
		this.assertEquals(m.next_degree(d6), MtlDegree(6, 3, \scala, \onebased), "next_d6");

		this.assertEquals(m.previous_degree(d1), MtlDegree(7, 3, \score, \onebased), "next_d1");
		this.assertEquals(m.previous_degree(d2), MtlDegree(6, 6, \score, \onebased), "next_d2");
		this.assertEquals(m.previous_degree(d3), MtlDegree(4, 3, \score, \onebased), "next_d3");
		this.assertEquals(m.previous_degree(d4), MtlDegree(12, 3, \scala, \onebased), "next_d4");
		this.assertEquals(m.previous_degree(d5), MtlDegree(11, 6, \scala, \onebased), "next_d5");
		this.assertEquals(m.previous_degree(d6), MtlDegree(4, 3, \scala, \onebased), "next_d6");
	}

}