/*
 * @(#) JSONReferenceTest.java
 *
 * json-pointer  Java implementation of JSON Pointer
 * Copyright (c) 2021 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json.pointer.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pwall.json.JSON;
import net.pwall.json.JSONArray;
import net.pwall.json.JSONInteger;
import net.pwall.json.JSONMapping;
import net.pwall.json.JSONObject;
import net.pwall.json.JSONString;
import net.pwall.json.JSONValue;
import net.pwall.json.pointer.JSONPointer;
import net.pwall.json.pointer.JSONReference;

public class JSONReferenceTest {

    public static JSONValue testString = JSON.parse("\"test1\"");
    public static JSONValue testObject = JSON.parse("{\"field1\":123,\"field2\":[\"abc\",\"def\"]}");

    @Test
    public void shouldCreateJSONReferenceWithGivenPointer() {
        JSONReference testReference = new JSONReference(testString, JSONPointer.root);
        assertSame(testString, testReference.getBase());
        assertEquals(JSONPointer.root, testReference.getPointer());
        assertTrue(testReference.isValid());
        assertSame(testString, testReference.getValue());
        assertEquals("\"test1\"", testReference.toString());
    }

    @Test
    public void shouldCreateJSONReferenceWithDefaultPointer() {
        JSONReference testReference = new JSONReference(testString);
        assertSame(testString, testReference.getBase());
        assertEquals(JSONPointer.root, testReference.getPointer());
        assertTrue(testReference.isValid());
        assertSame(testString, testReference.getValue());
        assertEquals("\"test1\"", testReference.toString());
    }

    @Test
    public void shouldCreateJSONReferenceWithNonRootPointer() {
        JSONReference testReference = new JSONReference(testObject, new JSONPointer("/field1"));
        assertSame(testObject, testReference.getBase());
        assertEquals(JSONPointer.root.child("field1"), testReference.getPointer());
        assertTrue(testReference.isValid());
        assertEquals(new JSONInteger(123), testReference.getValue());
        assertEquals("123", testReference.toString());
    }

    @Test
    public void shouldCreateJSONReferenceWithStringPointer() {
        JSONReference testReference = new JSONReference(testObject, "/field2/0");
        assertSame(testObject, testReference.getBase());
        assertEquals(JSONPointer.root.child("field2").child(0), testReference.getPointer());
        assertTrue(testReference.isValid());
        assertEquals(new JSONString("abc"), testReference.getValue());
        assertEquals("\"abc\"", testReference.toString());
    }

    @Test
    public void shouldCreateJSONReferenceWithInvalidPointer() {
        JSONReference testReference = new JSONReference(testObject, new JSONPointer("/field99"));
        assertSame(testObject, testReference.getBase());
        assertEquals(JSONPointer.root.child("field99"), testReference.getPointer());
        assertFalse(testReference.isValid());
        assertNull(testReference.getValue());
        assertEquals("invalid", testReference.toString());
    }

    @Test
    public void shouldNavigateToChild() {
        JSONReference testReference1 = new JSONReference(testObject);
        assertSame(testObject, testReference1.getBase());
        assertEquals(JSONPointer.root, testReference1.getPointer());
        assertTrue(testReference1.isValid());
        assertTrue(testReference1.getValue() instanceof JSONMapping<?>);
        assertEquals("{\"field1\":123,\"field2\":[\"abc\",\"def\"]}", testReference1.toString());
        JSONReference testReference2 = testReference1.child("field1");
        assertSame(testObject, testReference2.getBase());
        assertEquals(JSONPointer.root.child("field1"), testReference2.getPointer());
        assertTrue(testReference2.isValid());
        assertEquals(new JSONInteger(123), testReference2.getValue());
        assertEquals("123", testReference2.toString());
    }

    @Test
    public void shouldNavigateBackToParent() {
        JSONReference testReference1 = new JSONReference(testObject);
        assertSame(testObject, testReference1.getBase());
        assertEquals(JSONPointer.root, testReference1.getPointer());
        assertTrue(testReference1.isValid());
        assertTrue(testReference1.getValue() instanceof JSONMapping<?>);
        assertEquals("{\"field1\":123,\"field2\":[\"abc\",\"def\"]}", testReference1.toString());
        JSONReference testReference2 = testReference1.child("field1");
        assertSame(testObject, testReference2.getBase());
        assertEquals(JSONPointer.root.child("field1"), testReference2.getPointer());
        assertTrue(testReference2.isValid());
        assertEquals(new JSONInteger(123), testReference2.getValue());
        assertEquals("123", testReference2.toString());
        JSONReference testReference3 = testReference2.parent();
        assertSame(testObject, testReference3.getBase());
        assertEquals(JSONPointer.root, testReference3.getPointer());
        assertTrue(testReference3.isValid());
        assertTrue(testReference3.getValue() instanceof JSONMapping<?>);
        assertEquals("{\"field1\":123,\"field2\":[\"abc\",\"def\"]}", testReference3.toString());
    }

    @Test
    public void shouldRespondCorrectlyToHasChild() {
        JSONReference testReference1 = new JSONReference(testObject);
        assertFalse(testReference1.hasChild("field99"));
        assertTrue(testReference1.hasChild("field2"));
        JSONReference testReference2 = testReference1.child("field2");
        assertFalse(testReference2.hasChild("field99"));
        assertFalse(testReference2.hasChild(2));
        assertTrue(testReference2.hasChild(0));
    }

    @Test
    public void shouldRegardNullBaseAsNotValid() {
        JSONReference testReference1 = new JSONReference(null);
        assertFalse(testReference1.isValid());
        JSONReference testReference2 = new JSONReference(null, JSONPointer.root);
        assertFalse(testReference2.isValid());
    }

    @Test
    public void shouldLocateChildInNestedObject() {
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        obj1.put("aaa", obj2);
        assertEquals(new JSONReference(obj1, "/aaa"), (new JSONReference(obj1)).locateChild(obj2));
        JSONString str1 = new JSONString("xyz");
        obj2.put("bbb", str1);
        assertEquals(new JSONReference(obj1, "/aaa/bbb"), (new JSONReference(obj1)).locateChild(str1));
        assertEquals(new JSONReference(obj2, "/bbb"), (new JSONReference(obj2)).locateChild(str1));
        JSONInteger int1 = new JSONInteger(123);
        JSONInteger int2 = new JSONInteger(456);
        JSONArray array1 = new JSONArray(int1, int2);
        obj2.put("ccc", array1);
        assertEquals(new JSONReference(obj1, "/aaa/ccc/1"), (new JSONReference(obj1)).locateChild(int2));
    }

    @Test
    public void shouldRegardEqualReferencesAsEqual() {
        JSONReference testReference1 = new JSONReference(testObject, new JSONPointer("/field2/1"));
        JSONReference testReference2 = new JSONReference(testObject).child("field2").child(1);
        assertEquals(testReference1, testReference2);
    }

    @Test
    public void shouldRegardReferencesWithDifferentPathsAsNotEqual() {
        JSONReference testReference1 = new JSONReference(testObject, new JSONPointer("/field2/1"));
        JSONReference testReference2 = new JSONReference(testObject).child("field2");
        assertNotEquals(testReference1, testReference2);
    }

    @Test
    public void shouldRegardReferencesWithDifferentBasesAsNotEqual() {
        JSONReference testReference1 = new JSONReference(testObject, new JSONPointer("/field2"));
        JSONReference testReference2 = new JSONReference(new JSONObject((JSONObject)testObject)).child("field2");
        assertNotEquals(testReference1, testReference2);
    }

}
