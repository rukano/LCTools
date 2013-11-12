/*

LiveBuffer is inteded to be used in a NodeProxy or SynthDef using a buffer.
When recording, it uses another buffer.
The buffers will switch when the .buffer method is used.

// Usage:
b = LiveBuffer(1) // create live buffer with duration (in seconds)
b.record(0) // record input 0
b.record.normalize // record, wait and normalize
b.record(0, false); // no envelope
b.buffer.play; // test

// NOTE: if you record more than 2 times in a row, it will use the same buffer.
// this class has 2 buffers and uses one for playing, and one for recording
// using the method .buffer switches the active buffer
// when recording, it stick to one buffer until you use the .buffer method

// example:

// record
b.record

// play
b.buffer.play

// these 3 record on the same buffer
b.record
b.record
b.record

// switches the active buffer to the last recorded
b.bufer.play

*/


LiveBuffer {

	classvar <>initialized=false;
	var <>buffers, <>seq, <>dur, <>counter, <>activeBuffer;

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
		this.buffer;
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
		counter.postln;
		if ( (counter > 1).not ) {
			activeBuffer = seq.next;
		};
		if( env ) {
			Synth(\EZBufferRecorderSynthEnv, [\in, in, \dur, dur, \buf, activeBuffer]);
		} {
			Synth(\EZBufferRecorderSynthRaw, [\in, in, \dur, dur, \buf, activeBuffer]);
		};
	}

	normalize {
		fork{ (dur+0.1).wait; activeBuffer.normalize };
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