## Collaborative Graphical Editor

A document that has the ability to have multiple simultaneous editors of the same document.

The basic client/server set-up is much like that of the chat server. Each client editor has a thread for talking to the sketch server, along with a main thread for user interaction (previously, getting console input; now, handling the drawing). The server has a main thread to get the incoming requests to join the shared sketch, along with separate threads for communicating with the clients. The client tells the server about its user's drawing actions. The server then tells all the clients about all the drawing actions of each of them.
