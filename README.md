# JSON Parser
A collection of Java classes for parsing (and searching through) [JSON](https://www.json.org/json-en.html).

This library should be prefered for projects which require simple, unencumbered JSON parsing or an easily searchable output JSON structure. However, other use cases may also see improvements over other libraries depending on the specifics of the application.

## Advantages
This library provides many advantages for use over other libraries such as [GSON](https://github.com/google/gson) and [Jackson](https://github.com/FasterXML/jackson).

1. Easy use with no additional class creation necessary
2. A powerful, semi-regex-style, search syntax for easy accesss to specific files
3. Speed in scanning and parsing JSON

### Ease of Use
Many other JSON libraries requiure creating POJO classes which mirror the objects found in the parsed JSON. However, this library gets around that issue by instead parsing the JSON into a collection of JSONValues, which can each represent a different JSON cnstruct.

### Powerful Search Syntax
The provided search syntax can search through the entire JSON tree based upon the specified pathing and return every single JSON element which matches that path specification.

The special syntax is structured quite similarly to Java's package formatting, with periods ('.') and brackets ('\[', '\]') acting as separators for JSON element names and array indices, respectively. For example, "object.variable" finds the element named "object" and accesses its internal element named "variable", and "array\[index\]" finds the element named "array" and accesses the element at index "index" in the array. Periods and brackets are actually treated identically by the search parser, but each should only be used for its given use case for readability.

In addition to basic text matching, there are also 2 special characters: asterisks ('\*') and question marks ('?').
* Asterisks are wildcard characters which designate one miscellaneous term in the search. For example, "a.\*.b" can match "a.x.b" and "a.y.b" but not "a.b" or "a.x.y.b".
* On the other hand, question marks are multi-level (recursive) wildcard characters. For example, "a.?.b" can match "a.b", "a.x.b", "a.x.y.b", and more.

### Scanning and Parsing Speed
Although not the fastest library around by any stretch of the imagination, this library does provide significant speed, with a scanner that can tokenize the input file at roughly the same speed as a BufferedReader can provide new lines (in *most* JSON files). Additionally, the JSON parser is fast, though not as much as the scanner. There are currently plans to implement a non-recursive parser, which would theoretically speed up the parsing process.

## How to Use
All of the necessary JSON stuff can be found in "[me.utk.json_parser.json](https://github.com/utk003/JSON-Parser/tree/main/src/me/utk/json_parser/json)". The parser itself is "[me.utk.json_parser.json.JSONParser.java](https://github.com/utk003/JSON-Parser/blob/main/src/me/utk/json_parser/json/JSONParser.java)" Some methods might not be fully implemented, so beware (*TODO fix that*). Also, JavaDoc is planned, and currently nonexistant.

Also, "[src/Main.java](https://github.com/utk003/JSON-Parser/blob/main/src/Main.java)" has some benchmarking methods for the scanner and parser. The scanner is quite fast, but the parser is extremely slow in comparison. A non-recursive parser is in developement to remedy this issue, but it is not yet complete.
