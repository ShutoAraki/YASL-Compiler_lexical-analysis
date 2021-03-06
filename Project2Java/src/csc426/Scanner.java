package csc426;

import java.io.IOException;
import java.io.Reader;

/**
 * A Lexical Analyzer for a subset of YASL. Uses a (Mealy) state machine to
 * extract the next available token from the input each time next() is called.
 * Input comes from a Reader, which will generally be a BufferedReader wrapped
 * around a FileReader or InputStreamReader (though for testing it may also be
 * simply a StringReader).
 * 
 * The documentation is available on README.md written by ShutoAraki.
 * 
 * @author bhoward
 * @student ShutoAraki
 */
public class Scanner {
	/**
	 * Construct the Scanner ready to read tokens from the given Reader.
	 * 
	 * @param in
	 */
	public Scanner(Reader in) {
		source = new Source(in);
	}

	/**
	 * Extract the next available token. When the input is exhausted, it will
	 * return an EOF token on all future calls.
	 * 
	 * @return the next Token object
	 */
	public Token next() {
		
		int state = 0;
		StringBuilder lexeme = new StringBuilder();
		int startLine = source.line;
		int startColumn = source.column;
		
		while (true) {
			switch (state) {
			case 0:
				if (source.atEOF) {
					return new Token(source.line, source.column, TokenType.EOF, null);
				} else if (source.current == '0') {
					startLine = source.line;
					startColumn = source.column;
					lexeme.append(source.current);
					source.advance();
					state = 1;
				} else if (Character.isDigit(source.current)) {
					startLine = source.line;
					startColumn = source.column;
					lexeme.append(source.current);
					source.advance();
					state = 2;
				} else if (Character.isAlphabetic(source.current)) {
					startLine = source.line;
					startColumn = source.column;
					lexeme.append(source.current);
					source.advance();
					state = 3;
				} else if (source.current == '/') { // For comments
					state = 4;
					source.advance();
				} else if (source.current == '+') {
					startLine = source.line;
					startColumn = source.column;
					state = 8;
					source.advance();
				} else if (source.current == '-') {
					startLine = source.line;
					startColumn = source.column;
					state = 9;
					source.advance();
				} else if (source.current == '*') {
					startLine = source.line;
					startColumn = source.column;
					state = 10;
					source.advance();
				} else if (source.current == ';') {
					startLine = source.line;
					startColumn = source.column;
					state = 11;
					source.advance();
				} else if (source.current == '.') {
					startLine = source.line;
					startColumn = source.column;
					state = 12;
					source.advance();
				} else if (source.current == '=') {
					startLine = source.line;
					startColumn = source.column;
					state = 13;
					source.advance();
				} else if (Character.isWhitespace(source.current)) {
					source.advance();
				} else {
					System.err.println("Unexpected character '" + source.current + "' at line " + source.line + " column " + source.column + " was skipped.");
					source.advance();
				}
				break;
				
			case 1:
				return new Token(startLine, startColumn, TokenType.NUM, lexeme.toString());
				
			case 2:
				if (source.atEOF || !Character.isDigit(source.current)) {
					return new Token(startLine, startColumn, TokenType.NUM, lexeme.toString());
				} else {
					lexeme.append(source.current);
					source.advance();
				}
				break;
			case 3:
				char[] end_chars = {'+', '-', '*', ';', '.', '=', '/', ' ', '\t','\n'};
				
				if (Character.isAlphabetic(source.current) || Character.isDigit(source.current) || source.current == '_') {
					lexeme.append(source.current);
					source.advance();
				} else if (new String(end_chars).contains(String.valueOf(source.current))) {
					if (lexeme.toString().equals("program"))
						return new Token(startLine, startColumn, TokenType.PROGRAM, null);
					else if (lexeme.toString().equals("print"))
						return new Token(startLine, startColumn, TokenType.PRINT, null);
					else if (lexeme.toString().equals("mod"))
						return new Token(startLine, startColumn, TokenType.MOD, null);
					else if (lexeme.toString().equals("div"))
						return new Token(startLine, startColumn, TokenType.DIV, null);
					else if (lexeme.toString().equals("val"))
						return new Token(startLine, startColumn, TokenType.VAL, null);
					else if (lexeme.toString().equals("begin"))
						return new Token(startLine, startColumn, TokenType.BEGIN, null);
					else if (lexeme.toString().equals("end"))
						return new Token(startLine, startColumn, TokenType.END, null);
					else
						return new Token(startLine, startColumn, TokenType.ID, lexeme.toString());	
				} else {
					System.err.println("Unexpected character '" + source.current + "' at line " + source.line + " column " + source.column + " was skipped.");
					source.advance();
				}
				break;
			case 4:
				if (source.current == '/') {
					state = 5;
					source.advance();
				}
				else if (source.current == '*') {
					state = 6;
					source.advance();
				}
				else {
					System.err.println("You have to use // for a one-line comment.");
					state = 0;
				}
				break;
			case 5:
				if (source.current == '\n')
					state = 0;
				source.advance();
				break;
			case 6:
				if (source.current == '*')
					state = 7;
				else if (source.atEOF) {
					System.err.println("Comment has to be closed with '*/' before EOF");
					state = 0;
				}
				source.advance();
				break;
			case 7:
				if (source.current == '/')
					state = 0;
				else if (source.current == '*')
					state = 7;
				else if (source.atEOF) {
					System.err.println("Comment has to be closed with '*/' before EOF");
					state = 0;
				} else
					state = 6;
				source.advance();
				break;
			case 8:
				return new Token(startLine, startColumn, TokenType.PLUS, null);
			case 9:
				return new Token(startLine, startColumn, TokenType.MINUS, null);
			case 10:
				return new Token(startLine, startColumn, TokenType.STAR, null);
			case 11:
				return new Token(startLine, startColumn, TokenType.SEMI, null);
			case 12:
				return new Token(startLine, startColumn, TokenType.PERIOD, null);
			case 13:
				return new Token(startLine, startColumn, TokenType.ASSIGN, null);
			default:
				// This part will NOT be executed. The error will be thrown just in case.
				throw new RuntimeException("Unreachable. Something is wrong with the lexer.");
			}
		}
	}

	/**
	 * Close the underlying Reader.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		source.close();
	}

	private Source source;
}
