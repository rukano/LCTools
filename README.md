LCTools
=======

Live Coding Tools developed at a residency at STEIM Nov 11th-15th 2013 by Juan A. Romero and Felipe Ignacio.
Developement will continue after the residency for more general usability.

LiveBuffer
-----------

A dual Buffer system with autoswaping. It uses one buffer for playing and the other one for recording. The buffers swap when a new recording is started.

LiveMidi
--------

A class for recording events (specially midi events) as a sequence or in ring (pushback). The recording of the buffer can be started and stopped easily and then the events can be extracted as arrays.

CodeDisplay
-----------
A simple client/server solution for displaying code on a screen from different computer/clients.
The clients can also control some global properties on the server (color, size, etc).
Everybody can send text to the server and it will be displayed. Be sure you can trust the people that are going to send text for displaying on the server. There is no mechanism to ban IPs or stuff like that. ;)

FeedbackUGens
-------------

Wrapper for feedback with effects (depends on the Feedback Quark)

- PitchDelay: PitchShift in the feedback loop
- FilterDelay: BPF in the feedback loop
- DubDelay: BPF, reverse (ping pong) and softclip in the feedback loop
- ReverbDelay: FreeVerb in the feedback loop


HelperUGens
-----------

Pseudo UGens with UGen combinations I usually use. This is for less typing (avoiding snippets) and meybe more understandability for the audience.
