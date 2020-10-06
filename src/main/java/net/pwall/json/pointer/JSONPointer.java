/*
 * @(#) JSONPointer.java
 *
 * json-pointer  Java implementation of JSON Pointer
 * Copyright (c) 2020 Peter Wall
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
import java.util.function.Supplier;

import net.pwall.json.JSONMapping;
import net.pwall.json.JSONSequence;
import net.pwall.json.JSONValue;
import net.pwall.util.CharMapper;
import net.pwall.util.CharUnmapper;
import net.pwall.util.Strings;
import net.pwall.util.URI;

/**
 * JSON Pointer.
 *
 * @author  Peter Wall
 */
public class JSONPointer {

    private static final String[] emptyArray = new String[0];

    public static final JSONPointer root = new JSONPointer(emptyArray);

    private final String[] tokens;

    /**
     * Private constructor (used by {@link #parent()} and {@link #child(int)}/{@link #child(String)} methods.
     *
     * @param   tokens  a list of tokens
     */
    private JSONPointer(String[] tokens) {
        this.tokens = Objects.requireNonNull(tokens);
    }

    /**
     * Main constructor - creates a {@code JSONPointer} using the specified path.
     *
     * @param   string      the path
     * @throws              JSONPointerException if the string does not start with a "/"
     */
    public JSONPointer(String string) {
        this(parse(Objects.requireNonNull(string)));
    }

    /**
     * Evaluate a JSON Pointer against a given JSON value.
     *
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          the value at that point (may be {@code null})
     * @throws          JSONPointerException if there are any errors in navigation
     */
    public JSONValue eval(JSONValue base) {
        return find(tokens, base);
    }

    /**
     * Find the value corresponding to the pointer in the given JSON value.
     *
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          the value at that point (may be {@code null})
     * @throws          JSONPointerException if there are any errors in navigation
     */
    public JSONValue find(JSONValue base) {
        return find(tokens, base);
    }

    /**
     * Test whether a value exists at a given path.
     *
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          {@code true} if the value exists (including if it is {@code null})
     */
    public boolean exists(JSONValue base) {
        return exists(tokens, base);
    }

    /**
     * Get a pointer to the parent of the currently-addressed element.
     *
     * @return      a pointer to the parent element
     * @throws      JSONPointerException on any attempt to get the parent of the root element
     */
    public JSONPointer parent() {
        if (tokens.length == 0)
            throw new JSONPointerException("Can't get parent of root JSON Pointer");
        int n = tokens.length - 1;
        String[] newTokens = new String[n];
        System.arraycopy(tokens, 0, newTokens, 0, n);
        return new JSONPointer(newTokens);
    }

    /**
     * Get a pointer to the named child of the currently-addressed element.
     *
     * @param   string      the name of the child
     * @return              a pointer to the child element
     */
    public JSONPointer child(String string) {
        Objects.requireNonNull(string);
        String[] newTokens = new String[tokens.length + 1];
        System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
        newTokens[tokens.length] = string;
        return new JSONPointer(newTokens);
    }

    /**
     * Get a pointer to the numbered child of the currently-addressed element (must be an array).
     *
     * @param   index       the index of the child
     * @return              a pointer to the child element
     * @throws              JSONPointerException if the index is negative
     */
    public JSONPointer child(int index) {
        if (index < 0)
            throw new JSONPointerException("JSON Pointer index must not be negative");
        return child(String.valueOf(index));
    }

    /**
     * Convert the pointer to a URI fragment.
     *
     * @return      the URI fragment
     */
    public String toURIFragment() {
        StringBuilder sb = new StringBuilder();
        sb.append('#');
        for (String token : tokens)
            sb.append('/').append(encodeURI(escapeToken(token)));
        return sb.toString();
    }

    /**
     * Compare two pointers for equality.
     *
     * @param   obj     the other pointer
     * @return          {@code true} if the pointers are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof JSONPointer))
            return false;
        String[] otherTokens = ((JSONPointer)obj).tokens;
        int n = tokens.length;
        if (n != otherTokens.length)
            return false;
        for (int i = 0; i < n; i++)
            if (!tokens[i].equals(otherTokens[i]))
                return false;
        return true;
    }

    /**
     * Get the hash code for the pointer.
     *
     * @return      the hash code
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (String token : tokens)
            hash ^= token.hashCode();
        return hash;
    }

    /**
     * Get a string representation of the pointer.
     *
     * @return      the pointer as a string
     */
    @Override
    public String toString() {
        return toString(tokens, tokens.length);
    }

    /**
     * Find the value corresponding to the pointer (as a {@link String}) in the given JSON value.
     *
     * @param   string  the pointer as a string
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          the value at that point (may be {@code null})
     * @throws          JSONPointerException if there are any errors in navigation
     */
    public static JSONValue find(String string, JSONValue base) {
        return find(parse(Objects.requireNonNull(string)), base);
    }

    /**
     * Find the value corresponding to the pointer (as an array of pointer elements) in the given JSON value.
     *
     * @param   tokens  the array of pointer elements
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          the value at that point (may be {@code null})
     * @throws          JSONPointerException if there are any errors in navigation
     */
    private static JSONValue find(String[] tokens, JSONValue base) {
        JSONValue result = base;
        for (int i = 0, n = tokens.length; i < n; i++) {
            String token = tokens[i];
            if (result instanceof JSONMapping) {
                JSONMapping<?> resultMapping = (JSONMapping<?>)result;
                if (!resultMapping.containsKey(token))
                    error(tokens, i + 1);
                result = resultMapping.get(token);
            }
            else if (result instanceof JSONSequence) {
                JSONSequence<?> resultSequence = (JSONSequence<?>)result;
                final int ii = i + 1;
                if (token.equals("-"))
                    throw new JSONPointerException("Can't dereference end-of-array JSON Pointer " +
                            toString(tokens, ii));
                int index = checkIndex(token, () -> "Illegal array index in JSON Pointer " + toString(tokens, ii));
                if (index < 0 || index >= resultSequence.size())
                    throw new JSONPointerException("Array index out of range in JSON Pointer " + toString(tokens, ii));
                result = resultSequence.get(index);
            }
            else
                error(tokens, i + 1);
        }
        return result;
    }

    /**
     * Test whether a value exists at a given path (as a {@link String}).
     *
     * @param   string  the pointer as a string
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          {@code true} if the value exists (including if it is {@code null})
     */
    public static boolean exists(String string, JSONValue base) {
        return exists(parse(Objects.requireNonNull(string)), base);
    }

    /**
     * Test whether a value exists at a given path (as an array of pointer elements).
     *
     * @param   tokens  the array of pointer elements
     * @param   base    the base value (path elements will be used to navigate from this point)
     * @return          {@code true} if the value exists (including if it is {@code null})
     */
    private static boolean exists(String[] tokens, JSONValue base) {
        JSONValue current = base;
        for (String token : tokens) {
            if (current instanceof JSONMapping) {
                JSONMapping<?> currentMapping = (JSONMapping<?>)current;
                if (!currentMapping.containsKey(token))
                    return false;
                current = currentMapping.get(token);
            }
            else if (current instanceof JSONSequence) {
                JSONSequence<?> currentSequence = (JSONSequence<?>)current;
                if (!checkNumber(token))
                    return false;
                int index = Integer.parseInt(token);
                if (index < 0 || index >= currentSequence.size())
                    return false;
                current = currentSequence.get(index);
            }
            else
                return false;
        }
        return true;
    }

    /**
     * Get a string representation of a limited portion of the pointer (for error reporting).
     *
     * @param   n       the number of element to be included
     * @return          the string representation of that portion of the pointer
     */
    private static String toString(String[] tokens, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
            sb.append('/').append(escapeToken(tokens[i]));
        return sb.toString();
    }

    /**
     * Report an error.
     *
     * @param   tokenIndex      the index of the pointer element in error
     */
    private static void error(String[] tokens, int tokenIndex) {
        throw new JSONPointerException("Can't resolve JSON Pointer " + toString(tokens, tokenIndex));
    }

    /**
     * Check a number using the rules in the specification.
     *
     * @param   token   the number as a string
     * @return          {@code true} if the string is a valid number
     */
    private static boolean checkNumber(String token) {
        int n = token.length();
        if (n < 1 || n > 8)
            return false;
        char ch = token.charAt(0);
        if (ch == '0')
            return n == 1;
        int i = 1;
        while (true) {
            if (ch < '0' || ch > '9')
                return false;
            if (i >= n)
                break;
            ch = token.charAt(i++);
        }
        return true;
    }

    /**
     * Check a number and convert to an integer.
     *
     * @param   token           the number as a string
     * @param   lazyMessage     a function to create a message lazily (the message may be complex)
     * @return                  the number as an integer
     * @throws                  JSONPointerException if the number is not valid
     */
    private static int checkIndex(String token, Supplier<String> lazyMessage) {
        if (!checkNumber(token))
            throw new JSONPointerException(lazyMessage.get());
        return Integer.parseInt(token);
    }

    /**
     * Parse a pointer string into an array of pointer elements.
     *
     * @param   string      the original string
     * @return              the array of elements
     * @throws              JSONPointerException if the string does not start with a "/"
     */
    private static String[] parse(String string) {
        if (Objects.requireNonNull(string).length() == 0)
            return emptyArray;
        if (!string.startsWith("/"))
            throw new JSONPointerException("Illegal JSON Pointer " + string);
        String[] tokens = Strings.split(string, 1, string.length(), '/', false, null);
        for (int i = 0, n = tokens.length; i < n; i++)
            tokens[i] = unescapeToken(tokens[i]);
        return tokens;
    }

    /**
     * Encode a string for use in a URI.  This conversion uses "%20" to encode spaces rather than "+", because that is
     * the form used in the JSON Pointer specification.
     *
     * @param   str     the input string
     * @return          the encoded string
     */
    public static String encodeURI(String str) {
        String result = URI.escape(str);
        if (result.indexOf('+') < 0)
            return result;
        return Strings.join(Strings.split(result, '+', false, null), "%20");
    }

    /**
     * Create a {@code JSONPointer} from a URI fragment.
     *
     * @param   fragment    the URI fragment
     * @return              the pointer
     * @throws              JSONPointerException if the fragment is illegal (does not start with "#/")
     */
    public static JSONPointer fromURIFragment(String fragment) {
        if (!fragment.startsWith("#"))
            throw new JSONPointerException("Illegal URI fragment " + fragment);
        return new JSONPointer(URI.unescape(fragment.substring(1)));
    }

    /**
     * Escape a token using the JSON Pointer escaping rules.
     *
     * @param   token   the token
     * @return          the escaped token
     */
    public static String escapeToken(String token) {
        return Strings.escape(token, mapper);
    }

    /**
     * Unescape a token using the JSON Pointer escaping rules.
     *
     * @param   str     the escaped token
     * @return          the unescaped token
     */
    public static String unescapeToken(String str) {
        return Strings.unescape(str, unmapper);
    }

    /**
     * A {@link CharMapper} to assist with escaping JSON Pointers.
     */
    public static final CharMapper mapper = codePoint -> {
        if (codePoint == '~')
            return "~0";
        if (codePoint == '/')
            return "~1";
        return null;
    };

    /**
     * A {@link CharUnmapper} to assist with unescaping JSON Pointers.
     */
    public static final CharUnmapper unmapper = new CharUnmapper() {

        @Override
        public boolean isEscape(CharSequence s, int offset) {
            return s.charAt(offset) == '~';
        }

        @Override
        public int unmap(StringBuilder sb, CharSequence s, int offset) {
            int nextIndex = offset + 1;
            if (nextIndex < s.length()) {
                char ch = s.charAt(nextIndex);
                if (ch == '0') {
                    sb.append('~');
                    return 2;
                }
                if (ch == '1') {
                    sb.append('/');
                    return 2;
                }
            }
            sb.append('~');
            return 1;
        }

    };

}
