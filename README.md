# json-pointer

[![Build Status](https://travis-ci.org/pwall567/json-pointer.svg?branch=main)](https://travis-ci.org/pwall567/json-pointer)
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


## Dependency Specification

The latest version of the library is 1.0, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-pointer</artifactId>
      <version>1.0</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-pointer:1.0'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-pointer:1.0")
```

Peter Wall

2021-01-10
