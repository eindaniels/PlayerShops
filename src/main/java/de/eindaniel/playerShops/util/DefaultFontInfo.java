package de.eindaniel.playerShops.util;

public enum DefaultFontInfo {
    A('A', 5), a('a', 5), B('B', 5), b('b', 5), C('C', 5), c('c', 5), D('D', 5), d('d', 5),
    E('E', 5), e('e', 5), F('F', 5), f('f', 4), G('G', 5), g('g', 5), H('H', 5), h('h', 5),
    I('I', 3), i('i', 1), J('J', 5), j('j', 5), K('K', 5), k('k', 4), L('L', 5), l('l', 2),
    M('M', 5), m('m', 5), N('N', 5), n('n', 5), O('O', 5), o('o', 5), P('P', 5), p('p', 5),
    Q('Q', 5), q('q', 5), R('R', 5), r('r', 5), S('S', 5), s('s', 5), T('T', 5), t('t', 4),
    U('U', 5), u('u', 5), V('V', 5), v('v', 5), W('W', 5), w('w', 5), X('X', 5), x('x', 5),
    Y('Y', 5), y('y', 5), Z('Z', 5), z('z', 5), SPACE(' ', 4), EXCLAMATION('!', 1),
    AT('@', 6), NUM('#', 5), DOLLAR('$', 5), PERCENT('%', 5), CARET('^', 5), AMPERSAND('&', 5),
    STAR('*', 5), OPEN_PAREN('(', 4), CLOSE_PAREN(')', 4), MINUS('-', 3), UNDERSCORE('_', 5),
    PLUS('+', 5), EQUALS('=', 5), OPEN_BRACE('{', 4), CLOSE_BRACE('}', 4), OPEN_BRACKET('[', 3),
    CLOSE_BRACKET(']', 3), PIPE('|', 1), BACKSLASH('\\', 5), COLON(':', 1), SEMICOLON(';', 1),
    DOUBLE_QUOTE('"', 3), SINGLE_QUOTE('\'', 1), LESS('<', 5), GREATER('>', 5), QUESTION('?', 5),
    SLASH('/', 5), TILDE('~', 5), BACKTICK('`', 2), COMMA(',', 1), PERIOD('.', 1),
    ZERO('0', 5), ONE('1', 5), TWO('2', 5), THREE('3', 5), FOUR('4', 5), FIVE('5', 5),
    SIX('6', 5), SEVEN('7', 5), EIGHT('8', 5), NINE('9', 5),
    DEFAULT('a', 5);

    private final char character;
    private final int length;

    DefaultFontInfo(char character, int length) {
        this.character = character;
        this.length = length;
    }

    public char getCharacter() { return character; }
    public int getLength() { return length; }

    public static DefaultFontInfo getByChar(char c) {
        for (DefaultFontInfo dfi : values()) {
            if (dfi.character == c) return dfi;
        }
        // default for unknown chars
        return DEFAULT;
    }
}