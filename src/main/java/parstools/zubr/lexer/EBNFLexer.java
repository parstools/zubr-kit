package parstools.zubr.lexer;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class EBNFLexer {
    public enum Mode {
        SIMPLE,
        NORMAL
    }
    private final String input;
    private int index;
    private final int length;
    private final Mode mode;
    private Queue<Token> cache = new LinkedList<>();

    // Token type constants
    public static final int EOF = -1;
    public static final int IDENT = 1;
    public static final int STRING = 2;
    public static final int COLON = 3;
    public static final int PIPE = 4;
    public static final int SEMICOLON = 5;
    public static final int QUESTION = 6;
    public static final int STAR = 7;
    public static final int PLUS = 8;
    public static final int LPAREN = 9;
    public static final int RPAREN = 10;

    public EBNFLexer(String input, Mode mode) {
        this.input = input;
        this.mode = mode;
        this.index = 0;
        this.length = input.length();
    }

    public Token peek() {
        if (cache.isEmpty())
            cache.offer(nextTokenPrivate());
        return cache.element();
    }

    public void consume() {
        nextToken();
    }

    public Token nextToken() {
        if (cache.isEmpty())
            cache.offer(nextTokenPrivate());
        return cache.remove();
    }

    private Token nextTokenPrivate() {
        skipWhitespaceAndComments();
        if (index >= length) {
            return new Token(EOF, "", length);
        }

        char current = input.charAt(index);

        // Identifier
        if (isLetter(current)) {
            int start = index;
            if (mode == Mode.NORMAL) {
                while (index < length && isLetterOrDigit(input.charAt(index))) {
                    index++;
                }
            } else {
                index++;
            }
            String value = input.substring(start, index);
            return new Token(IDENT, value, start);
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
            return new Token(STRING, value, start);
        }

        // Symbols
        switch (current) {
            case ':':
                index++;
                return new Token(COLON, ":", index-1);
            case '|':
                index++;
                return new Token(PIPE, "|", index-1);
            case ';':
                index++;
                return new Token(SEMICOLON, ";", index-1);
            case '?':
                index++;
                return new Token(QUESTION, "?", index-1);
            case '*':
                index++;
                return new Token(STAR, "*", index-1);
            case '+':
                index++;
                return new Token(PLUS, "+", index-1);
            case '(':
                index++;
                return new Token(LPAREN, "(", index-1);
            case ')':
                index++;
                return new Token(RPAREN, ")", index-1);
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
        public final int index;

        public Token(int type, String value, int index) {
            this.type = type;
            this.value = value;
            this.index = index;
        }
    }
}