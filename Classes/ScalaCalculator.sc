/*
[general]
title = "ScalaCalculator"
summary = "a calculator for interpreting mitola degrees in scala definitions"
categories = "Microtonal utils"
related = "Classes/ScalaParser, Classes/Mitola"
description = '''
ScalaCalculator implements calculations required to translate mitola degrees and pitch modifiers into frequencies
'''
*/
ScalaCalculator {
	/*
	[classmethod.prime_factors]
	description='''
	A table of prime factors, used to interpret scala's prime vector notation for ratios
	'''
	[classmethod.prime_factors.returns]
	what="a list of primes"
	*/
	classvar prime_factors;

	/*
	[method.scala_parse_result]
	description='''
	a variable to store the result of parsing the scala definition
	'''
	[method.scala_parse_result.returns]
	what="a list of primes"
	*/
	var <>scala_parse_result;
	/*
	[method.previous_repeat_interval]
	description='''
	a variable to remember the previously used note repeat interval so it can be reused if set to \previous
	repeat interval corresponds to what in conventional music notation would be called an octave
	'''
	[method.previous_repeat_interval.returns]
	what= "a repeat interval value"
	*/
	var <>previous_repeat_interval;

	/*
	[classmethod.new]
	description = "New creates a new ScalaCalculator"
	[classmethod.new.returns]
	what = "a new ScalaCalculator"
	*/
	*new {
		^super.new.init();
	}

	/*
	[classmethod.initClass]
	description = "initializes the prime factor table (which is shared by all ScalaCalculator instances)"
	[classmethod.initClass.returns]
	what = "the initialized table of primes"
	*/
	*initClass {
		prime_factors = [ // 1000 prime factors ought to be enough for everyone :-)
			2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,
			109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,223,227,
			229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,337,347,349,
			353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,
			479,487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,
			617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,
			757,761,769,773,787,797,809,811,821,823,827,829,839,853,857,859,863,877,881,883,887,
			907,911,919,929,937,941,947,953,967,971,977,983,991,997,1009,1013,1019,1021,1031,1033,
			1039,1049,1051,1061,1063,1069,1087,1091,1093,1097,1103,1109,1117,1123,1129,1151,1153,
			1163,1171,1181,1187,1193,1201,1213,1217,1223,1229,1231,1237,1249,1259,1277,1279,1283,
			1289,1291,1297,1301,1303,1307,1319,1321,1327,1361,1367,1373,1381,1399,1409,1423,1427,
			1429,1433,1439,1447,1451,1453,1459,1471,1481,1483,1487,1489,1493,1499,1511,1523,1531,
			1543,1549,1553,1559,1567,1571,1579,1583,1597,1601,1607,1609,1613,1619,1621,1627,1637,
			1657,1663,1667,1669,1693,1697,1699,1709,1721,1723,1733,1741,1747,1753,1759,1777,1783,
			1787,1789,1801,1811,1823,1831,1847,1861,1867,1871,1873,1877,1879,1889,1901,1907,1913,
			1931,1933,1949,1951,1973,1979,1987,1993,1997,1999,2003,2011,2017,2027,2029,2039,2053,
			2063,2069,2081,2083,2087,2089,2099,2111,2113,2129,2131,2137,2141,2143,2153,2161,2179,
			2203,2207,2213,2221,2237,2239,2243,2251,2267,2269,2273,2281,2287,2293,2297,2309,2311,
			2333,2339,2341,2347,2351,2357,2371,2377,2381,2383,2389,2393,2399,2411,2417,2423,2437,
			2441,2447,2459,2467,2473,2477,2503,2521,2531,2539,2543,2549,2551,2557,2579,2591,2593,
			2609,2617,2621,2633,2647,2657,2659,2663,2671,2677,2683,2687,2689,2693,2699,2707,2711,
			2713,2719,2729,2731,2741,2749,2753,2767,2777,2789,2791,2797,2801,2803,2819,2833,2837,
			2843,2851,2857,2861,2879,2887,2897,2903,2909,2917,2927,2939,2953,2957,2963,2969,2971,
			2999,3001,3011,3019,3023,3037,3041,3049,3061,3067,3079,3083,3089,3109,3119,3121,3137,
			3163,3167,3169,3181,3187,3191,3203,3209,3217,3221,3229,3251,3253,3257,3259,3271,3299,
			3301,3307,3313,3319,3323,3329,3331,3343,3347,3359,3361,3371,3373,3389,3391,3407,3413,
			3433,3449,3457,3461,3463,3467,3469,3491,3499,3511,3517,3527,3529,3533,3539,3541,3547,
			3557,3559,3571,3581,3583,3593,3607,3613,3617,3623,3631,3637,3643,3659,3671,3673,3677,
			3691,3697,3701,3709,3719,3727,3733,3739,3761,3767,3769,3779,3793,3797,3803,3821,3823,
			3833,3847,3851,3853,3863,3877,3881,3889,3907,3911,3917,3919,3923,3929,3931,3943,3947,
			3967,3989,4001,4003,4007,4013,4019,4021,4027,4049,4051,4057,4073,4079,4091,4093,4099,
			4111,4127,4129,4133,4139,4153,4157,4159,4177,4201,4211,4217,4219,4229,4231,4241,4243,
			4253,4259,4261,4271,4273,4283,4289,4297,4327,4337,4339,4349,4357,4363,4373,4391,4397,
			4409,4421,4423,4441,4447,4451,4457,4463,4481,4483,4493,4507,4513,4517,4519,4523,4547,
			4549,4561,4567,4583,4591,4597,4603,4621,4637,4639,4643,4649,4651,4657,4663,4673,4679,
			4691,4703,4721,4723,4729,4733,4751,4759,4783,4787,4789,4793,4799,4801,4813,4817,4831,
			4861,4871,4877,4889,4903,4909,4919,4931,4933,4937,4943,4951,4957,4967,4969,4973,4987,
			4993,4999,5003,5009,5011,5021,5023,5039,5051,5059,5077,5081,5087,5099,5101,5107,5113,
			5119,5147,5153,5167,5171,5179,5189,5197,5209,5227,5231,5233,5237,5261,5273,5279,5281,
			5297,5303,5309,5323,5333,5347,5351,5381,5387,5393,5399,5407,5413,5417,5419,5431,5437,
			5441,5443,5449,5471,5477,5479,5483,5501,5503,5507,5519,5521,5527,5531,5557,5563,5569,
			5573,5581,5591,5623,5639,5641,5647,5651,5653,5657,5659,5669,5683,5689,5693,5701,5711,
			5717,5737,5741,5743,5749,5779,5783,5791,5801,5807,5813,5821,5827,5839,5843,5849,5851,
			5857,5861,5867,5869,5879,5881,5897,5903,5923,5927,5939,5953,5981,5987,6007,6011,6029,
			6037,6043,6047,6053,6067,6073,6079,6089,6091,6101,6113,6121,6131,6133,6143,6151,6163,
			6173,6197,6199,6203,6211,6217,6221,6229,6247,6257,6263,6269,6271,6277,6287,6299,6301,
			6311,6317,6323,6329,6337,6343,6353,6359,6361,6367,6373,6379,6389,6397,6421,6427,6449,
			6451,6469,6473,6481,6491,6521,6529,6547,6551,6553,6563,6569,6571,6577,6581,6599,6607,
			6619,6637,6653,6659,6661,6673,6679,6689,6691,6701,6703,6709,6719,6733,6737,6761,6763,
			6779,6781,6791,6793,6803,6823,6827,6829,6833,6841,6857,6863,6869,6871,6883,6899,6907,
			6911,6917,6947,6949,6959,6961,6967,6971,6977,6983,6991,6997,7001,7013,7019,7027,7039,
			7043,7057,7069,7079,7103,7109,7121,7127,7129,7151,7159,7177,7187,7193,7207,7211,7213,
			7219,7229,7237,7243,7247,7253,7283,7297,7307,7309,7321,7331,7333,7349,7351,7369,7393,
			7411,7417,7433,7451,7457,7459,7477,7481,7487,7489,7499,7507,7517,7523,7529,7537,7541,
			7547,7549,7559,7561,7573,7577,7583,7589,7591,7603,7607,7621,7639,7643,7649,7669,7673,
			7681,7687,7691,7699,7703,7717,7723,7727,7741,7753,7757,7759,7789,7793,7817,7823,7829,
			7841,7853,7867,7873,7877,7879,7883,7901,7907,7919];
	}

	/*
	[method.init]
	description = "initializes the ScalaCalculator class"
	[method.init.returns]
	what = "an initialized ScalaCalculator"
	*/
	init {
		this.previous_repeat_interval = 4; // a default value
	}

	/*
	[method.parse]
	description = "parses a string containing a scala definition"
	[mathod.parse.args]
	scala_contents = "a string containing a valid scala definition"
	[method.parse.returns]
	what = "the parse tree representing the information in the scala string"
	*/
	parse {
		| scala_contents |
		this.scala_parse_result = ScalaParser.parse(scala_contents);
		if (this.scala_parse_result.isNil) {
			"Failed to parse scala contents".postln;
		};
		^this.scala_parse_result;
	}

	/*
	[method.parseFile]
	description = "parses a file containing a scala definition"
	[method.parseFile.args]
	filename = "the file name containing a scala definition to be parsed"
	[method.parseFile.returns]
	what = "the parse tree representing the information in the scala file"
	*/
	parseFile {
		| filename |
		var contents = FileReader.read(filename);
		if (contents.notNil) {
			^this.parse(contents.join("\n"));
		} {
			("Error! Couldn't open file '" ++ filename ++ "' for reading.").postln;
		};
		^this.scala_parse_result;
	}

	/*
	[method.note_to_freq]
	description = "parses a file containing a scala definition"
	[method.note_to_freq.args]
	mitola_note_string = "a mitola string containing a single note (degree) - may optionally be decorated with modifiers, repeatinterval, duration, properties"
	root_frequency = "the base frequency, i.e. the frequency for degree 1[0] in the given scala definition. For calculating such frequency, you can use the RootFrequenceCalculator class."
	[method.note_to_freq.returns]
	what = "the frequency of the given mitola degree, given the current scala definition and root frequency"
	*/
	note_to_freq {
		| mitola_note_string, root_frequency=nil |
		var state = MitolaParser.pr_noteAndModAndOctAndDurAndProp.run(mitola_note_string);
		if (state.isError) {
			state.prettyprint;
			^0;
		};
		^this.pr_note_pitch_parse_tree_to_freq(state.result[\info][\note][\pitch], root_frequency);
	}

	/*
	[method.no_of_degrees]
	description = "returns the number of degrees that are defined in the scala definition"
	[method.no_of_degrees.returns]
	what = "an integer"
	*/
	no_of_degrees {
		^this.scala_parse_result.[\degrees].size;
	}

	/*
	[method.max_degree]
	description = "returns the maximum degree number for the given scala definition. Note that this method replies a 0-based answer, whereas the mitola degrees are specified using 1-based degree numbers. So if max_degree return 3, the valid mitola degrees are 1,2,3 and 4."
	[method.max_degree.returns]
	what = "an integer"
	*/
	max_degree {
		^(this.scala_parse_result.[\degrees].size - 1);
	}

	/*
	[classmethod.pr_ratio_to_cents]
	description = "converts between ratio and cents representation; cents make it easy to calculate in pitch space, whereas ratios are easier to use in frequency space"
	[classmethod.pr_ratio_to_cents.args]
	ratio = "the ratio to convert (a float)"
	[classmethod.pr_ratio_to_cents.returns]
	what = "a Float"
	*/
	*pr_ratio_to_cents {
		| ratio |
		var cents = 1200*(ratio.log2);
		^cents;
	}

	/*
	[classmethod.pr_cents_to_ratio]
	description = "converts between ratio and cents representation; cents make it easy to calculate in pitch space, whereas ratios are easier to use in frequency space"
	[classmethod.pr_cents_to_ratio.args]
	cents = "the number of cents to convert to a ratio (a float)"
	[classmethod.pr_cents_to_ratio.returns]
	what = "a Float"
	*/
	*pr_cents_to_ratio {
		| cents |
		^2.pow(cents/1200);
	}

	/*
	[method.pr_degree_to_cents]
	description='''
	pr_degree_to_cents is an internal method that converts a mitola degree (integer) into a number of cents, given the current scala definition - it does not care about pitch modifiers
	'''
	[method.pr_degree_to_cents.args]
	degree = "an integer representing a 0-based mitola degree"
	note_repeat_interval = "an integer representing the note repeat interval (equivalent of 'octave' in traditional notation)"
	[method.pr_degree_to_cents.returns]
	what= "a Float"
	*/
	pr_degree_to_cents {
		| degree, note_repeat_interval |
		var scala_info = this.scala_parse_result[\degrees][degree]; // ('kind': 'cents', 'what': 'pitch', 'numerator': 0,'denominator': 1)
		var cents = 0;
		var scale_repeat_interval_factor;

		if (scala_info[\kind] == \cents) {
			cents = scala_info[\numerator]
		} {
			if (scala_info[\kind] == \ratio) {
				cents = ScalaCalculator.pr_ratio_to_cents(scala_info[\numerator] / scala_info[\denominator]);
			} {
				if (scala_info[\kind] == \primevector) {
					var overall_num = 1;
					var overall_den = 1;
					scala_info[\exponents].do({
						| factor, idx |
						if (factor[\kind] == \ratio) {
							var num = factor[\numerator];
							var den  = factor[\denominator];
							if (den == 0) {
								("Internal error in pr_degree_to_cents. num = " ++ num ++ "; den = 0").postln;
								cents = 0;
							} {
								if ((num * den) < 0) {
									overall_den = overall_den * prime_factors[idx].pow((num/den).abs);
								}{
									overall_num = overall_num * prime_factors[idx].pow(num/den);
								}
							}
						} {
							("Internal error in pr_degree_to_cents. factor[\kind] =" + factor[\kind]).postln;
							cents = 0;
						}
					});
					cents = ScalaCalculator.pr_ratio_to_cents(overall_num / overall_den);
				} {
					("Error. pr_degree_to_freq called with unknown scala_info[\kind]" + scala_info[\kind]).postln;
					cents = 0;
				}
			}
		};
		scale_repeat_interval_factor = this.scala_parse_result[\repeatinterval][\numerator] / this.scala_parse_result[\repeatinterval][\denominator];
		cents = cents + (note_repeat_interval*ScalaCalculator.pr_ratio_to_cents(scale_repeat_interval_factor));
		^cents;
	}


	/*
	[method.pr_previous_degree]
	description='''
	an internal method that converts a given degree and repeat_interval to the previous degree and repeat_interval in the given scala definition, taking care of wrapping.
	'''
	[method.pr_previous_degree.args]
	degree = "an integer representing a 0-based mitola degree"
	repeat_interval = "an integer representing the note repeat interval (equivalent of 'octave' in traditional notation)"
	[method.pr_previous_degree.returns]
	what= "a Float"
	*/
	pr_previous_degree {
		| degree, repeat_interval |
		var next_degree = degree - 1;
		if (next_degree < 0) { // wrap
			next_degree = this.max_degree;
			repeat_interval = repeat_interval - 1;
		};
		^(\degree: next_degree, \repeatinterval: repeat_interval);
	}


	/*
	[method.pr_next_degree]
	description='''
	an internal method that converts a given degree and repeat_interval to the next degree and repeat_interval in the given scala definition, taking care of wrapping.
	'''
	[method.pr_next_degree.args]
	degree = "an integer representing a 0-based mitola degree"
	repeat_interval = "an integer representing the note repeat interval (equivalent of 'octave' in traditional notation)"
	[method.pr_next_degree.returns]
	what= "a Float"
	*/
	pr_next_degree {
		| degree, repeat_interval |
		var next_degree = degree + 1;
		if (next_degree > this.max_degree) { // wrap
			next_degree = 0;
			repeat_interval = repeat_interval + 1;
		};
		^(\degree: next_degree, \repeatinterval: repeat_interval);
	}

	/*
	[method.pr_info_note_pitch_modifier_parse_tree_to_cents]
	description='''
	function that calculates the influence of the pitch modifier on the frequency. Modifiers specified in cents are applied as absolute modifiers. Modifiers specified as ratios are interpreted as relative degree modifiers, e.g. {+3/2} is interpreted as 2/2+1/2, i.e. raising to the next degree (+2/2=+1) and then raising to halfway between the 2nd next and 3rd next degree (+1/2). This distinction is important to understand the behavior in case of scales with different gaps between the pitches.
	'''
	[method.pr_info_note_pitch_modifier_parse_tree_to_cents.args]
	degree = "an integer representing a 0-based mitola degree"
	repeat_interval = "an integer representing the note repeat interval (equivalent of 'octave' in traditional notation)"
	info_note_pitch_modifier = "parse tree of the note modifier part of the mitola specification"
	[method.pr_info_note_pitch_modifier_parse_tree_to_cents.returns]
	what= "a Float"
	*/
	pr_info_note_pitch_modifier_parse_tree_to_cents {
		| degree, repeat_interval, info_note_pitch_modifier |
		/* Example
		(
		'kind': 'cents',
		'what': 'notemodifier',
		'value': (
		     'numerator': 50.0,
		     'kind': 'cents',
		     'what': 'pitch',
		     'denominator': 1 ),
		'direction': 'raise' )
		*/
		var cents = 0;
		var kind;
		if (info_note_pitch_modifier.class != Event) {
			"Error. pr_info_note_pitch_modifier_parse_tree_to_cents called with wrong argument types".postln;
			cents = 0;
		};
		if (info_note_pitch_modifier[\direction] == \none) {
			// no modifier (natural)
			cents = 0;
		} {
			kind = info_note_pitch_modifier[\kind];
			if (kind == \cents) {
				// value specified in cents is absolute modification
				cents = info_note_pitch_modifier[\value][\numerator];
				if (info_note_pitch_modifier[\direction] == \lower) {
					cents = cents.neg;
				};
			} {
				if (kind == \ratio) {
					// value specified as ratio is relative to previous/next degree
					if (info_note_pitch_modifier[\direction] == \lower) {
						var previous_cents, diff, current_cents;
						var ratio = info_note_pitch_modifier[\value][\numerator]/info_note_pitch_modifier[\value][\denominator];
						var previous_degree = this.pr_previous_degree(degree, repeat_interval);
						var cents_compensation = 0;
						while({ratio > 1}) {
							var current_cents = this.pr_degree_to_cents(degree, repeat_interval);
							var previous_cents = this.pr_degree_to_cents(previous_degree[\degree], previous_degree[\repeatinterval]);
							cents_compensation = cents_compensation + (previous_cents - current_cents);
							ratio = ratio - 1;

							degree = previous_degree[\degree];
							repeat_interval = previous_degree[\repeatinterval];
							previous_degree = this.pr_previous_degree(degree, repeat_interval);
						};
						current_cents = this.pr_degree_to_cents(degree, repeat_interval);
						previous_cents = this.pr_degree_to_cents(previous_degree[\degree], previous_degree[\repeatinterval]);
						diff = current_cents - previous_cents;
						cents = cents_compensation + (diff * ratio).neg;
					} {
						if (info_note_pitch_modifier[\direction] == \raise) {
							var next_cents, diff, current_cents;
							var next_degree = this.pr_next_degree(degree, repeat_interval);
							var ratio = info_note_pitch_modifier[\value][\numerator]/info_note_pitch_modifier[\value][\denominator];
							var cents_compensation = 0;
							this.pr_degree_to_cents(degree, repeat_interval);
							while({ratio > 1}) {
								var current_cents = this.pr_degree_to_cents(degree, repeat_interval);
								var next_cents = this.pr_degree_to_cents(next_degree[\degree], next_degree[\repeatinterval]);
								cents_compensation = cents_compensation + (next_cents - current_cents);
								ratio = ratio - 1;
								degree = next_degree[\degree];
								repeat_interval = next_degree[\repeatinterval];
								next_degree = this.pr_next_degree(degree, repeat_interval);
							};
							current_cents = this.pr_degree_to_cents(degree, repeat_interval);
							next_cents = this.pr_degree_to_cents(next_degree[\degree], next_degree[\repeatinterval]);
							diff = next_cents - current_cents;
							cents = cents_compensation + (diff * ratio);
						}
					}
				} {
					if (kind == \primevector) {
						var overall_num = 1;
						var overall_den = 1;
						info_note_pitch_modifier[\value].do({
							| factor, idx |
							if (factor[\kind] == \ratio) {
								var num = factor[\numerator];
								var den  = factor[\denominator];
								if (den == 0) {
									("Internal error in pr_info_note_pitch_modifier_parse_tree_to_cents. num = " ++ num ++ "; den = 0").postln;
									cents = 0;
								} {
									if ((num * den) < 0) {
										overall_den = overall_den * prime_factors[idx].pow((num/den).abs);
									}{
										overall_num = overall_num * prime_factors[idx].pow(num/den);
									}
								}
							} {
								("Internal error in pr_info_note_pitch_modifier_parse_tree_to_cents. factor[\kind] =" + factor[\kind]).postln;
								cents = 0;
							}
						});
						if (info_note_pitch_modifier[\direction] == \lower) {
							var previous_cents, diff, current_cents;
							var previous_degree = this.pr_previous_degree(degree, repeat_interval);
							var ratio = overall_num/overall_den;
							var cents_compensation = 0;
							while({ratio > 1}) {
								var current_cents = this.pr_degree_to_cents(degree, repeat_interval);
								var previous_cents = this.pre_degree_to_cents(previous_degree[\degree], previous_degree[\repeatinterval]);
								cents_compensation = cents_compensation + (previous_cents - current_cents);
								ratio = ratio - 1;
								degree = previous_degree[\degree];
								repeat_interval = previous_degree[\repeatinterval];
								previous_degree = this.pr_previous_degree(degree, repeat_interval);
							};
							current_cents = this.pr_degree_to_cents(degree, repeat_interval);
							previous_cents = this.pr_degree_to_cents(previous_degree[\degree], previous_degree[\repeatinterval]);
							diff = current_cents - previous_cents;
							cents = cents_compensation + (diff * ratio).neg;
						} {
							if (info_note_pitch_modifier[\direction] == \raise) {
								var next_cents, diff, current_cents;
								var next_degree = this.pr_next_degree(degree, repeat_interval);
								var ratio = overall_num/overall_den;
								var cents_compensation = 0;
								while({ratio > 1}) {
									var current_cents = this.pr_degree_to_cents(degree, repeat_interval);
									var next_cents = this.pr_degree_to_cents(next_degree[\degree], next_degree[\repeatinterval]);
									cents_compensation = cents_compensation + (next_cents - current_cents);
									ratio = ratio - 1;
									degree = next_degree[\degree];
									repeat_interval = next_degree[\repeatinterval];
									next_degree = this.pr_next_degree(degree, repeat_interval);
								};
								current_cents = this.pr_degree_to_cents(degree, repeat_interval);
								next_cents = this.pr_degree_to_cents(next_degree[\degree], next_degree[\repeatinterval]);
								diff = next_cents - current_cents;
								cents = cents_compensation + (diff * ratio);
							}
						}
					} {
						("Internal error in pr_info_note_pitch_modifier_parse_tree_to_cents. Unknown kind:"+kind).postln;
						cents =0;
					}
				}
			}
		}
		^cents;
	}


	/*
	[method.pr_note_pitch_parse_tree_to_freq]
	description='''
	takes a part of a mitola parse tree representing a single note's pitch information and converts it to a frequency value, given the current scala definition and a root frequency.
	'''
	[method.pr_note_pitch_parse_tree_to_freq.args]
	info_note_pitch_parse_tree = "parse tree"
	root_frequency = "root frequency"
	[method.pr_note_pitch_parse_tree_to_freq.returns]
	what= "a Float"
	*/
	pr_note_pitch_parse_tree_to_freq {
		| info_note_pitch_parse_tree, root_frequency=nil |
		/* example:
		(
		'what': 'note',
		'repeatinterval': 'previous',
		'notename': 4,
		'notemodifier': (
		'kind': 'cents',
		'what': 'notemodifier',
		'value': (
		'numerator': 70.0,
		'kind': 'cents',
		'what':
		'pitch',
		'denominator': 1 ),
		'direction': 'lower' ))
		*/
		var degree;
		var repeatinterval;
		var cents, ratio;
		if (this.scala_parse_result.isNil) {
			"Error. Need to parse scala definition before conversion.".postln;
			^0;
		};
		if (root_frequency.isNil) {
			"Error. Need to pass the root frequency, i.e. the frequency of note 1[0] (first degree in repeat interval 0).".postln;
			^0;
		};
		if (info_note_pitch_parse_tree[\what] == \rest){
			^0;
		};
		degree = info_note_pitch_parse_tree[\notename];
		if (degree > this.max_degree) {
			("Warning: degree" + (degree+1) + "is higher than number of degrees (" ++ this.no_of_degrees ++ ") defined in scala definition. Degree will be clipped to" + this.max_degree).postln;
			degree = this.max_degree;
		};
		repeatinterval = info_note_pitch_parse_tree[\repeatinterval];
		if (repeatinterval == \previous) {
			repeatinterval = this.previous_repeat_interval; // reuse previous value
		} {
			this.previous_repeat_interval = repeatinterval; // update with current value
		};
		cents = this.pr_degree_to_cents(degree, repeatinterval) + this.pr_info_note_pitch_modifier_parse_tree_to_cents(degree, repeatinterval, info_note_pitch_parse_tree[\notemodifier]);
		ratio = ScalaCalculator.pr_cents_to_ratio(cents);
		^(root_frequency * ratio);
	}
}