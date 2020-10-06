# json-pointer

Java implementation of JSON Pointer

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
    JSONValue value = pointer.eval(obj);
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

The latest version of the library is 0.2, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-pointer</artifactId>
      <version>0.2</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-pointer:0.2'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-pointer:0.2")
```

Peter Wall

2020-10-06
