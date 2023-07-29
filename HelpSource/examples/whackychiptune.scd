(
// example recording on https://voca.ro/1be3vPzTFfqP
s.waitForBoot({
	var patterns = ();
	var drumpatterns = ();
	var gen;
	var alldrums;
	var tuning;
	var tuner;
	var root_frequency;
	var player;
	var seq;

	SynthDef(\triangle, {
		| out=0, freq=440, amp=0.1, pan=0.0, attack=0.01, release=0.5 |
		var sig = LFTri.ar(freq);
		var env = EnvGen.ar(Env.perc(attack, release), doneAction:Done.freeSelf);
		sig = Pan2.ar(env*amp*sig, pan);
		Out.ar(out, sig);
	}).add;

	SynthDef(\pulse, {
		| out=0, freq=440, amp=0.1, pan=0.0, width=0.5, attack=0.01, release=0.5 |
		var sig = LFPulse.ar(freq, width:width);
		var env = EnvGen.ar(Env.perc(attack, release), doneAction:Done.freeSelf);
		sig = Pan2.ar(env*amp*sig, pan);
		Out.ar(out, sig);
	}).add;

	SynthDef(\noise, {
		| out=0, amp=0.1, pan=0.0, attack=0.01, release=0.2 |
		var sig = WhiteNoise.ar();
		var env = EnvGen.ar(Env.perc(attack, release), doneAction:Done.freeSelf);
		sig = Pan2.ar(env*amp*sig, pan);
		Out.ar(out, sig);
	}).add;

	SynthDef(\blip, {
		| out=0, freq=250, amp=1.0, pan=0.0, attack=0.01, release=0.2|
		var rampdown = EnvGen.ar(Env([freq, 30], 0.04, curve:'exponential'), doneAction:Done.none);
		var sig = LFTri.ar(rampdown);
		var env = EnvGen.ar(Env.perc(attack, release), doneAction:Done.freeSelf);
		sig = Pan2.ar(env*amp*sig, pan);
		Out.ar(out, sig);
	}).add;

	s.sync;

	gen = {
		| pattern="x.x.",
		instrument=\noise,
		freq=250,
		repeat=1,
		division=8,
		amplitudeerrors=0,
		timingerrors=0|

		Pn(
			Pbind(
				\instrument, instrument,
				\freq, Prout({
					pattern.do({
						| character |
						if (character != $.) {
							freq.yield;
						} {
							Rest(0).yield;
						};
					});
				}),
				\amp, Prout({
					pattern.do({
						| character |
						if (character == $X) {
							(0.8 + (amplitudeerrors/2.0).rrand(amplitudeerrors/2.0.neg)).yield;
						} {
							if (character == $x) {
								(0.5 + (amplitudeerrors/2.0).rrand(amplitudeerrors/2.0.neg)).yield;
							} {
								0.yield;
							};
						};
					});
				}),
				\dur, Prout({
					var durations = pattern.collectAs({1.0/division}, Array);
					var totalduration = pattern.size * (1.0/division);
					var durations_with_errors = durations.collect({
						| dur |
						dur + (timingerrors/2.0).rrand(timingerrors/2.0.neg);
					}).normalizeSum * totalduration;
					durations_with_errors.do({ |el|
						el.yield;
					});
			})),
			repeat);
	};

	drumpatterns[\line1] = [
		["X.X...X...X..X..", \blip, 250, 8],
		["....X..x.x.xX..X", \noise, 0,  8],
	];

	alldrums = Ppar(drumpatterns[\line1].collect({
		|el|
		gen.(pattern: el[0],
			instrument: el[1],
			freq:el[2],
			repeat: 1,
			division: el[3],
			amplitudeerrors:0.1,
			timingerrors:0.02);
	}));

	patterns[\drums] = alldrums;

	tuning = [
		"! pipedum_10a.scl",
		"!",
		"2048/2025, 25/24 are homophonic intervals",
		" 10",
		"!",
		" 16/15  degree 2",
		" 9/8    degree 3",
		" 5/4    degree 4",
		" 4/3    degree 5",
		" 64/45  degree 6",
		" 3/2    degree 7",
		" 8/5    degree 8",
		" 16/9   degree 9",
		" 15/8   degree 10",
		" 2/1"
	].join("\n");

	tuner = RootFrequencyCalculator(scala_contents:tuning);
	root_frequency = tuner.get_root_frequency("6[4]", 432);

	seq = {
		| dict_mitola_to_instrument, root_frequency, repeats |
		var list_of_pattern = [];
		dict_mitola_to_instrument.keysValuesDo({
			| mitola, instr |
			list_of_pattern = list_of_pattern.add(Pn(mitola.as_pbind(instrument:instr, root_frequency:root_frequency), repeats));
		});
		Ppar(list_of_pattern);
	};


	patterns[\m1_2] = seq.(
		Dictionary[
			Mitola("1[4]_16 4 7 6 2_32 7 2 6 5 4 3 6", tuning) -> \pulse,
			Mitola("1[2]_8 5 1 6", tuning) -> \triangle],
		root_frequency,
		2);

	patterns[\m3_4] = seq.(
		Dictionary[
			Mitola("1[4]_32 8 9 3 4_16 5 6 7 5_32 4 3 6", tuning) -> \pulse,
			Mitola("1[2]_8 4 1 2", tuning) -> \triangle],
		root_frequency,
		2);

	patterns[\m5_6] = seq.(
		Dictionary[
			Mitola("1[4]_64@amp{0.4} 8 9 3 4_32 5 6 7 5_64 4 3 6 1[5]_64@amp{0.2} 8 9 3 4_32 5 6 7 5_64 4 3 6@amp{0.4}", tuning) -> \pulse,
			Mitola("1[3]_8 4 1 2", tuning) -> \triangle],
		root_frequency,
		2);

	patterns[\m7_8] = seq.(
		Dictionary[
			Mitola("1[4]_64@amp{0.5}@legato{0.2}@attack{0.05}@width{0.1} 8 9 3 1[5]_64 8 9 3 1[3]_64 8 9 3 1[6]_64@width{0.5} 8 9 3 1[5]_64 8 9 3 1[3]_64 8 9 3@width{0.1} 1[3]_64 8 9 3 1[4]_64 8 9 3@amp{0.1}@legato{1.0}@attack{0.01}@width{0.5}", tuning) -> \pulse,
			Mitola("1[4]_8 4 1_16 2 4 5", tuning) -> \triangle],
		root_frequency,
		2);

	fork {

		(thisThread.clock.elapsedBeats - thisThread.clock.beats).wait;

		//s.record("~/documents/music/supercollider/mitola/HelpSource/examples/example.wav");

		player = Ppar([
			Pseq([patterns[\m1_2], patterns[\m3_4], patterns[\m5_6], patterns[\m7_8]], 2),
			Pn(patterns[\drums], 16),
		]).play;

	}
});
)


//s.stopRecording;
