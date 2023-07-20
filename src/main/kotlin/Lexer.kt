fun lex(sourceCode: String): List<Token> {
	val tokens = mutableListOf<Token>()

	var index = 0
	while (index < sourceCode.length) {
		val character = sourceCode[index]
		if (character.isWhitespace()) {
			index++
			continue
		}

		// comment
		if (character == '#') {
			// identifies the end of the comment
			val indexOfNext = sourceCode.substring(index, sourceCode.length).indexOf("\n")
			index += indexOfNext
			continue
		}

		var tokenType: TT?
		var tokenValue: String? = null

		// ignores minus in front of numbers
		if (character == '-') {
			index++
			continue
		}

		// checks for single-digit tokens
		tokenType = when (character) {
			'(' -> TT.OPEN
			')' -> TT.CLOSE
			'[' -> TT.OPEN_SQUARE
			']' -> TT.CLOSE_SQUARE
			'{' -> TT.OPEN_CURLY
			'}' -> TT.CLOSE_CURLY
			',' -> TT.COMMA
			// ';' or ':3' work as end line characters
			';' -> TT.SEMICOLON
			// if it's not found here it is unidentified (until it is hopefully identified)
			else -> TT.UNIDENTIFED
		}

		// checks for '<-'
		if (index + 1 < sourceCode.length &&
			character == '<' && sourceCode[index + 1] == '-'
		) {
			tokenType = TT.ASSIGNER
			// (2 characters so an extra increment needed)
			index++
		}

		// checks for :3
		if (index + 1< sourceCode.length &&
				character == ':' && sourceCode[index + 1] == '3'
		) {
			tokenType = TT.SEMICOLON
			index++
		}

		// checks for regular characters
		if (index < sourceCode.length - 2 &&
			character == '\'' && sourceCode[index + 2] == '\''
		) {
			tokenType = TT.CHAR
			tokenValue = sourceCode[index + 1].toString()
			index += 2
		}

		// checks for special characters
		if (index < sourceCode.length - 3 &&
			character == '\'' && sourceCode[index + 1] == '\\' && sourceCode[index + 3] == '\''
		) {
			tokenValue = when (sourceCode[index + 2]) {
				't' -> "\t"
				'b' -> "\b"
				'n' -> "\n"
				'r' -> "\r"
				'\'' -> "\'"
				'"' -> "\""
				'\\' -> "\\"
				else -> null
			}

			if (tokenValue != null) {
				tokenType = TT.CHAR
			}
		}

		tokenType = if (character.isLetter()) {
			// this means it could either be a tag or name
			var handle = "" + character
			var lookAheadIndex = index + 1
			var lookAheadCharacter: Char
			while (lookAheadIndex < sourceCode.length) {
				lookAheadCharacter = sourceCode[lookAheadIndex]
				if ("$lookAheadCharacter".matches("\\w".toRegex())) {
					// [a-zA-Z0-9_] character
					handle += lookAheadCharacter
					lookAheadIndex++
				} else break
			}

			// change index to the index before the lookAheadIndex
			// (as index++ is at the end)
			index = --lookAheadIndex
			tokenValue = handle
			when (handle) {
				"f" -> TT.F_TAG
				"if" -> TT.IF_TAG
				"else" -> TT.ELSE_TAG
				"while" -> TT.WHILE_TAG
				"main" -> TT.MAIN_TAG
				else -> TT.NAME
			}
		} else {
			tokenType
		}

		// string literal
		if (character == '"') {
			var handle = ""
			var lookAheadIndex = index + 1
			if (lookAheadIndex < sourceCode.length && sourceCode[lookAheadIndex] == '"') {
				index = lookAheadIndex
				tokenValue = ""
				tokenType = TT.STRING_LITERAL
			} else {
				while (lookAheadIndex < sourceCode.length) {
					val lookAheadCharacter = sourceCode[lookAheadIndex]
					if (lookAheadCharacter != '"') {
						if (lookAheadIndex + 1 < sourceCode.length &&
							lookAheadCharacter == '\\'
						) {
							handle += when (sourceCode[lookAheadIndex + 1]) {
								't' -> "\t"
								'b' -> "\b"
								'n' -> "\n"
								'r' -> "\r"
								'\'' -> "\'"
								'"' -> "\""
								'\\' -> "\\"
								else -> ""
							}
							lookAheadIndex++
						} else {
							handle += lookAheadCharacter
						}
						lookAheadIndex++
					} else break
				}
				// change index to index of second '"'
				// (as there is an index++ at end)
				index = lookAheadIndex
				tokenValue = handle
				tokenType = TT.STRING_LITERAL
			}
		}

		// number
		if (character.isDigit()) {
			val negative = index - 1 > 0 && sourceCode[index - 1] == '-'
			var number = "" + character
			var lookAheadIndex = index + 1
			while (lookAheadIndex < sourceCode.length) {
				val lookAheadCharacter = sourceCode[lookAheadIndex]
				if ("$lookAheadCharacter".matches("[0-9_.]".toRegex())) {
					// checks for digit or '_' or '.'
					number += lookAheadCharacter
					lookAheadIndex++
				} else break
			}
			// change index to one before lookAheadIndex
			// (as index++ at the end)
			index = --lookAheadIndex
			// ensures it is a valid number
			// removes '_' as they are only 'cosmetic'
			number = number.replace("_", "")
			tokenType = if (number.matches("^[0-9]\\d*(\\.\\d+)?\$".toRegex())) {
				tokenValue = if (negative) {
					"-$number"
				} else {
					number
				}
				if (number.contains(".")) {
					TT.DOUBLE
				} else {
					TT.INTEGER
				}
			} else {
				TT.UNIDENTIFED
			}
		}

		// finish analysing 'token'
		tokens.add(Token(tokenType, tokenValue))
		index++
	}

	return tokens
}

class Token(val type: TT, val value: String? = null) {
	override fun toString(): String {
		return "$type" + if (value != null) {
			" {$value}"
		} else ""
	}
}

// TokenType
enum class TT {
	F_TAG, // 'f'
	IF_TAG, // 'if'
	ELSE_TAG, // 'else'
	WHILE_TAG, // 'while'
	MAIN_TAG, // 'main'

	NAME, // variable, parameter, argument, and function names
	STRING_LITERAL, // "hello world" etc.
	CHAR, // 'a'
	INTEGER, // "4523" etc.
	DOUBLE, // "3.14159" etc.

	ASSIGNER, // '<-'
	SEMICOLON, // ':3'

	OPEN, // '('
	CLOSE, // ')'
	OPEN_SQUARE, // '['
	CLOSE_SQUARE, // ']'
	OPEN_CURLY, // '{'
	CLOSE_CURLY, // '}'
	COMMA, // ','

	UNIDENTIFED, // any string which cannot be matched
}
