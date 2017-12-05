/*
 * @(#)Token.java                        2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package Triangle.SyntacticAnalyzer;

final class Token extends Object {

    protected int kind;
    protected String spelling;
    protected SourcePosition position;

    public Token(int kind, String spelling, SourcePosition position) {

        if (kind == Token.IDENTIFIER) {
            int currentKind = firstReservedWord;
            boolean searching = true;

            while (searching) {
                int comparison = tokenTable[currentKind].compareTo(spelling);
                if (comparison == 0) {
                    this.kind = currentKind;
                    searching = false;
                } else if (comparison > 0 || currentKind == lastReservedWord) {
                    this.kind = Token.IDENTIFIER;
                    searching = false;
                } else {
                    currentKind++;
                }
            }
        } else
            this.kind = kind;

        this.spelling = spelling;
        this.position = position;

    }

    public static String spell(int kind) {
        return tokenTable[kind];
    }

    public String toString() {
        return "Kind=" + kind + ", spelling=" + spelling +
                ", position=" + position;
    }

    // Token classes...

    public static final int

            // literals, identifiers, operators...
            INTLITERAL = 0,
            CHARLITERAL = 1,
            IDENTIFIER = 2,
            OPERATOR = 3,
            FIXEDSTRING = 4,
            DYNAMICSTRING = 5,

            // reserved words - must be in alphabetical order...
            ARRAY = 6,
            BEGIN = 7,
            CASE = 8,
            CONST = 9,
            DO = 10,
            ELSE = 11,
            END = 12,
            ENUM = 13,
            FOR = 14,
            FROM = 15,
            FUNC = 16,
            IF = 17,
            IN = 18,
            LET = 19,
            NIL = 20,
            OF = 21,
            OUT = 22,
            PACKAGE = 23,
            PRED = 24,
            PROC = 25,
            REC = 26,
            RECORD = 27,
            REPEAT = 28,
            STRING = 29,
            SUCC = 30,
            THEN = 31,
            TO = 32,
            TYPE = 33,
            UNTIL = 34,
            VAR = 35,
            WHERE = 36,
            WHILE = 37,

            // punctuation...
            DOT = 121,
            COLON = 122,
            SEMICOLON = 123,
            COMMA = 124,
            BECOMES = 125,
            IS = 126,

            // brackets...
            LPAREN = 227,
            RPAREN = 228,
            LBRACKET = 229,
            RBRACKET = 230,
            LCURLY = 231,
            RCURLY = 232,
            CASH = 233,

            // special tokens...
            EOT = 333,
            ERROR = 334;

    private static String[] tokenTable = new String[]{
            "<int>",
            "<char>",
            "<identifier>",
            "<operator>",
            "<fixedstring>",
            "<dynamicstring>",
            "array",
            "begin",
            "case",
            "const",
            "do",
            "else",
            "end",
            "enum",
            "for",
            "from",
            "func",
            "if",
            "in",
            "let",
            "nil",
            "of",
            "out",
            "package",
            "pred",
            "proc",
            "rec",
            "record",
            "repeat",
            "string",
            "succ",
            "then",
            "to",
            "type",
            "until",
            "var",
            "where",
            "while",
            ".",
            ":",
            ";",
            ",",
            ":=",   // [2003.04.22 ruys] added missing BECOMES token (reported and fixed by Ingo Wassink)
            "~",
            "(",
            ")",
            "[",
            "]",
            "{",
            "}",
            "$",
            "",
            "<error>"
    };

    private final static int firstReservedWord = Token.ARRAY,
            lastReservedWord = Token.WHILE;

}
