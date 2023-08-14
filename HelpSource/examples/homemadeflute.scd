(
s.waitForBoot({
	var pattern;
	var player;
	var tuning;
	var rootfreq;
	var track;
	var score;

	SynthDef(\flute, {
		| out=0, freq=440, amp=0.5, atk=0.085, sus=1.0, rel=0.59, pan=0, falloff=0.1875, fallofftime=0.25, breathnoise=1, vibratofreq=5.4, resonancefreq=400, brightnessdb=15, gate=1 |
		// speed of vibrato
		// make frequency "vibrato" a bit irregular by adding some noise to the vibrato frequency
		var vib = SinOsc.kr(vibratofreq + LFNoise1.kr(1).range(1.neg,1)).range(1.0, 1.03);
		// make vibrato by mulitplying frequency with vibrato signal
		var vibfreq = freq*vib;
		// add some immediate amplitude vibrato (could use an env to have it kick in after a while instead)
		// add some some small irregularities to the amp
		var vibamp = amp*(LFCub.kr(5.4).range(0.7, 1) + LFNoise1.kr(1).range(0, 0.1));
		// shape frequency over time: start slighty below the tone, and allow for a frequency falloff during release
		var freqenv = EnvGen.kr(Env.new([vibfreq*0.97, vibfreq, (vibfreq.cpsmidi - falloff).midicps], [0.01, fallofftime], [0, -10], 1), gate, doneAction:Done.none);
		// additive synthesis - define some harmonics
		var sig = DynKlang.ar(`[
			[freqenv*1, freqenv*2, freqenv*3, freqenv*4, freqenv*5, freqenv*6, freqenv*7, freqenv*8],
			[37.neg.dbamp, 41.neg.dbamp, 65.neg.dbamp, 69.neg.dbamp, 55.neg.dbamp, 70.neg.dbamp, 60.neg.dbamp, 70.neg.dbamp]*30,
			[pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi), pi.neg.rrand(pi)]
		]);
		// shape the amplitude of the sound over time: relatively short linear attack, exponential decay
		var env = EnvGen.ar(Env.asr(atk, sus, rel, [\linear, -10]), gate, doneAction:Done.freeSelf);
		// make some breath noise
		var noise = LPF.ar(0.007*breathnoise*LFClipNoise.ar(SampleRate.ir), LFNoise1.kr(0.5).exprange(2500,3500));
		// an initial "chiff" sound made with a resononant filter
		var chiff = Resonz.ar(sig, resonancefreq, bwr:0.01, mul:WhiteNoise.ar().range(0.5,1.0));
		// make sound a bit more wooden (??)
		sig = Resonz.ar(sig, resonancefreq, bwr:0.12, mul:5.0);
		sig = chiff + sig;
		// add noise, apply amplitude vibrato and envelope
		sig = vibamp*env*(sig+noise);
		// make higher frequencies sound brighter
		sig = HighShelf.ar(sig, freq*2.0, dbgain:freqenv.explin(300,2400,9,15));
		// position in stereo field
		sig = Pan2.ar(sig, pan);
		Out.ar(out, sig);
	}).add;

	SynthDef(\reverbfx, {
		| in, out=0, mix=0.4 |
		var sig = In.ar(in, 2);
		Out.ar(out, sig.blend(NHHall.ar(sig), mix));
	}).add;

	s.sync;

	if (~group_instr.notNil) {
		~group_instr.freeAll;
		~group_instr = nil;
	};
	~group_instr = Group();

	if (~group_fx.notNil) {
		~group_fx.freeAll;
		~group_fx = nil;
	};
	~group_fx = Group(~group_instr, \addAfter);

	CmdPeriod.doOnce({
		if (~group_instr.notNil){
			~group_instr.freeAll;
			~group_instr = nil;
		};
		if (~group_fx.notNil) {
			~group_fx.freeAll;
			~group_fx = nil;
		};
	});

	Synth(\reverbfx, [\mix, 0.2], ~group_fx);

	tuning = [
		"! homemadeflute.scl",
		"!",
		"tuning of an (imaginary) home made flute",
		" 12",
		"!",
		" 1/1",
		" 156.000 cents",  // deg 2
		" 240.000 cents",  // deg 3
		" 276.000 cents",  // deg 4
		" 312.000 cents",  // deg 5
		" 480.000 cents",  // deg 6
		" 678.000 cents",  // deg 7
		" 720.000 cents",  // deg 8
		" 777.000 cents",  // deg 9
		" 834.000 cents",  // deg 10
		" 960.000 cents",  // deg 11
		"2/1"].join("\n");

	rootfreq = MtlRootFrequencyCalculator(tuning).get_root_frequency("6[4]", 432 /* :-) */);

	// to keep the score somewhat editable, separate notes from properties
	track = [
		["2[5]_4",                 "@pan[0]@legato[0.97]@fallofftime[0.25]@falloff[0.1875]@amp{0.5}"],
		["6_2 5_4 6_2 8_4 9_2",    "@amp{1.0}"],
		["8_8*2/3 9 8 6_2",        "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
		["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
		["5_2 4_4 2_4.",           "@legato[0.8]"],
		["2_8",                    "@fallofftime[0.3]@falloff[0.2]"],
		["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
		["5_2 4_8*2/3 6 4 2_2",    "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
		["2_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]@amp{0.5}"],
		["6_2 5_4 6_2 8_4 9_2",    "@amp{1.0}"],
		["8_8*2/3 9 8 6_2",        "@legato[0.8]@fallofftime[1.0]@falloff[1]@amp{0.5}"],
		["4_4",                    "@legato[1.0]@fallofftime[0.25]@falloff[0.1875]"],
		["5_2 4_4 2_2",            "@legato[0.5]"],
		["10[4]_4",                "@legato[1.0]"],
		["2[5]_2.",                "@fallofftime[0.7]@falloff[0.5]"],
	];

	score = track.collect({
		| line |
		line[0].stripWhiteSpace ++ line[1].stripWhiteSpace
	}).join(" ");

	pattern = MtlMitola(score, scala_contents:tuning).as_pbind(\flute, root_frequency:rootfreq);
	player = pattern.play(TempoClock.new(100/60));

});
)
