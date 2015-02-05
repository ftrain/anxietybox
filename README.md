# AnxietyBox

This is the Clojure project code to run anxietybox.com.

This project was discussed on the Reply All podcast in early 2015
http://gimletmedia.com/episode/the-anxiety-box/

And later featured on This American Life.

It runs at
http://anxietybox.com

I will write essay about it that will be published on Medium one of
these days.

## TODO

This thing is a hacky mess; it was my first attempt to get something
working on a website using Clojure. And also to manage my
anxiety. It worked, but it's badly organized and undocumented.

Since I created it I wrote probably 20,000 more lines of
Clojure/Clojurescript, so it should be possible to refactor into
something more orderly.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

The code relies on the Mandrill API. Mandrill donated a million
emails per month.

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2014-2015 Paul Ford
