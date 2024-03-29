# json-pointer

[![Build Status](https://travis-ci.com/pwall567/json-pointer.svg?branch=main)](https://travis-ci.com/github/pwall567/json-pointer)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/net.pwall.json/json-pointer?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.pwall.json%22%20AND%20a:%22json-pointer%22)

Java implementation of [JSON Pointer](https://tools.ietf.org/html/rfc6901).

## Quick Start

To create a JSON Pointer:
```java
    JSONPointer pointer = new JSONPointer("/prop1/0");
```
This creates a pointer to the 0th element of the "prop1" property (of whatever JSON value is addressed).

To test whether such an element exists in the JSON object "obj":
```java
    if (pointer.exists(obj)) {
        // whatever
    }
```

To retrieve the element:
```java
    JSONValue value = pointer.find(obj);
```

A pointer to the root element of any JSON value:
```java
    JSONPointer pointer = JSONPointer.root;
```

To navigate to a child property:
```java
    JSONPointer newPointer = pointer.child("prop1");
```

To navigate to a child array element:
```java
    JSONPointer newPointer2 = newPointer.child(0);
```
(the result of the last two operations is a pointer equivalent to the pointer in the first example).

To create a pointer to a specified child value within a structure:
```java
    JSONPointer childPointer = JSONPointer.root.locateChild(structure, target);
```
(This will perform a depth-first search of the JSON structure, so it should be used only when there is no alternative.)

## `JSONReference`

A `JSONReference` is a combination of a `JSONPointer` and a `JSONValue`.
This can be valuable when navigating around a complex tree &ndash; it removes the necessity to pass around both a
pointer and the base value to which it refers, and it pre-calculates the destination value (and its validity).

To create a `JSONReference`:
```java
    JSONReference ref = new JSONReference(base, pointer);
```

If the pointer is to the root element it may be omitted:
```java
    JSONReference ref = new JSONReference(base);
```

The `parent()` and `child()` operations work on `JSONReference`s similarly to the `JSONPointer` equivalents.

To get the value from the `JSONReference`:
```java
    JSONValue value = ref.getValue(); // may be null
```

To test whether the reference is valid, that is, the pointer refers to a valid location in the base object:
```java
    if (ref.isValid()) {
        // the reference can be take to be valid
    }
```

To test whether the reference has a nominated child:
```java
    if (ref.hasChild(name)) { // or index
        // use child ref.child(name)
    }
```

To create a reference to a specified child value:
```java
    JSONReference childRef = baseRef.locateChild(target);
```
(This will perform a depth-first search of the JSON structure, so it should be used only when there is no alternative.)

## Dependency Specification

The latest version of the library is 2.5, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-pointer</artifactId>
      <version>2.5</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-pointer:2.5'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-pointer:2.5")
```

Peter Wall

2023-12-04
