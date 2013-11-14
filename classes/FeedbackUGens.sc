// These Pseudo Ugens are convenience methos for creating a feedback with a function in it
// it depends on the Feedback Quark

FeedbackDelay {
	classvar <>maxtime=4;
}

PitchDelay : FeedbackDelay {
	*ar { |in, time=1, decay=0.9, pitch=2, size=0.1, pdisp=0, tdisp=0|
		^FbC({ |fb| PitchShift.ar(in + fb, size, pitch, pdisp, tdisp) * decay }, maxtime, time);
	}
}

FilterDelay : FeedbackDelay {
	*ar { |in, time=1, decay=0.9, freq=800, rq=2.3|
		^FbC({ |fb| BPF.ar(in + fb, freq, rq) * decay }, maxtime, time);
	}
}

DubDelay : FeedbackDelay {
	*ar { |in, time=1, decay=0.9, freq=800, rq=2.3|
		^FbC({ |fb| BPF.ar(in + fb, freq, rq).reverse.softclip * decay }, maxtime, time);
	}
}

ReverbDelay : FeedbackDelay {
	*ar { |in, time=1, decay=0.9, mix=0.3, room=0.3, damp=0.5|
		^FbC({ |fb| FreeVerb.ar(in + fb, mix, room, damp) * decay }, maxtime, time);
	}
}

/*
Testing Zone


FeedbackDelay.maxtime = 4; // globally set the max time

play{ var in = Impulse.ar(1); in + PitchDelay.ar(in!2, 0.3, 0.9, 1.5, 0.01, 1, 1) }
play{ var in = Impulse.ar(1); in + FilterDelay.ar(in!2, 0.3, 0.9) }
play{ var in = Impulse.ar(1); in + DubDelay.ar(in!2, 0.3, 0.5) }
play{ var in = Impulse.ar(4); in + ReverbDelay.ar(in!2, 0.3, 0.5) }


*/