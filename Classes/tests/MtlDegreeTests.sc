/*
[general]
title = "MtlDegreeTests"
summary = "tests for the MtlDegree class"
categories = "Microtonal utils"
related = "Classes/MtlScalaParser, Classes/MtlMitola, Classes/MtlDegreeMapper"
description = '''
unit tests
'''
*/
MtlDegreeTests : UnitTest {
	*new {
		^super.new.init();
	}

	init {

	}

	test_degree {
		var d1 = MtlDegree(5, 3, \score, \onebased);
		var d2 = MtlDegree(4, 3, \score, \zerobased);
		var d3 = MtlDegree(2, 3, \score, \onebased);
		this.assertEquals(d1.degree_value(\onebased), 5, "d1_degree_value_onebased");
		this.assertEquals(d1.degree_value(\zerobased), 4, "d1_degree_value_zerobased");
		this.assertEquals(d1.degree_kind, \score, "d1_degree_kind");
		this.assertEquals(d1.equave, 3, "d1_equave");
		this.assert(d1 == d2, "compare onebased to zerobased");
		this.assert(d3 < d2, "smaller than");
		this.assert(d2 > d3, "greater than");
	}
}