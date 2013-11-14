// Live midi buffer is the superclass for handling small sequences recorded live for example via midi
// live midi buffer adds events on a list when it's recording
// for specializes functions, look at the subclasses

LiveMidiBuffer {
	var <>maxevents;
	var <>events;
	var <>recording=false;

	*new{ |maxevents=inf|
		^super.newCopyArgs(maxevents).init;
	}

	init {
		events = List.new;
	}

	makeEvent { |note, vel|
		^(note:note, vel:vel, amp:vel/127, time:Main.elapsedTime)
	}

	addEvent { |note, vel|
		var event = this.makeEvent(note, vel);
		if( recording ) {
			events.add( event );
		};
	}

	reset {
		this.init
	}

	record {
		recording = true;
	}

	stop {
		recording = false;
	}

	getEventFromType { |type=\note|
		^events.collect{ |event| event[type] }
	}

	notes {
		^this.getEventFromType( \note )
	}

	freqs {
		^this.notes.midicps
	}

	velocities {
		^this.getEventFromType( \vel )
	}

	vel { // convenience for lazy ;)
		^this.velocities
	}

	amps {
		^this.getEventFromType( \amp )
	}

	absoluteTimes {
		^this.getEventFromType( \time )
	}

	relativeTimes {
		var times = this.absoluteTimes;
		times = times - times[0];
		^times.differentiate
	}

	durations { |addTime=1|
		^this.relativeTimes.put(0,addTime).rotate(-1)
	}

	seconds {
		var times = this.absolutTimes;
		^(times.last - times.first)
	}

	size {
		^events.size
	}
}

// fills the list and pushes back when full
LiveMidiRing : LiveMidiBuffer {
	addEvent { |note, vel|
		var event = this.makeEvent(note, vel);
		if( recording ) {
			if ( events.size >= maxevents ) {
				events = events.rotate(-1).put(events.size-1, event)
			} {
				events.add( event );
			}
		};
	}
}

// fills the list and stops automatically when reaches max size
LiveMidiSeq : LiveMidiBuffer {
	addEvent { |note, vel|
		var event = this.makeEvent(note, vel);
		if( recording ) {
			if ( events.size > maxevents ) {
				this.stopRecording;
			} {
				events.add( event );
			};
		};
	}
}


/*

a = LiveMidiBuffer(8)
a.record
a.addEvent(3.rand+60, 3.rand+100, Main.elapsedTime)
a.events.size
a.stop
a.reset.record

a.absoluteTimes
a.relativeTimes
a.durations


Pbind(\midinote, Pseq(a.notes, inf), \amp, Pseq(a.amps, inf), \dur, Pseq(a.durations(0.5), inf)).play

*/