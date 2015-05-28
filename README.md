# treemud

A multi-threaded mud server written in clojure with a diku style commands.

## Installation

Expand into its host directory. Should work from there.


## Usage

Recommended to be launched with a repl for debugging and administration.

~/treemud $ lein repl

Then

treemud.core=> (launch-server)

Will open up on port 13579 by default.

I personaly use cider as I have an emacs obsession. But anything that can work with
a lein repl and/or nREPL should work fine.


## License

Copyright © 2015 Nathanael Cunningham

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
