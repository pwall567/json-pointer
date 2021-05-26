package net.pwall.json.pointer.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pwall.json.JSON;
import net.pwall.json.JSONInteger;
import net.pwall.json.JSONMapping;
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

}
