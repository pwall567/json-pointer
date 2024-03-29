/*
 * @(#) JSONPointerTest.java
 *
 * json-pointer  Java implementation of JSON Pointer
 * Copyright (c) 2020, 2021, 2023 Peter Wall
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

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import net.pwall.json.JSON;
import net.pwall.json.JSONArray;
import net.pwall.json.JSONInteger;
import net.pwall.json.JSONObject;
import net.pwall.json.JSONString;
import net.pwall.json.JSONValue;
import net.pwall.json.pointer.JSONPointer;
import net.pwall.json.pointer.JSONPointerException;

public class JSONPointerTest {

    private static JSONValue document = null;
    private static final JSONString string1 = new JSONString("bar");
    private static final JSONString string2 = new JSONString("baz");
    private static final JSONArray array1 = new JSONArray(string1, string2);
    private static final JSONValue testArray = JSON.parse(
            "[\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\",\"H\",\"I\",\"J\",\"K\",\"L\",\"M\",\"N\",\"O\",\"P\"]");

    @BeforeClass
    public static void setupDocument() throws IOException {
        document = JSON.parse(new File("src/test/resources/json-pointer-example.json"));
    }

    @Test
    public void shouldGiveResultsShownInExampleInSpecification() {
        assertSame(document, createPointer("").eval(document));
        assertEquals(array1, createPointer("/foo").eval(document));
        assertEquals(string1, createPointer("/foo/0").eval(document));
        assertEquals(new JSONInteger(0), createPointer("/").eval(document));
        assertEquals(new JSONInteger(1), createPointer("/a~1b").eval(document));
        assertEquals(new JSONInteger(2), createPointer("/c%d").eval(document));
        assertEquals(new JSONInteger(3), createPointer("/e^f").eval(document));
        assertEquals(new JSONInteger(4), createPointer("/g|h").eval(document));
        assertEquals(new JSONInteger(5), createPointer("/i\\j").eval(document));
        assertEquals(new JSONInteger(6), createPointer("/k\"l").eval(document));
        assertEquals(new JSONInteger(7), createPointer("/ ").eval(document));
        assertEquals(new JSONInteger(8), createPointer("/m~0n").eval(document));
    }

    @Test
    public void shouldGiveResultsShownInExampleInSpecificationUsingFind() {
        assertSame(document, createPointer("").find(document));
        assertEquals(array1, createPointer("/foo").find(document));
        assertEquals(string1, createPointer("/foo/0").find(document));
        assertEquals(new JSONInteger(0), createPointer("/").find(document));
        assertEquals(new JSONInteger(1), createPointer("/a~1b").find(document));
        assertEquals(new JSONInteger(2), createPointer("/c%d").find(document));
        assertEquals(new JSONInteger(3), createPointer("/e^f").find(document));
        assertEquals(new JSONInteger(4), createPointer("/g|h").find(document));
        assertEquals(new JSONInteger(5), createPointer("/i\\j").find(document));
        assertEquals(new JSONInteger(6), createPointer("/k\"l").find(document));
        assertEquals(new JSONInteger(7), createPointer("/ ").find(document));
        assertEquals(new JSONInteger(8), createPointer("/m~0n").find(document));
    }

    @Test
    public void shouldGiveResultsShownInExampleInSpecificationUsingStaticMethod() {
        assertSame(document, JSONPointer.find("", document));
        assertEquals(array1, JSONPointer.find("/foo", document));
        assertEquals(string1, JSONPointer.find("/foo/0", document));
        assertEquals(new JSONInteger(0), JSONPointer.find("/", document));
        assertEquals(new JSONInteger(1), JSONPointer.find("/a~1b", document));
        assertEquals(new JSONInteger(2), JSONPointer.find("/c%d", document));
        assertEquals(new JSONInteger(3), JSONPointer.find("/e^f", document));
        assertEquals(new JSONInteger(4), JSONPointer.find("/g|h", document));
        assertEquals(new JSONInteger(5), JSONPointer.find("/i\\j", document));
        assertEquals(new JSONInteger(6), JSONPointer.find("/k\"l", document));
        assertEquals(new JSONInteger(7), JSONPointer.find("/ ", document));
        assertEquals(new JSONInteger(8), JSONPointer.find("/m~0n", document));
    }

    @Test
    public void shouldEscapeCorrectlyOnToString() {
        assertEquals("", createPointer("").toString());
        assertEquals("/foo", createPointer("/foo").toString());
        assertEquals("/foo/0", createPointer("/foo/0").toString());
        assertEquals("/", createPointer("/").toString());
        assertEquals("/a~1b", createPointer("/a~1b").toString());
        assertEquals("/c%d", createPointer("/c%d").toString());
        assertEquals("/e^f", createPointer("/e^f").toString());
        assertEquals("/g|h", createPointer("/g|h").toString());
        assertEquals("/i\\j", createPointer("/i\\j").toString());
        assertEquals("/ ", createPointer("/ ").toString());
        assertEquals("/m~0n", createPointer("/m~0n").toString());
    }

    @Test
    public void shouldCreateCorrectURIFragment() {
        assertEquals("#", createPointer("").toURIFragment());
        assertEquals("#/foo", createPointer("/foo").toURIFragment());
        assertEquals("#/foo/0", createPointer("/foo/0").toURIFragment());
        assertEquals("#/", createPointer("/").toURIFragment());
        assertEquals("#/a~1b", createPointer("/a~1b").toURIFragment());
        assertEquals("#/c%25d", createPointer("/c%d").toURIFragment());
        assertEquals("#/e%5Ef", createPointer("/e^f").toURIFragment());
        assertEquals("#/g%7Ch", createPointer("/g|h").toURIFragment());
        assertEquals("#/i%5Cj", createPointer("/i\\j").toURIFragment());
        assertEquals("#/k%22l", createPointer("/k\"l").toURIFragment());
        assertEquals("#/%20", createPointer("/ ").toURIFragment());
        assertEquals("#/m~0n", createPointer("/m~0n").toURIFragment());
        assertEquals("#/o%2Ap", createPointer("/o*p").toURIFragment());
        assertEquals("#/q%2Br", createPointer("/q+r").toURIFragment());
    }

    @Test
    public void shouldCorrectlyDecodeURIFragment() {
        assertEquals(createPointer(""), JSONPointer.fromURIFragment("#"));
        assertEquals(createPointer("/foo"), JSONPointer.fromURIFragment("#/foo"));
        assertEquals(createPointer("/foo/0"), JSONPointer.fromURIFragment("#/foo/0"));
        assertEquals(createPointer("/"), JSONPointer.fromURIFragment("#/"));
        assertEquals(createPointer("/a~1b"), JSONPointer.fromURIFragment("#/a~1b"));
        assertEquals(createPointer("/c%d"), JSONPointer.fromURIFragment("#/c%25d"));
        assertEquals(createPointer("/e^f"), JSONPointer.fromURIFragment("#/e%5Ef"));
        assertEquals(createPointer("/g|h"), JSONPointer.fromURIFragment("#/g%7Ch"));
        assertEquals(createPointer("/i\\j"), JSONPointer.fromURIFragment("#/i%5Cj"));
        assertEquals(createPointer("/k\"l"), JSONPointer.fromURIFragment("#/k%22l"));
        assertEquals(createPointer("/ "), JSONPointer.fromURIFragment("#/%20"));
        assertEquals(createPointer("/m~0n"), JSONPointer.fromURIFragment("#/m~0n"));
        assertEquals(createPointer("/o*p"), JSONPointer.fromURIFragment("#/o%2Ap"));
        assertEquals(createPointer("/q+r"), JSONPointer.fromURIFragment("#/q%2Br"));
    }

    @Test
    public void shouldTestWhetherPointerExistsOrNot() {
        assertTrue(createPointer("/foo").exists(document));
        assertTrue(createPointer("/foo/0").exists(document));
        assertTrue(createPointer("/foo/1").exists(document));
        assertFalse(createPointer("/foo/2").exists(document));
        assertFalse(createPointer("/fool").exists(document));
    }

    @Test
    public void shouldTestWhetherPointerExistsUsingStaticMethod() {
        assertTrue(JSONPointer.exists("/foo", document));
        assertTrue(JSONPointer.exists("/foo/0", document));
        assertTrue(JSONPointer.exists("/foo/1", document));
        assertFalse(JSONPointer.exists("/foo/2", document));
        assertFalse(JSONPointer.exists("/fool", document));
    }

    @Test
    public void shouldHandleNullObjectPropertiesCorrectly() {
        JSONObject object = new JSONObject();
        object.putValue("nonNullValue", "OK");
        object.putNull("nullValue");
        assertEquals(new JSONString("OK"), createPointer("/nonNullValue").find(object));
        assertEquals(new JSONString("OK"), JSONPointer.find("/nonNullValue", object));
        assertTrue(createPointer("/nonNullValue").exists(object));
        assertTrue(JSONPointer.exists("/nonNullValue", object));
        assertNull(createPointer("/nullValue").find(object));
        assertNull(JSONPointer.find("/nullValue", object));
        assertTrue(createPointer("/nullValue").exists(object));
        assertTrue(JSONPointer.exists("/nullValue", object));
    }

    @Test
    public void shouldHandleNullArrayItemsCorrectly() {
        JSONArray array = new JSONArray();
        array.addValue("OK");
        array.addNull();
        assertEquals(new JSONString("OK"), createPointer("/0").find(array));
        assertEquals(new JSONString("OK"), JSONPointer.find("/0", array));
        assertTrue(createPointer("/0").exists(array));
        assertTrue(JSONPointer.exists("/0", array));
        assertNull(createPointer("/1").find(array));
        assertNull(JSONPointer.find("/1", array));
        assertTrue(createPointer("/1").exists(array));
        assertTrue(JSONPointer.exists("/1", array));
    }

    @Test
    public void shouldNavigateCorrectlyToChild() {
        JSONPointer basePointer = createPointer("");
        assertSame(document, basePointer.eval(document));
        JSONPointer childPointer1 = basePointer.child("foo");
        assertEquals(array1, childPointer1.eval(document));
        JSONPointer childPointer2 = childPointer1.child(0);
        assertEquals(string1, childPointer2.eval(document));
        JSONPointer childPointer3 = childPointer1.child(1);
        assertEquals(string2, childPointer3.eval(document));
    }

    @Test
    public void shouldNavigateCorrectlyToParent() {
        JSONPointer startingPointer = createPointer("/foo/1");
        assertEquals(string2, startingPointer.eval(document));
        JSONPointer parentPointer1 = startingPointer.parent();
        assertEquals(array1, parentPointer1.eval(document));
        JSONPointer parentPointer2 = parentPointer1.parent();
        assertSame(document, parentPointer2.eval(document));
    }

    @Test
    public void shouldGiveCorrectErrorMessageOnBadReference() {
        JSONPointerException exception = assertThrows(JSONPointerException.class,
                () -> createPointer("/wrong/0").eval(document));
        assertEquals("Can't resolve JSON Pointer /wrong", exception.getMessage());
    }

    @Test
    public void shouldReturnValidRootPointer() {
        assertEquals(createPointer(""), JSONPointer.root);
    }

    @Test
    public void shouldNavigateNumericIndex() {
        assertEquals(new JSONString("A"), createPointer("/0").eval(testArray));
        assertEquals(new JSONString("B"), createPointer("/1").eval(testArray));
        assertEquals(new JSONString("K"), createPointer("/10").eval(testArray));
        assertEquals(new JSONString("P"), createPointer("/15").eval(testArray));
    }

    @Test
    public void shouldRejectInvalidNumericIndex() {
        JSONPointerException exception = assertThrows(JSONPointerException.class,
                () -> createPointer("/01").eval(testArray));
        assertEquals("Illegal array index in JSON Pointer /01", exception.getMessage());
        exception = assertThrows(JSONPointerException.class, () -> createPointer("/").eval(testArray));
        assertEquals("Illegal array index in JSON Pointer /", exception.getMessage());
        exception = assertThrows(JSONPointerException.class, () -> createPointer("/A").eval(testArray));
        assertEquals("Illegal array index in JSON Pointer /A", exception.getMessage());
        exception = assertThrows(JSONPointerException.class, () -> createPointer("/999999999").eval(testArray));
        assertEquals("Illegal array index in JSON Pointer /999999999", exception.getMessage());
        exception = assertThrows(JSONPointerException.class, () -> createPointer("/-1").eval(testArray));
        assertEquals("Illegal array index in JSON Pointer /-1", exception.getMessage());
        exception = assertThrows(JSONPointerException.class, () -> createPointer("/99").eval(testArray));
        assertEquals("Array index out of range in JSON Pointer /99", exception.getMessage());
    }

    @Test
    public void shouldGetCurrentToken() {
        assertEquals("second", createPointer("/first/second").getCurrent());
        assertEquals("first", createPointer("/first/second").parent().getCurrent());
        assertEquals("2", createPointer("/first/2").getCurrent());
        assertNull(JSONPointer.root.getCurrent());
    }

    @Test
    public void shouldReturnFalseForExistsWithNullBase() {
        assertFalse(JSONPointer.root.exists(null));
    }

    @Test
    public void shouldLocateChildInNestedObject() {
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        obj1.put("aaa", obj2);
        assertEquals(createPointer("/aaa"), JSONPointer.root.locateChild(obj1, obj2));
        JSONString str1 = new JSONString("xyz");
        obj2.put("bbb", str1);
        assertEquals(createPointer("/aaa/bbb"), JSONPointer.root.locateChild(obj1, str1));
        assertEquals(createPointer("/bbb"), JSONPointer.root.locateChild(obj2, str1));
        JSONInteger int1 = new JSONInteger(123);
        JSONInteger int2 = new JSONInteger(456);
        JSONArray array1 = new JSONArray(int1, int2);
        obj2.put("ccc", array1);
        assertEquals(createPointer("/aaa/ccc/1"), JSONPointer.root.locateChild(obj1, int2));
    }

    private static JSONPointer createPointer(String str) {
        return new JSONPointer(str);
    }

}
