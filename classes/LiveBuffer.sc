/*

LiveBuffer is inteded to be used in a NodeProxy or SynthDef using a buffer.
When recording, it uses another buffer.
The buffers will switch when the .buffer method is used.

// TODO: record from internal buses!
*/


LiveBuffer {

	classvar <>initialized=false;
	var <>buffers, <>seq, <>dur, <>counter, <>activeBuffer, <>inactiveBuffer;

	*new { |duration=1|
		^super.new.init(duration)
	}

	init { |duration|
		if ( Server.default.serverRunning.not ) {
			^"Server not running".warn;
		};
		if( initialized.not ) {
			this.buildSynthDefs;
			initialized = true;
		};

		dur = duration;
		buffers = {
			Buffer.alloc(Server.default, Server.default.sampleRate * dur)
		}.dup;
		seq = Pseq(buffers, inf).asStream;
		counter = 0;
		this.swap;
	}

	buildSynthDefs {
		SynthDef(\EZBufferRecorderSynthEnv, { |in, dur, buf|
			var snd = SoundIn.ar(in) * EnvGen.ar(Env.sine(dur), doneAction:2);
			RecordBuf.ar(snd, buf, 0, 1, 0, 1, 0, 1, 2);
		}).add;
		SynthDef(\EZBufferRecorderSynthRaw, { |in, dur, buf|
			var snd = SoundIn.ar(in);
			RecordBuf.ar(snd, buf, 0, 1, 0, 1, 0, 1, 2);
		}).add;
	}

	record { |in, env=true|
		counter = counter + 1;
		if ( (counter > 1).not ) { this.swap };
		if( env ) {
			Synth(\EZBufferRecorderSynthEnv, [\in, in, \dur, dur, \buf, activeBuffer]);
		} {
			Synth(\EZBufferRecorderSynthRaw, [\in, in, \dur, dur, \buf, activeBuffer]);
		};
	}

	swap {
		inactiveBuffer = activeBuffer;
		activeBuffer = seq.next;
	}

	normalize { |max=1, lag=0.1|
		fork{ (dur+lag).wait; activeBuffer.normalize(max) };
	}

	free {
		buffers.do{ |buf| buf.free };
		seq = nil;
	}

	buffer {
		counter = 0;
		^activeBuffer;
	}

}