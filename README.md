dart-tutorials-cljs
===================

This is a ClojureScript port of the [Dart tutorials][dt].  This is just my attempt to see the differences between ClojureScript and Dart.  The code herein shouldn't be considered exemplary by any measure.

About the samples
-----------------

There are two types of tutorials:

1. Command line apps, which require Node.js to run; and
2. Web apps, which can be run using the lein-ring plug-in.

The code for all of these is put under `src/cljs` in separate directories for each project.

Building and running the samples
--------------------------------

To build the samples, just `lein cljsbuild once`.  This will build all of the samples.

### The web samples

To run the web samples, just run `lein ring server`, or `lein ring server-headless` and browse to [http://localhost:3000][lh].  The one exception to this is slambook, which requires running the slambookserver command line application.


### The command line samples

All of the compiled command-line applications are placed in `target/bin`.  To run these, just run the script using Node.js, e.g. `node target/bin/helloworld.js`.

In order to run the dcat sample, you'll first need install the `optimist` module, which you can obtain by running `npm install` from the root directory of the repository.

[dt]: https://www.dartlang.org/docs/tutorials/
[dt-code]: https://github.com/dart-lang/dart-tutorials-samples
[lh]: http://localhost:3000
