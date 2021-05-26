/*
 * @(#) JSONReference.java
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

package net.pwall.json.pointer;

import java.util.Objects;

import net.pwall.json.JSONMapping;
import net.pwall.json.JSONSequence;
import net.pwall.json.JSONValue;

/**
 * A JSON Reference - a combination of a JSON Pointer and the JSON value to which it refers.  This allows for a single
 * object to be used (and passed as a parameter between functions) in the common case of a pointer being employed to
 * navigate a tree of JSON values.
 *
 * The class is implemented as a derived class from {@link JSONPointer} - this is an optimisation to reduce object
 * allocation.
 *
 * @author  Peter Wall
 */
public class JSONReference extends JSONPointer {

    private final JSONValue base;
    private final boolean valid;
    private final JSONValue value;

    /**
     * Private constructor (used by {@link #child(int)}/{@link #child(String)} methods).
     *
     * @param   base    the base {@link JSONValue}
     * @param   tokens  a list of tokens representing the pointer
     * @param   valid   {@code true} if the value is valid
     * @param   value   the value pointed to by the pointer
     */
    private JSONReference(JSONValue base, String[] tokens, boolean valid, JSONValue value) {
        super(tokens);
        this.base = base;
        this.valid = valid;
        this.value = value;
    }

    /**
     * Private constructor (used by {@link #parent()} method).
     *
     * @param   base    the base {@link JSONValue}
     * @param   tokens  a list of tokens representing the pointer
     */
    private JSONReference(JSONValue base, String[] tokens) {
        super(tokens);
        this.base = base;
        if (exists(base)) {
            valid = true;
            value = eval(base);
        }
        else {
            valid = false;
            value = null;
        }
    }

    /**
     * Create a JSON Reference using the given base JSON value and pointer.
     *
     * @param   base    the base {@link JSONValue}
     * @param   pointer a {@link JSONPointer} to a node within the base value
     */
    public JSONReference(JSONValue base, JSONPointer pointer) {
        this(base, pointer.getTokens());
    }

    /**
     * Create a JSON Reference using the given base JSON value and a pointer in string form.
     *
     * @param   base    the base {@link JSONValue}
     * @param   string  a string representing a JSON Pointer
     * @throws          JSONPointerException if the string is not either an empty string, or starts with "/"
     */
    public JSONReference(JSONValue base, String string) {
        this(base, parse(string));
    }

    /**
     * Create a JSON Reference using the given base JSON value and a root pointer.
     * @param   base    the base {@link JSONValue}
     */
    public JSONReference(JSONValue base) {
        this(base, emptyArray, true, base);
    }

    /**
     * Get the pointer component of the reference.
     *
     * Note that since this class derives from {@link JSONPointer}, then provided the virtual functions (e.g.
     * {@code toString()}) are not required, the object may simply be cast to a {@link JSONPointer}.
     *
     * @return  the pointer
     */
    @Override
    public JSONPointer getPointer() {
        return new JSONPointer(getTokens());
    }

    /**
     * Get the base {@link JSONValue}.
     *
     * @return  the {@link JSONValue}
     */
    public JSONValue getBase() {
        return base;
    }

    /**
     * Test whether the reference is valid, i.e. the pointer points to a valid location within the base.
     *
     * @return  {@code true} iff the reference is valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the value referred to by this reference.
     *
     * @return  the value, or {@code null} if the reference is not valid
     */
    public JSONValue getValue() {
        return value;
    }

    /**
     * Test whether the reference has the nominated child (by name).
     *
     * @param name  the name of the child
     * @return      {@code true} iff that child exists
     */
    public boolean hasChild(String name) {
        return valid && value instanceof JSONMapping && ((JSONMapping<?>)value).containsKey(name);
    }

    /**
     * Test whether the reference has the nominated child (by index).
     *
     * @param index the index of the child
     * @return      {@code true} iff that child exists
     */
    public boolean hasChild(int index) {
        return valid && ((value instanceof JSONSequence && index >= 0 && index < ((JSONSequence<?>)value).size()) ||
                (value instanceof JSONMapping && ((JSONMapping<?>)value).containsKey(Integer.toString(index))));
    }

    /**
     * Navigate to the parent of the currently-addressed element.
     *
     * @return  a reference that refers to the parent element
     * @throws  JSONPointerException on any attempt to get the parent of the root element
     */
    @Override
    public JSONReference parent() {
        String[] tokens = getTokens();
        int n = tokens.length;
        if (n == 0)
            throw new JSONPointerException("Can't get parent of root JSON Pointer");
        String[] newTokens = new String[--n];
        System.arraycopy(tokens, 0, newTokens, 0, n);
        return new JSONReference(base, newTokens);
    }

    /**
     * Navigate to the named child of the currently-addressed element.
     *
     * @param name  the name of the child
     * @return      a reference that refers to the child element (possibly not valid)
     */
    @Override
    public JSONReference child(String name) {
        String[] tokens = getTokens();
        int n = tokens.length;
        String[] newTokens = new String[n + 1];
        System.arraycopy(tokens, 0, newTokens, 0, n);
        newTokens[n] = name;
        if (valid && value instanceof JSONMapping) {
            JSONMapping<?> mapping = (JSONMapping<?>)value;
            if (mapping.containsKey(name))
                return new JSONReference(base, newTokens, true, mapping.get(name));
        }
        return new JSONReference(base, newTokens, false, null);
    }

    /**
     * Navigate to the numbered child of the currently-addressed element (must be an array).
     *
     * @param index the index of the child
     * @return      a reference that refers to the child element (possibly not valid)
     * @throws      JSONPointerException if the index is negative
     */
    @Override
    public JSONReference child(int index) {
        if (index < 0)
            throw new JSONPointerException("JSON Pointer index must not be negative");
        String[] tokens = getTokens();
        int n = tokens.length;
        String[] newTokens = new String[n + 1];
        System.arraycopy(tokens, 0, newTokens, 0, n);
        newTokens[n] = Integer.toString(index);
        if (valid) {
            if (value instanceof JSONSequence) {
                JSONSequence<?> sequence = (JSONSequence<?>)value;
                if (index < sequence.size())
                    return new JSONReference(base, newTokens, true, sequence.get(index));
            }
            else if (value instanceof JSONMapping) {
                String name = Integer.toString(index);
                JSONMapping<?> mapping = (JSONMapping<?>)value;
                if (mapping.containsKey(name))
                    return new JSONReference(base, newTokens, true, mapping.get(name));
            }
        }
        return new JSONReference(base, newTokens, false, null);
    }

    /**
     * Compare two JSON references for equality.  The references are equal only if the pointers are equal and the base
     * values refer to the <b>same</b> object.
     *
     * @param   other   the other object for comparison
     * @return          {@code true} iff the objects are equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof JSONReference) || !super.equals(other))
            return false;
        JSONReference otherRef = (JSONReference)other;
        return base == otherRef.base && valid == otherRef.valid && value == otherRef.value;
    }

    /**
     * Get the hash code for the reference.
     *
     * @return  the hash code
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(base) ^ (valid ? 1 : 0) ^ Objects.hashCode(value);
    }

    /**
     * Get a string representation of the reference.
     *
     * @return  a string representation of the value (or "invalid" if the reference is not valid)
     */
    @Override
    public String toString() {
        return valid ? value == null ? "null" : value.toJSON() : "invalid";
    }

}
