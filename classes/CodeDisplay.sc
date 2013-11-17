////////////////////////////////////////////////////////////////////////
// Code Display (Client/Server)
// for easy code desplaying
// on SC3 cleints, they can use the:
// thisProcess.interpreter.codeDump function
// to directly send code to the server
// the client is a wrapper for the OSC messages to the server


// Roadmap:
// TODO: receive characters and display them
// TODO: visualize typing (keyboard view?)



/*
// Start Server
c = CodeDisplayServer(Rect(0,0,800,600), 100);

// Start Client
d = CodeDisplayClient(\juan, NetAddr("localhost", 57120));
d.autosend_(true)

// force send code
d.sendCode("asdfasdifjoaidsfjoaf")

d.set(\font, "Menlo", 18)
d.set(\color, 1, 1, 1, 1)


*/

CodeDisplayServer {
	var <>bounds, <>fadeRate, <>alpha;
	var <>window, <>uview;
	var <>responder, <>sources;

	*new { |bounds, fadeRate=1, alpha=1|
		^super.newCopyArgs(bounds, fadeRate, alpha).init;
	}

	init {
		bounds = bounds ? Rect(0, 0, 800, 600);
		sources = ();
		responder = ();

		this.makeResponders;
		this.makeWindow;
	}

	makeWindow {
		window = Window("Code Display", bounds, false).front;
		window.background_( Color.black );
		uview = UserView(window, window.view.bounds);
		uview.animate = true;
		uview.clearOnRefresh_(false);
		// TODO: use window transparency (?)
		this.frameRate_(30);

		"creating view".postln;
		uview.drawFunc = { |uview|
			Pen.fillColor = Color(0,0,0,fadeRate.reciprocal);
			Pen.fillRect( uview.bounds );
			sources.keysValuesDo{ |name, obj|
				obj.draw;
			};
		};

		window.onClose_{ this.free };
	}

	makeResponders {
		// TODO: global responder for setting colors, fade rate, etc
		responder.global = OSCFunc({ |m|
			var cmd = m[1];
			var arguments = m[2..];
			cmd.switch(
				\faderate, {
					fadeRate = argument.first;
				}
				\background, {
					defer{ window.background_( Color(*arguments) ) };
				},
				\refresh, {
					defer{ window.refresh };
				},
				\alpha, {
					alpha = arguments.first;
					defer{ window.alpha_(alpha) };
				},
				\bounds, {
					bounds = Rect(*arguments);
					defer{ window.bounds = bounds };
				},
				\fullscreen, {
					defer{ window.fullScreen };
				},
				\windowed, {
					defer{ window.endFullScreen };
				},
				\close, {
					defer{ window.close };
				},
				\clear, {
					sources.do{ |source| source.code = "" };
				},
				{ "Command % not found".format(cmd).postln }
			);
		}, "/codedisplay/global").permanent_(true);

		responder.setter = OSCFunc({ |m|
			// set stuff remotely (font, size, etc)
			var name = m[1];
			var cmd = m[2];
			var arguments = m[3..];
			var obj;
			this.checkAndRegister(name);
			obj = sources[name];
			cmd.switch(
				\font, {
					obj.font = Font(*arguments);
				},
				\bounds, {
					obj.bounds = Rect(*arguments);
				},
				\color, {
					obj.color = Color(*arguments);
				},
				\remove, {
					this.removeSource( name );
				},
				{ "Property % not found".format(cmd).postln }
			);

			m.postln;
		}, "/codedisplay/set").permanent_(true);
		responder.code = OSCFunc({ |m|
			// receives only a [name, codestring]
			var name = m[1];
			var code = m[2];
			this.checkAndRegister(name);
			sources[name].code = code.asString;
			m.postln;
		}, "/codedisplay/code").permanent_(true);
		responder.chars = OSCFunc({ |m|
			// receives a key and a char
			// [name, char]
			// TODO: display incoming keystrokes
			m.postln;
		}, "/codedisplay/char").permanent_(true);

	}

	frameRate_ { |fps|
		uview.frameRate_(fps);
	}

	checkAndRegister { |name|
		if( sources[name].isNil ) {
			"Registered new source: %".format(name).postln;
			this.registerNewSource(name);
		}
	}

	registerNewSource { |name|
		sources[name] = CodeObject(name);
		"Source % has been added".format(name).postln;
	}

	removeSource { |name|
		sources.removeAt( name );
		"Source % has been removed".format(name).postln;
	}

	free {
		// frees the responders only
		"freeing responders".postln;
		responder.do{ |resp| resp.free };
	}

	clear {
		// clears the sources and frees the responders
		this.free;
		sources = nil;
	}
}

CodeDisplayClient {
	var <>name, <>addr;
	var <>autosending=false;

	*new { |name, addr|
		if( name.isNil ) { ^"you have to have a name".error };
		if( addr.isNil ) { ^"you have to give a NetAddr to the CodeDisplay Server".error };
		^super.newCopyArgs(name, addr);
	}

	sendCode { |code|
		addr.sendMsg("/codedisplay/code", name, code);
	}

	sendChar { |char|
		// TODO: send keystrokes
	}

	setProperty { |property ... arguments|
		addr.sendMsg("/codedisplay/set", property, *arguments);
	}

	set { |...arguments|
		this.setProperty(*arguments);
	}

	setGlobal { |...arguments|
		addr.sendMsg("/codedisplay/global", *arguments);
	}

	autosend {
		this.autosend_( autosending.not );
	}

	autosend_ { |bool|
		if( true ) {
			autosending = true;
			thisProcess.interpreter.codeDump = { |code| this.sendCode(code) }
		} {
			autosending = false;
			thisProcess.interpreter.codeDump = nil;
		}
	}
}

CodeObject {
	var <>name, <>code, <>bounds, <>color, <>font;

	*new { |name, code, bounds, color, font|
		^super.newCopyArgs(name, code, bounds, color, font).init;
	}

	init {
		if( name == nil ) { ^"Must give a name".warn };
		code =  code ? "";
		bounds = bounds ? Rect(0,0,800,600);
		color = color ? Color.rand;
		font = font ? Font("Monaco", 12);
	}

	draw {
		code.drawInRect( bounds, font, color );
	}
}