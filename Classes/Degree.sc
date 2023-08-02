/*
[general]
title = "Degree"
summary = "models a Degree (can be a \\score degree or a \\scala degree)"
categories = "Microtonal utils"
related = "Classes/Mitola, Classes/ScalaCalculator"
description = '''
a class that holds a single Degree
'''
*/
Degree {
	/*
	[method.value]
	description='''
	the numerical value of the degree (0-based)
	'''
	[method.value.returns]
	what = "an Integer"
	*/
	var <>value;
	/*
	[method.equave]
	description='''
	the numerical value of the degree's equave
	'''
	[method.equave.returns]
	what = "an Integer"
	*/
	var <>equave;
	/*
	[method.score_or_scala]
	description='''
	a symbol indicating if this is a \score degree or a \scala degree
	'''
	[method.score_or_scala.returns]
	what = "a symbol \\score or \\scala"
	*/
	var <>score_or_scala;

	/*
	[classmethod.new]
	description='''
	creates a new Degree
	'''
	[classmethod.new.args]
	value = "numerical value of degree Integer"
	equave = "numerical value of equave Integer"
	score_or_scala = "one of \\score or \\scala"
	one_or_zero_based = "one of \\zerobased or \\onebased"
	[classmethod.new.returns]
	what = "a new Degree"
	*/
	*new {
		| value, equave, score_or_scala, one_or_zero_based |
		^super.new.init(value, equave, score_or_scala, one_or_zero_based);
	}

	/*
	[method.init]
	description='''
	initializes a new Degree
	'''
	[method.init.args]
	value = "numerical value Integer"
	equave = "numerical value of equave Integer"
	score_or_scala = "one of \\score or \\scala"
	one_or_zero_based = "one of \\zerobased or \\onebased"
	[method.init.returns]
	what = "an initialized Degree"
	*/
	init {
		| value, equave, score_or_scala, one_or_zero_based |
		// internally always store as 0-based
		var subtract = if (one_or_zero_based == \onebased) { 1 } { 0 };
		this.score_or_scala = score_or_scala;
		this.value = value - subtract;
		this.equave = equave;
		^this;
	}

	/*
	[method.degree_value]
	description='''
	gets the numerical value of the degree
	'''
	[method.degree_value.args]
	one_or_zero_based = "one of \\zerobased or \\onebased; determines which numerical value you get back. Interaction with the user should be \\onebased, internal usage should be \\zerobased"
	[method.degree_value.returns]
	what = "a numerical value for a degree according to the desired counting scheme"
	*/
	degree_value {
		| one_or_zero_based=nil |
		if (one_or_zero_based.isNil) {
			"Error. Must pass one of \onebased or \zerobased to Degree.value".postln;
		};
		if (one_or_zero_based == \zerobased) {
			^this.value;
		} {
			^(this.value + 1);
		}
	}


	/*
	[method.degree_kind]
	description='''
	gets the kind of degree
	'''
	[method.degree_kind.returns]
	what = "one of \\score or \\scala"
	*/
	degree_kind {
		^this.score_or_scala;
	}

	/*
	["method.=="]
	description='''
	overrides the equality operator for degrees so they can be looked up in a Dictionary
	equave is ignored for this purpose
	'''
	["method.==.args"]
	otherdegree = "another Degree to compare this one with"
	["method.==.returns"]
	what = "boolean indicating of two degrees are the same or not (ignoring equave)"
	*/
	== { // ignores equave
		| otherdegree |
		var reply = false;
		reply = ((this.value == otherdegree.value) && (this.equave == otherdegree.equave) && (this.score_or_scala == otherdegree.score_or_scala));
		^reply;
	}

	/*
	["method.<"]
	description='''
	overrides the < operator for degrees so they can be compared
	'''
	["method.<.args"]
	otherdegree = "another Degree to compare this one with"
	["method.<.returns"]
	what = "boolean indicating of this Degree < otherdegree"
	*/
	< {
		| otherdegree |
		var reply = false;
		if (this.value < otherdegree.value) { reply = true; } {
			if (this.value > otherdegree.value) { reply = false; } {
				if (this.equave < otherdegree.value) { reply = true; } {
					if (this.equave > otherdegree.value) { reply = false; } {
						if ((this.score_or_scala == \score) && otherdegree.score_or_scala == \scala) { reply = true; } {
							if ((this.score_or_scala == \scala) && otherdegree.score_or_scala == \score) { reply = false; }
						}
					}
				}
			};
		};
		^reply;
	}

	/*
	["method.>"]
	description='''
	overrides the > operator for degrees so they can be compared
	'''
	["method.>.args"]
	otherdegree = "another Degree to compare this one with"
	["method.>.returns"]
	what = "boolean indicating of this Degree > otherdegree"
	*/
	> {
		| otherdegree |
		var reply = false;
		if (this.value > otherdegree.value) { reply = true; } {
			if (this.value < otherdegree.value) { reply = false; } {
				if (this.equave > otherdegree.value) { reply = true; } {
					if (this.equave < otherdegree.value) { reply = false; } {
						if ((this.score_or_scala == \scala) && otherdegree.score_or_scala == \score) { reply = true; } {
							if ((this.score_or_scala == \score) && otherdegree.score_or_scala == \scala) { reply = false; }
						}
					}
				}
			}
		};
		^reply;
	}


	/*
	[method.printOn]
	description='''
	override of Object.printOn to make sure a Degree prints something useful when called with .postln
	'''
	[method.printOn.args]
	stream = "stream on which to print"
	[method.printOn.returns]
	what = "the updated stream"
	*/
	printOn {
		| stream |
		stream << "Degree(" << (value+1) << ", " << equave << ", \\" << score_or_scala << ", \\onebased)";
	}
}

/*
[examples]
what = '''
(
var d1 = Degree(5, 3, \score, \zerobased); // make a score degree
)
'''
*/