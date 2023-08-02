/*
[general]
title = "DegreeMapper"
summary = "conversion between score degrees and scala degrees"
categories = "Microtonal utils"
related = "Classes/Mitola, Classes/ScalaCalculator"
description = '''
a class that can map between score degrees and scala degrees. The two are different in case of a degree mapping that is set up.
Using a degree mapping of e.g.
code::
Dictionary[1->1,2->3, 3->5, 4->6, 5->8, 6->10, 7->12]
::
degrees 1,2,3,4,5,6,7 in the score map to degrees 1,3,5,6,8,10,12 in the scala definition. This can be used e.g. to define a diatonic major key based on the 12EDO scala definition.
'''
*/
DegreeMapper {
	/*
	[method.max_scala_degree]
	description='''
	an Integer indicating what the highest scala degree is (1-based)
	'''
	[method.max_scala_degree.returns]
	what = "an Integer"
	*/
	var <>max_scala_degree;
	/*
	[method.score_to_scala_lookup]
	description='''
	a Dictionary mapping from score degrees to scala degrees. If set to nil, there's a one-to-one correspondence between score and scala degrees.
	'''
	[method.score_to_scala_lookup.returns]
	what = "a Dictionary"
	*/
	var <>score_to_scala_lookup;
	/*
	[method.max_score_degree]
	description='''
	max score degree (1-based)
	'''
	[method.max_score_degree.returns]
	what = "an Integer"
	*/
	var <>max_score_degree;

	/*
	[classmethod.new]
	description = "New creates a new DegreeMapper"
	[classmethod.new.args]
	max_scala_degree_1based = "max degree number (Integer) as defined in the scala definition"
	score_to_scala_1based  = "mapping from score degrees to scala degrees (Dictionary[Integer->Integer])"
	[classmethod.new.returns]
	what = "a new DegreeMapper"
	*/
	*new {
		| max_scala_degree_1based =nil, score_to_scala_1based =nil |
		^super.new.init(max_scala_degree_1based, score_to_scala_1based);
	}

	/*
	[method.init]
	description = "init initializes a new DegreeMapper"
	[method.init.args]
	max_scala_degree_1based = "max degree number (Integer) as defined in the scala definition"
	score_to_scala_1based  = "mapping from score degrees to scala degrees (Dictionary[Integer->Integer])"
	[method.init.returns]
	what = "a new DegreeMapper"
	*/
	init {
		| max_scala_degree_1based =nil, score_to_scala_1based =nil |
		var keys, values;

		if (max_scala_degree_1based.isNil) {
			"Error. Need to pass in the max degree number from scala (0 based)".postln;
			^nil;
		};
		this.max_scala_degree = max_scala_degree_1based;
        if (score_to_scala_1based.isNil) {
			var map = Dictionary.newFrom(max_scala_degree.collect({
				| deg |
				[Degree(deg, 0, \score, \zerobased), Degree(deg, 0, \scala, \zerobased)]
			}).flatten);
			this.score_to_scala_lookup = map;
			this.max_score_degree = max_scala_degree;
		} {
			var new_elements = [];
			var max_deg = 0;
			// convert from 1-based (user facing) to 0-based (internal) representation
			score_to_scala_1based.keysValuesDo({
				| key, value |
				var zerobasedkey = key-1;
				if (zerobasedkey > max_deg) { max_deg = zerobasedkey; };
				new_elements = new_elements.add([Degree(zerobasedkey, 0, \score, \zerobased), Degree(value-1, 0, \scala, \zerobased)]);
			});
			this.max_score_degree = max_deg + 1;
			this.score_to_scala_lookup = Dictionary.newFrom(new_elements.flatten);
		};

		keys = this.score_to_scala_lookup.keys;
		if ((keys.minItem.degree_value(\onebased)) < 1 || (keys.maxItem.degree_value(\onebased) > keys.size))
		{
			("Error. Invalid mapping: score degrees must lie in [1,"+keys.size++"]").postln;
			^nil;
		};
		values = this.score_to_scala_lookup.values;
		if ((values.minItem.degree_value(\onebased) < 1) || (values.maxItem.degree_value(\onebased) > this.max_scala_degree)) {
			("Error. Invalid mapping: scala degrees must lie in [1,"+max_scala_degree++"]").postln;
			^nil;
		};
		^this;
	}

	/*
	[method.score_to_scala]
	description='''
	maps a Degree of kind \score to a degree of kind \scala. This method will warn if the argument is not a Degree of type \score
	'''
	[method.score_to_scala.args]
	degree = "a Degree"
	[method.score_to_scala.returns]
	what = "a Degree of kind \\scala, or nil if the lookup failed"
	*/
	score_to_scala {
		| degree |
		var scale_degree;
		if (degree.degree_kind != \score) {
			"Warning. Expected a \\score degree but got a \\scala degree instead.".postln;
			^degree;
		};
		scale_degree = this.score_to_scala_lookup[degree];
		if (scale_degree.isNil) {
			("Error. Couldn't find \\score degree in lookup table. degree =" + degree + "table is "+this.score_to_scala_lookup).postln;
			^nil;
		};
		^scale_degree;
	}

	/*
	[method.to_scala]
	description='''
	maps a Degree to \scala. This method will not warn if the passed in degree is already a \scala degree.
	'''
	[method.to_scala.args]
	degree = "a Degree"
	[method.to_scala.returns]
	what = "a Degree of kind \\scala, or nil if the lookup failed"
	*/
	to_scala {
		| degree |
		var scale_degree;
		if (degree.degree_kind != \score) {
			if ((degree.degree_value(\onebased) > this.max_scala_degree) || (degree.degree_value(\zerobased) < 0)) {
				("Error. Scala degree falls outside valid range. Degree = " + degree + "Max scala degree (onebased) = " + this.max_scala_degree).postln;
				^nil;
			};
			^degree;
		};
		this.score_to_scala_lookup.keysValuesDo({
			|key,value|

			if (key.value == degree.degree_value(\zerobased))
			{
				scale_degree = value;
			}
		});
		if (scale_degree.isNil) {
			("Error. Couldn't find \\score degree in lookup table. degree =" + degree + "table is "+this.score_to_scala_lookup).postln;
			^nil;
		};
		scale_degree.equave = degree.equave;
		^scale_degree;
	}

	/*
	[method.printOn]
	description='''
	override of Object.printOn to make sure a DegreeMapper prints something useful when called with .postln
	'''
	[method.printOn.args]
	stream = "stream on which to print"
	[method.printOn.returns]
	what = "the updated stream"
	*/
	printOn {
		| stream |
		stream << "(";
		this.score_to_scala.keysValuesDo({
			| key, value |
			stream << key << "->" << value << " ";
		});
		stream << ")";
	}

	/*
	[method.scala_to_score]
	description='''
	maps a Degree of kind \scala to a degree of kind \score. This method will warn if the argument is not a Degree of type \score.
	Note that such lookup will fail for scala degrees that have no corresponding score degree. Then it returns nil.
	'''
	[method.scala_to_score.args]
	degree = "a Degree"
	[method.scala_to_score.returns]
	what = "a Degree of kind \\score, or nil if the lookup failed"
	*/
	scala_to_score {
		| degree |
		var score_degree;
		if (degree.degree_kind != \scala) {
			"Warning. Expected a \\scala degree but got a \\score degree instead.".postln;
		};
		this.to_score(degree);
	}

	/*
	[method.to_score]
	description='''
	maps a Degree of kind \scala to a degree of kind \score. This method will not warn if the argument is not a Degree of type \score.
	Note that such lookup will fail for scala degrees that have no corresponding score degree. Then it returns nil.
	'''
	[method.to_score.args]
	degree = "a Degree"
	[method.to_score.returns]
	what = "a Degree of kind \\score, or nil if the lookup failed"
	*/
	to_score {
		| degree |
		var score_degree;
		var lookup_degree;
		if (degree.degree_kind != \scala) {
			if ((degree.degree_value(\onebased) > this.max_score_degree) || (degree.degree_value(\zerobased) < 0)) {
				("Error. Score degree falls outside valid range. Degree = " + degree + "Max score degree (onebased) = " + this.max_score_degree).postln;
				^nil;
			};
			^degree;
		};
		lookup_degree = degree.copy();
		lookup_degree.equave = 0;
		score_degree = this.score_to_scala_lookup.findKeyForValue(lookup_degree);
		if (score_degree.isNil) {
			"Error. \\scala degree has no corresponding \\score degree.".postln;
			^nil;
		};
		score_degree.equave = degree.equave;
		^score_degree;
	}

	/*
	[method.next_degree]
	description='''
	next_degree returns the next Degree, taking care of wrapping if needed.
	for degrees of kind \score, the next \score Degree is returned; for degrees of kind \scala, the next \scala Degree is returned
	'''
	[method.next_degree.args]
	degree = "a Degree"
	[method.next_degree.returns]
	what = "the next Degree of the same kind"
	*/
	next_degree {
		| degree |
		if (degree.degree_kind == \scala) {
			// scale degree
			var new_degree = Degree(degree.degree_value(\zerobased) + 1, degree.equave, \scala, \zerobased);
			if (new_degree.degree_value(\onebased) > this.max_scala_degree) {
				new_degree.value = 0;
				new_degree.equave = new_degree.equave + 1;
			};
			^new_degree;
		} {
			// score degree
			var new_degree = Degree(degree.degree_value(\zerobased) + 1, degree.equave, \score, \zerobased);
			if (new_degree.degree_value(\onebased) > this.max_score_degree) {
				new_degree.value = 0;
				new_degree.equave = new_degree.equave + 1;
			};
			^new_degree;
		};
	}

	/*
	[method.previous_degree]
	description='''
	previous_degree returns the previous Degree, taking care of wrapping if needed.
	for degrees of kind \score, the previous \score Degree is returned; for degrees of kind \scala, the previous \scala Degree is returned
	'''
	[method.previous_degree.args]
	degree = "a Degree"
	[method.previous_degree.returns]
	what = "the previous Degree of the same kind"
	*/
	previous_degree {
		| degree |
		if (degree.degree_kind == \scala) {
			// scale degree
			var new_degree = Degree(degree.degree_value(\zerobased) - 1, degree.equave, \scala, \zerobased);
			if (new_degree.degree_value(\zerobased) < 0) {
				new_degree.value = this.max_scala_degree - 1; // internally we calculate everything zerobased
				new_degree.equave = new_degree.equave - 1;
			};
			^new_degree;
		} {
			// score degree
			var new_degree = Degree(degree.degree_value(\zerobased) - 1, degree.equave, \score, \zerobased);
			if (new_degree.degree_value(\zerobased) < 0) {
				new_degree.value = this.max_score_degree - 1; // internally we calculate everything zerobased
				new_degree.equave = new_degree.equave - 1;
			};
			^new_degree;
		};
	}

}

/*
[examples]
what = '''
(
var m = DegreeMapper(12, Dictionary[1->1, 2->3, 3->5, 4->6, 5->8, 6->10, 7->12]);
var d1 = Degree(1, 4, \score, \onebased);
var d2 = Degree(2, 4, \score, \onebased);
(m.previous_degree(d1) == Degree(7, 3, \score, \onebased)).debug("check"); // expect true
(m.to_scala(d2) == Degree(3, 4, \scala, \onebased)).debug("check 2"); // expect true
)
'''
*/
