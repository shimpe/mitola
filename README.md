# mitola 
**MI**cro **TO**nal **LA**nguage for supercollider; microtonal counterpart to Panola
 
Mitola provides notation and utilities to write microtonal music in supercollider using a text based notation. Mitola is modeled after its sister quark Panola, which provides text based notation for "conventional" music.
Mitola shares many ideas and syntax with Panola, but differs in some aspects where I thought Panola could be improved, or where things cannot be reused as-is.

Mitola depends on another quark called scparco which is a library to generate parsers in supercollider. Both need to be installed:

```smalltalk
(
Quarks.install("https://github.com/shimpe/scparco"); // parser combinator library
Quarks.install("https://github.com/shimpe/mitola"); // mitola implementation
)
```

A Mitola score consists of a list of notes and/or chords. Mitola does not use note names. Instead it uses integer scale degrees 1,2,...N, where N is determined by the tuning in use. Suppose you have a 12EDO tuning, then N can be at most 12.

```smalltalk
(
var valid_notes_12_edo = "1 2 3 4 5 6 7 8 9 10 11 12"; // 1-based degree names
)
```

Degrees can be modified with modifiers (in conventional music these would be accidentals, like sharps and flats). Modifiers in Mitola can be specified in two ways. First way: absolute modification by a number of cents. E.g. 1{+100.0} modifies degree 1 by adding 100.0 cents. Note that you must include a decimal point for the number to be interpreted as cents. Similarly 4{-53.0} would lower the pitch represented by degree 4 by 53.0 cents. 

The second way is to specify a ratio. E.g. 5{+2/3} will modify the pitch so that it sounds 2/3 between degree 5 and degree 6. Ratios with an abs value larger than 1 are reduced: 5{-4/3} is interpreted as 4{-1/3}. This is important to understand the behavior in case of tunings with different gaps between the different degrees. An alternative way to specify ratios is using prime vector notation. In prime vector notation, a ratio is defined as a multiplication of prime factors. The exponents of the prime factors are between | and >. So this is a valid note: 3{+|1/12>}. It will raise degree 3 with 2^(1/12).

```smalltalk
(
var modified_notes = "7{+50.0} 4{-34.0} 6{+1/4} 10{-3/4} 3{+|1/2 -3/2 2/7>}";
)
```

In addition to modifiers, degrees also have an equivalence interval (aka equave). In traditional notation, this would be called an octave. The equivalence interval is indicated using square brackets. E.g. 1[4] is degree one in the fourth equave. If you do not specify an equave for a degree, the previous one is reused.

```smalltalk
(
var same_note_in_different_equaves = "3[2] 3[4] 3[5]";
)
```

To indicate rhythm, a note can be decorated with an underscore (_) followed by a number, e.g. 1_4 indicates degree one as a quarter note, whereas 1_8 is the first degree as an eighth note. If you do not specify a duration value, the last specified one is reused.

```smalltalk
(
var quarter_note_followed_by_two_eighths = "1_4 5_8 5";
)
```

The duration value optionally can be extended with one or more dots. Similar to traditional notation, 1_4 lasts one quarter note; 1_4. lasts 1+0.5 quarter notes (= 3 eighths) and 1_4.. lasts 1+0.5+0.25 quarter notes (=7 sixteenths).

```smalltalk
(
var two_dotted_quarter_note_followed_by_a_regular_eighth = "6_4. 7 9_8";
)
```

In addition to the dots, one can also (optionally) specificy a multiplier and a divider. These can be used to define tuplets. E.g. "1[4]_8*2/3 4 7" consists of 3 degrees in the fourth equave forming a triplet of eighth notes.

```smalltalk
(
var triplet_of_eighths = "1[4]_8*2/3 4 7";
)
```

Notes can also optionally be decorated with properties (anything you like really). These properties by default are included in the supercollider patterns that are derived from the score, where they can be used to drive synth arguments or control behavior of the patterns. The properties can be animated between notes (see example code below).

```smalltalk
(
var animated_prop_crescendo = "1[4]@amp{0.2} 2 3 4 5 6 7@amp{0.9}";
var static_prop_two_staccato_one_legato = "1[4]@legato[0.1] 5 1[5]@legato[1.0];
)
```

Notes can be grouped in angular brackets < > to make chords. Limitation: the properties of the first note iin the chord are used for the complete chord. "< 1[4]_8 4 1{+25.0}[5]>" is a chord consisting of degrees 1[4] 4[4] and 1{+25.0}[5].

```smalltalk
(
var chord = "<1[4]_2 4 7>";
)
```

Lastly, notes can be put between repeat brackets and the number of repeats can be indicated, e.g.
|: 1[4]_8 2 3 :|*5 repeats notes 1[4]_8 2 3 five times.

```smalltalk
(
var repeats = "|: 1[4]_8 2 3 :| * 5";
)
```

To convert Mitola scores to frequencies it is necessary to know in which tuning the score has to be interpreted. Tuning is indicated in the form of a scala definition and a root frequency. In order to pin a given degree in your scala definition to a fixed frequency (e.g. ensure that A4 is 440Hz in a 12EDO tuning), a suitable root_frequency can be calculated using the RootFrequencyCalculator class.


# Examples
```smalltalk
// Let's start with the "Hello world" of Mitola: a simple scale.
// In microtonal music, we need to define the scale degrees first. I'm here inlining the scala contents,
// but you can also load them from file by passing a scala_filename instead scala_contents.
(
s.waitForBoot({
	var tuning = [
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
	var score = Mitola("1[4]_16@amp[0.6] 2 3 4 5 6 7 8 9 10 11 12 1[5]", scala_contents: tuning);
	// find out which root frequency to use to get degree 10 in octave 4 (A4) to map to 440Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("10[4]", 440);
	var player = score.as_pbind(root_frequency:root_freq).play; // listen to the score with the default instrument
});
)
```

Another example of playing two lines simultaneously in 7EDO tuning 
> (nothing prevents you from writing the second line in a different tuning of course...)

```smalltalk
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp[0.6] 2 3_32 4 5_16 6 7 1[5]_4", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.6] 5 1 5 1 5 1 5 1", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var player = Ppar([
		score.as_pbind(root_frequency:root_freq),
		score2.as_pbind(root_frequency:root_freq)
	]).play; // listen to score and score2 simultaneously with the default instrument
});
)
```

If you define synths in supercollider, you can use them instead of the default instrument to play

```smalltalk
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp[0.6] 2 3_32 4 5_16 6 7 1[5]_4", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.6] 5 1 5 1 5 1 5 1", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var pattern = Ppar([
		score.as_pbind(\sawSynth, root_frequency:root_freq),
		score2.as_pbind(\sawSynth, root_frequency:root_freq)
	]);
	var player;

	SynthDef(\sawSynth, { arg freq = 440, amp = 0.1, att = 0.1, rel = 2, lofreq = 1000, hifreq = 3000, pan = 0;
		var env, snd;
		env = Env.perc(
			attackTime: att,
			releaseTime: rel,
			level: amp
		).kr(doneAction: 2);
		snd = Saw.ar(freq: freq * [0.99, 1, 1.001, 1.008], mul: env);
		snd = LPF.ar(
			in: snd,
			freq: LFNoise2.kr(1).range(lofreq, hifreq)
		);
		snd = Mix.ar(snd);
		snd = Pan2.ar(snd, pan);
		Out.ar(0, snd);
		// Basic saw synth
		//By Bruno Ruviaro
		//http://sccode.org/1-54H
	}).add;

	s.sync;

	player = pattern.play;
});
)
```

Using properties, we can now even change synth parameters. Wrapping property values in {} animates them in the notes between, so here we make a crescendo in the melody between amp=0.2 and amp=0.6. To understand better what happens: "amp" appears in the score as a property name, and in the SynthDef as an argument. This shared name is how the score is linked to the SynthDef.

If you wrap the property values in [], the values remain static, so in the accompaniment, the amplitude stays at 0.3, until the last note where it becomes 0.6.
```smalltalk
(
s.waitForBoot({
	var tuning = [
		"! 7edo.scl",
		"!",
		"7 edo",
		" 7",
		"!",
		" | 1/7 >",
		" | 2/7 >",
		" | 3/7 >",
		" | 4/7 >",
		" | 5/7 >",
		" | 6/7 >",
		" 2/1"
	].join("\n");
	var score = Mitola("1[4]_16@amp{0.2} 2 3_32 4 5_16 6 7 1[5]_4@amp{0.6}", scala_contents: tuning);
	var score2 = Mitola("1[3]_16@amp[0.3] 5 1 5 1 5 1 5 1@amp[0.6]", scala_contents:tuning);
	// find out which root frequency to use to get degree 4 in octave 4 to map to 432Hz.
	var r = RootFrequencyCalculator(tuning);
	var root_freq = r.get_root_frequency("4[4]", 432);
	var pattern = Ppar([
		score.as_pbind(\sawSynth, root_frequency:root_freq),
		score2.as_pbind(\sawSynth, root_frequency:root_freq)
	]);
	var player;

	SynthDef(\sawSynth, { arg freq = 440, amp = 0.1, att = 0.1, rel = 2, lofreq = 1000, hifreq = 3000, pan = 0;
		var env, snd;
		env = Env.perc(
			attackTime: att,
			releaseTime: rel,
			level: amp
		).kr(doneAction: 2);
		snd = Saw.ar(freq: freq * [0.99, 1, 1.001, 1.008], mul: env);
		snd = LPF.ar(
			in: snd,
			freq: LFNoise2.kr(1).range(lofreq, hifreq)
		);
		snd = Mix.ar(snd);
		snd = Pan2.ar(snd, pan);
		Out.ar(0, snd);
		// Basic saw synth
		//By Bruno Ruviaro
		//http://sccode.org/1-54H
	}).add;

	s.sync;

	player = pattern.play;
});
)
```
