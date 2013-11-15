CodeDisplay {
	var <>width, <>height, <>alpha;
	var <>window, <>responder, <>sources;

	*new { |width=800, height=600, alpha=1|
		^super.newCopyArgs(width, height, alpha).init;
	}

	init {
		sources = ();
		responder = OSCFunc({ |m|

		}, "/codedisplay");
	}

	registerNewSource { |name|
		sources[name] = CodeObject(prefix:name);
	}

	clear {
//		responder.
	}
}

CodeObject {
	var <>string, <>position, <>color, <>width, <>font, <>prefix;

	*new { |string, position, color, width, font, prefix|
		^super.newCopyArgs(string, position, color, width, prefix).init;
	}

	init {
		string =  string ? "";
		position = position ? Point(0,0);
		color = color ? Color.rand;
		width = width ? 500;
		font = font ? Font("Monaco", 9);
		prefix = prefix ? "Anonymous";
	}

	draw {

	}
}