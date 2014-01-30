////////////////////////////////////////////////////////////////////////
// Code Display (Client/Server)
// for easy code displaying
// on SC3 cleints, they can use the:
// thisProcess.interpreter.codeDump function
// to directly send code to the server
// the client is a wrapper for the OSC messages to the server


// Roadmap:
// TODO: receive characters and display them
// TODO: visualize typing (keyboard view?)
// TODO: add possibility to change horizontal rendering sequence of the different clients
// TODO: fadeTime is currently not used

/*
Example Code:

// Start Server
c = CodeDisplayServer(Rect(0,0,800,600), 100);

// Start Client
d = CodeDisplayClient(\myName, NetAddr("localhost", 57120));
d.autosend_(true)

// force send code
d.sendCode("asdfasdifjoaidsfjoaf")

// Configure Global Server Properties
d.setGlobal(\faderate, 100)
d.setGlobal(\background, 0.2, 0.4, 0.8)
d.setGlobal(\refresh);
d.setGlobal(\bounds, 0, 0, 1024, 768)
d.setGlobal(\fullscreen)
d.setGlobal(\windowed)
d.setGlobal(\close)
d.setGlobal(\clear)
d.setGlobal(\removeAll)
d.setGlobal(\autoSetBounds, true)
d.setGlobal(\marginWidth, 5)

// Configure Client-specific Properties
d.set(\font, "Monaco", 15);
d.set(\color, 0.6, 0.6, 0.9);
d.set(\numCodeSnippets, 20)
d.set(\charsPerLine, 30)
d.set(\lineHeight, 20);
d.set(\hueDelta, 0.04)
d.set(\valueDelta, 0.05)
d.set(\drawLines, true)

*/

CodeDisplayServer {
	var <>bounds, <>fadeRate;
	var <>window, <>uview;
	var <>responder, <>sources;
	
	// flag indicating whether bounds are automatically adjusted when a new client connects
	var <>autoSetBounds = true;
	
	// margin width in pixels between source display bounds
	var <>marginWidth = 5;
	
	*new { |bounds, fadeRate=1|
		^super.newCopyArgs(bounds, fadeRate).init;
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
		// window.background_( Color.black );
		uview = UserView(window, window.view.bounds);
		uview.animate = true;
		uview.clearOnRefresh_(false);
		// TODO: use window transparency (?)
		this.frameRate_(30);

		"creating view".postln;
		uview.drawFunc = { |uview|
			Pen.fillColor = uview.background; // Color(0,0,0,fadeRate.reciprocal);
			Pen.fillRect( uview.bounds );
			
			sources.values.do{ |source|
				source.draw;
			};
		};

		window.onClose_{ this.free };
	}

	makeResponders {
		// OSC Responder for global settings (colors, fade rate, etc.)
		responder.global = OSCFunc({ |m|
			var cmd = m[1];
			var arguments = m[2..];
			cmd.switch(
				\faderate, {
					fadeRate = arguments.first;
					"fadeRate set to %".format(fadeRate).postln;
				},
				\background, {
					var color = Color(*arguments);
					defer { uview.background_( color ) };
					"background color set to %".format(color).postln;
				},
				\refresh, {
					defer{ window.refresh };
					"code window refreshed".postln;
				},
				\bounds, {
					bounds = Rect(*arguments);
					defer{ window.bounds = bounds; uview.bounds = bounds };
					"bounds set to %".format(bounds).postln;
				},
				\fullscreen, {
					defer{ window.fullScreen };
					"fullscreen activated".postln;
				},
				\windowed, {
					defer{ window.endFullScreen };
					"window mode activated".postln;
				},
				\close, {
					defer{ window.close };
					"window closed".postln;
				},
				\clear, {
					sources.do{ |source| source.codeList = List() };
					"cleared all source code data".postln;
				},
				\removeAll, {
					sources = ();
					"removed all sources".postln;
				},
				\autoSetBounds, {
					autoSetBounds = arguments.first.asBoolean;
					"autoSetBounds is now %".format(autoSetBounds).postln;
				},
				\marginWidth, {
					marginWidth = arguments.first.asInteger;
					"marginWidth is now %".format(marginWidth).postln;
					this.autoAdjustBounds;
				},
				{ "Command % not found".format(cmd).postln }
			);
		}, "/codedisplay/global").permanent_(true);
		
		// OSC Responder for client-specific settings (font, color, number of Strings to store etc.)
		responder.setter = OSCFunc({ |m|
			// set stuff remotely (font, size, etc)
			var name = m[1];
			var cmd = m[2];
			var arguments = m[3..];
			var source;
			this.checkAndRegister(name);
			source = sources[name];
			cmd.switch(
				\font, {
					var font = Font(*arguments);
					source.font = font;
					"Font for % is now %".format(name, font).postln;
				},
				\bounds, {
					var bounds = Rect(*arguments);
					source.bounds = bounds;
					"Bounds for % are now %".format(name, bounds).postln;
				},
				\color, {
					var color = Color(*arguments);
					source.color = color;
					source.hsvColor = color.asHSV;
					"Color for % is now %".format(name, color).postln;
				},
				\remove, {
					this.removeSource( name );
					"Removed source %".format(name).postln;
				},
				\numCodeSnippets, {
					var numCodeSnippets = arguments.first.asInteger;
					source.numCodeSnippets = numCodeSnippets;
					"For % only % code strings are stored now".format(name, numCodeSnippets).postln;
					
				},
				\charsPerLine, {
					var charsPerLine = arguments.first.asInteger;
					source.charsPerLine = charsPerLine;
					"% now has % characters per line".format(name, charsPerLine).postln;
				},
				\lineHeight, {
					var lineHeight = arguments.first.asInteger;
					source.lineHeight = lineHeight;
					"Line distance for % is now %".format(name, lineHeight).postln;
				},
				\valueDelta, {
					var valueDelta = arguments.first.asFloat;
					source.valueDelta = valueDelta;
					"Value delta for % is now %".format(name, valueDelta).postln;
				},
				\hueDelta, {
					var hueDelta = arguments.first.asFloat;
					source.hueDelta = hueDelta;
					"Hue delta for % is now %".format(name, hueDelta).postln;
				},
				\drawLines, {
					var drawLines = arguments.first.asBoolean;
					source.drawLines = drawLines;
					"Draw lines flag for % is now %".format(name, drawLines).postln;
				},
				{ "Unhandled property %".format(cmd).postln }
			);

			// m.postln;
		}, "/codedisplay/set").permanent_(true);
		
		// OSC Responder receiving the actual code snippets
		responder.code = OSCFunc({ |m|
			// receives only a [name, codestring]
			var name = m[1];
			var code = m[2];
			this.checkAndRegister(name);
			sources[name].appendCode(code);
			// m.postln;
		}, "/codedisplay/code").permanent_(true);
		
		// OSC Responder for keystrokes
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
			if(autoSetBounds) { this.autoAdjustBounds };
		}
	}
	
	autoAdjustBounds {
		var numSources = sources.size;
		var widthPerSource = round(bounds.width / numSources);
		var x = 0;
		sources.values.do { |source|
			var rect = Rect(x+marginWidth, 0, widthPerSource-(2*marginWidth), bounds.height);
			source.bounds = rect;
			"bounds for % are now %".format(source.name, rect).postln;
			x = x + widthPerSource;
		}
	}

	registerNewSource { |name|
		var source = CodeDisplaySource(name);
		sources[name] = source;
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

	setProperty { |name ... arguments|
		addr.sendMsg("/codedisplay/set", name, *arguments);
	}

	set { |...arguments|
		this.setProperty(name, *arguments);
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

/*
* Class to store data for individual clients including color, font, number of code snippets to store etc.
* The code snippets arriving from the clients are stored in a list inside this data structure.
*/
CodeDisplaySource {
	// Name of this client as symbol
	var <>name;
	
	// list of code objects for this client
	var <>codeList;
	
	// individual bounds for this client
	var <>bounds;
	
	// Code color
	var <>color;
	
	// Code font
	var <>font;
	
	// number of code snippets displayed for this client
	var <>numCodeSnippets;
	
	// number of chars where lines are wrapped automatically
	var <>charsPerLine;
	
	// boolean indicating whether lines between code snippets should be drawn
	var <>drawLines;
	
	// line height in pixels for this client
	var <>lineHeight;
	
	// positive float (0 <= valueDelta <= 1) to achieve a fade-out effect towards the bottom of the screen
	var <>valueDelta;
	
	// positive float (0 <= hueDelta <= 1) to achieve a color change effect towards the bottom of the screen
	var <>hueDelta;
	
	// remember to set this every time the color is updated. Array containing hue, saturation, value and alpha of the color
	var <>hsvColor;
	
	
	*new { |name, bounds, color, font|
		^super.newCopyArgs(name, bounds, color, font).init;
	}

	init {
		if( name == nil ) { ^"Must give a name".warn };
		codeList = List();
		bounds = bounds ? Rect(0,0,800,600);
		color = color ? Color.rand;
		hsvColor = color.asHSV;
		font = font ? Font("Monaco", 14);
		numCodeSnippets = numCodeSnippets ? 20;
		charsPerLine = charsPerLine ? 50;
		lineHeight = lineHeight ? 20;
		valueDelta = valueDelta ? 0.05;
		hueDelta = hueDelta ? 0.05;
		drawLines = drawLines ? true;
	}
	
	appendCode { |code|
		var linesOfCodeContainer = LinesOfCode(code, charsPerLine);
		
		// check if there are non-empty lines
		if (linesOfCodeContainer.isEmpty.not)
		{
			// insert the code snippet at the beginning of the list in order to be displayed on top
			codeList.insert(0, linesOfCodeContainer);
			if(codeList.size > numCodeSnippets) {
				// only keep the first numStings entries
				codeList = codeList[..(numCodeSnippets-1)];
			};
		}
	}
	
	/*
	* Draw the code snippets for this client.
	* Uses HSV colors to achieve a fade out effect towards the bottom of the screen.
	*/
	draw {
		var x = bounds.left;
		var y = bounds.top;
		var value = hsvColor[2];
		var hue = hsvColor[0];
		var right = bounds.left + bounds.width;
		
		codeList.do { |linesOfCode|
			
			if(value > 0) {
				var colorFaded = Color.hsv(hue, hsvColor[1], value);
				linesOfCode.lines.do { |line|
					line.drawInRect(Rect(x, y, bounds.width, bounds.height-y), font, colorFaded);
					y = y + lineHeight;
				};
				
				if(drawLines)
				{
					Pen.strokeColor = colorFaded;
					Pen.line(Point(x, y), Point(right,y));
					Pen.stroke;
				};
				
				value = value - valueDelta;
				hue = (hue - hueDelta) % 1;
			}
		}
	}
	
}

/*
* Container class for multiple lines of code.
* This class is also responsible for trimming lines to a specified length.
*/
LinesOfCode {
	
	var <>lines;
	
	*new { |code, charsPerLine|
		^super.new.init(code.asString, charsPerLine);
	}
	
	// splits all lines which are longer than charsPerLine to multiple lines
	init { |code, charsPerLine|
		var lineList = List.newUsing(code.split($\n));
		
		lines = List.new;
		lineList.do { |line, i|
			while ( { line.size > charsPerLine },
			{
				this.addLine(line[..(charsPerLine-1)]);
				line = line[charsPerLine..];
			});
			
			this.addLine(line);
		}
		
	}
	
	// adds a line only if it's not empty
	addLine { |line|
		if(line != "") { lines.add(line) }
	}
	
	// checks whether all lines stored for this instance are empty
	isEmpty {
		lines.do { |line|
			if (line != "") { ^false }
		}
		
		^true
	}
	
}