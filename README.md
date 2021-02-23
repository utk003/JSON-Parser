# JSON Library
A collection of Java classes for parsing (and searching through) [JSON](https://www.json.org/json-en.html).

This library should be prefered for projects which require simple, unencumbered JSON parsing, an easily searchable output JSON structure, or strictly defined object-oriented JSON. However, other use cases may also see improvements over other libraries depending on the specifics of the application.

## Project Website + Documentation
This project has a website, from where you can also access documentation for all stored versions as well as releases (as JARs) of this library for those versions.

Project Page: [JSON Library Site](https://utk003.github.io/JSON-Parser/) \
Documentation: [JSON Library Documentation](https://utk003.github.io/JSON-Parser/documentation.html) \
Releases: [JSON Library Releases](https://utk003.github.io/JSON-Parser/releases.html)

## Advantages
This library provides many advantages for use over other libraries such as [GSON](https://github.com/google/gson) and [Jackson](https://github.com/FasterXML/jackson).

1. Options for both simple class-free JSON parsing and POJO-based JSON
   * Convenient conversion from class-free JSON to POJO-based JSON  
2. A powerful, semi-regex-style search syntax for easy access to different JSON elements
3. Speed in scanning and parsing JSON

### Class-free JSON vs POJO-based JSON
Many other JSON libraries requiure creating POJO classes which mirror the objects found in the parsed JSON. However, this library provides the option to instead parse JSON into a tree of JSONValues.

The class-free JSON parsing, which can be found in the `io.github.utk003.json.traditional` package, creates a tree of JSONValues which can then be iterated through using various accessor methods or searched using an advanced-regex-like search syntax (see below).

The POJO-based JSON parsing, for which I have create the term "Object-Oriented JSON" (OOJ) for convenience, can be found in the `io.github.utk003.json.ooj`. This package provides functionality for parsing JSON into POJOs or for converting the tree-based traditional JSON parsing output (of JSONValues) into POJOs. 

### Powerful Search Syntax
This library provides a special search syntax can search through JSONValue trees for all JSON elements that matches a specific element path.

The search syntax closely mirrors Java's package/array syntax, with periods (`.`) identifying object's elements and brackets (`[`, `]`) designating a particular index in an array. For example, `object.variable` finds the JSON object element named `object` and accesses its internal element named `variable`, and `array[index]` finds the JSON array element named `array` and accesses the element at index `index`.

In addition to basic text matching, there are also 1 special character: asterisks (`*`).
* Asterisks are wildcard characters which designate a miscellaneous term in the search. For example, `a.*.b` can match `a.x.b` and `a.y.b` but not `a.b` or `a.x.y.b`.

### Scanning and Parsing Speed
Although not the fastest library around, this library does provide significant speed, with a scanner that can tokenize the input file at roughly the same speed as a BufferedReader can provide new lines (in *most* JSON files). The JSON parser is quite fast as well, though not to the same extent as the scanner.

## How to Use
For a tree-based, class-free JSON parsing approach, use the `io.github.utk003.json.traditional.JSONParser` class. For an OOJ approach instead, use `io.github.utk003.json.ooj.OOJParser`.
