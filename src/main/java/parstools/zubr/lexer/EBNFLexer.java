package parstools.zubr.lexer;

import java.util.*;

public class EBNFLexer {
    private String input;
    private int index;
    private int length;

    // Token type constants
    public static final int EOF = -1;
    public static final int IDENT = 1;
    public static final int STRING = 2;
    public static final int COLON = 3;
    public static final int PIPE = 4;
    public static final int SEMICOLON = 5;

    public EBNFLexer(String input) {
        this.input = input;
        this.index = 0;
        this.length = input.length();
    }

    public Token nextToken() {
        skipWhitespaceAndComments();
        if (index >= length) {
            return new Token(EOF, "");
        }

        char current = input.charAt(index);

        // Identifier
        if (isLetter(current)) {
            int start = index;
            while (index < length && isLetterOrDigit(input.charAt(index))) {
                index++;
            }
            String value = input.substring(start, index);
            return new Token(IDENT, value);
        }

        // String literal
        if (current == '\'') {
            index++; // Skip opening apostrophe
            int start = index;
            while (index < length && input.charAt(index) != '\'') {
                index++;
            }
            String value = input.substring(start, index);
            index++; // Skip closing apostrophe
            return new Token(STRING, value);
        }

        // Symbols
        switch (current) {
            case ':':
                index++;
                return new Token(COLON, ":");
            case '|':
                index++;
                return new Token(PIPE, "|");
            case ';':
                index++;
                return new Token(SEMICOLON, ";");
            default:
                throw new RuntimeException("Unexpected character: " + current);
        }
    }

    private void skipWhitespaceAndComments() {
        while (index < length) {
            char current = input.charAt(index);
            // Skip whitespace
            if (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
                index++;
                continue;
            }
            // Line comment
            if (current == '/' && index + 1 < length && input.charAt(index + 1) == '/') {
                index += 2;
                while (index < length && input.charAt(index) != '\n') {
                    index++;
                }
                continue;
            }
            // Block comment
            if (current == '/' && index + 1 < length && input.charAt(index + 1) == '*') {
                index += 2;
                while (index + 1 < length && !(input.charAt(index) == '*' && input.charAt(index + 1) == '/')) {
                    index++;
                }
                index += 2; // Skip closing */
                continue;
            }
            break;
        }
    }

    private boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private boolean isLetterOrDigit(char c) {
        return isLetter(c) || ('0' <= c && c <= '9');
    }

    // Token class to hold type and value
    public static class Token {
        public final int type;
        public final String value;

        public Token(int type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}