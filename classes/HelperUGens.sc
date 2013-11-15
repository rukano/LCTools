// These Ugens are wrappers for UGens combinations I often use

Dusty {
	*ar { |freq=40, density=40|
		^(Impulse.ar(freq) + Dust.ar(density))
	}
}

SynthPad {
	*ar { |ugen, freq=50, ffreq=150, res=0.1, detune=0.01, rate=1|
		var num = freq.size;
		if( num == 0 ) { num = 1 };
		^(Splay.ar(DFM1.ar(ugen.ar(freq * { LFNoise2.kr(rate, detune, 1) }.dup(num)), ffreq, res) / num))
	}
}

SawPad {
	*ar { |freq=50, ffreq=150, res=0.1, detune=0.01, rate=1|
		^SynthPad.ar(Saw, freq, ffreq, res, detune, rate)
	}
}

PulsePad {
	*ar { |freq=50, ffreq=150, res=0.1, detune=0.01, rate=1|
		^SynthPad.ar(Pulse, freq, ffreq, res, detune, rate)
	}
}


/*
Testing Zone:

{ IrregularImpulse.ar(80, 80)!2 }.play

{ SynthPad.ar(Saw, [50, 100, 200], 500, 0.3, 0.1) }.play

{ SawPad.ar([50, 100, 200], 500, 0.3, 0.1) }.play


{ PulsePad.ar([50, 100, 200], 500, 0.3, 0.1) }.play

*/
