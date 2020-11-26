# JSON Parser
A collection of Java classes for parsing (and searching through) JSON.

## How to Use
All of the necessary JSON stuff can be found in "me.utk.json_parser.json". Some methods might not be fully implemented, so beware (*TODO fix that*). Also, JavaDoc is planned, and currently nonexistant.

"src/Main.java" has some benchmarking methods for the scanner and parser. The scanner is quite fast, but the parser is extremely slow. A non-recursive parser was in developement to remedy that issue, but it is not yet complete.

## Advantages
The primary advantage of using this parser is that it provides a powerful search syntax involving wildcards and variable intermediates to provide a lot of power to the user in terms of the results. (*TODO a detailed listing of the JSON search syntax*)
