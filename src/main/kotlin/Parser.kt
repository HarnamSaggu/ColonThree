// collects all tokens encased by countIntType and countDecType
fun collect(
	tokens: List<Token>,
	startingIndex: Int,
	startingCount: Int,
	countIncType: TT,
	countDecType: TT
): Pair<Int, MutableList<Token>> {
	// index is needed so you can pick up from where this method leaves off
	var index = startingIndex
	// count keeps track of how nested we are in the pair of types which are collecting between
	var count = startingCount
	val accumulator: MutableList<Token> = mutableListOf()

	while (count != 0) {
		if (index == tokens.size) break

		if (tokens[index].type == countIncType) {
			count++
		} else if (tokens[index].type == countDecType) {
			count--
		}

		if (count != 0) {
			accumulator.add(tokens[index])
			index++
		}
	}

	return Pair(index, accumulator)
}

// first puts all code sections into their respective methods
fun parse(tokens: List<Token>): List<Command> {
	val commands = mutableListOf<Command>()

	var index = 0
	while (index < tokens.size) {
		val token = tokens[index]

		if (token.type == TT.MAIN_TAG) {
			val body = collect(tokens, index + 2, 1, TT.OPEN_CURLY, TT.CLOSE_CURLY)
//			body.second.addAll(mutableListOf(Token(TT.SEMICOLON), Token(TT.NAME, "exit"), Token(TT.OPEN), Token(TT.CLOSE), Token(TT.SEMICOLON)))
			// todo write better way of adding a closing exit function to show exit code of 0 for natural termination
			index = body.first
			commands.add(MainMethod(splitIntoSections(body.second).map { parseSection(it) }))
		} else if (token.type == TT.F_TAG) {
			// index is on f tag, move onto method name
			index++
			val methodName = tokens[index].value ?: "undefined"
			// moves from name to first parameter
			index += 2
			// stores the names of each parameter
			val parameters = mutableListOf<String>()
			while (tokens[index].type != TT.CLOSE) {
				if (tokens[index].type != TT.COMMA) {
					parameters.add(tokens[index].value ?: "undefined")
				}
				index++
			}

			// moves off ')' onto first token in body, collects body
			val func = collect(tokens, index + 2, 1, TT.OPEN_CURLY, TT.CLOSE_CURLY)
			index = func.first
			// body into lines/sections of code
			val sections = splitIntoSections(func.second)

			// if no return given, assume it to be 0 (null as 0 is not a BigInteger)
			var returnStatement: Command = RawValue("0", TT.INTEGER)
			if (sections.isNotEmpty() && sections.last()[0].type == TT.ASSIGNER) {
				val returnLine = sections.last()
				sections.removeLast()
				returnStatement = parseSection(returnLine.subList(1, returnLine.size))
			}
			val sectionCommands = sections.map { parseSection(it) }
			commands.add(MethodDeclaration(methodName, parameters, sectionCommands, returnStatement))
		}

		index++
	}

	return commands
}

// takes a body of tokens, splits into "lines" of code determined by semicolons
// if a "line" begins with an "if"/"while" it separates the body of it, so it's all one command
fun splitIntoSections(tokens: List<Token>): MutableList<MutableList<Token>> {
	val sections = mutableListOf<MutableList<Token>>()
	// keeps track of all tokens in the section being appended to
	val current = mutableListOf<Token>()
	var index = 0
	while (index < tokens.size) {
		if (tokens[index].type == TT.SEMICOLON) {
			// semicolons are the ends of sections
			sections.add(current.toMutableList())
			current.clear()
		} else if (tokens[index].type == TT.IF_TAG || tokens[index].type == TT.WHILE_TAG) {
			// an if statement/while loop are sections
			val isIf = tokens[index].type == TT.IF_TAG
			current.add(tokens[index])
			index++
			// loops until '{' to collect boolean expression
			while (tokens[index].type != TT.OPEN_CURLY) {
				current.add(tokens[index])
				index++
			}
			current.add(tokens[index])

			val body = collect(tokens, index + 1, 1, TT.OPEN_CURLY, TT.CLOSE_CURLY)
			current.addAll(body.second)
			index = body.first
			current.add(tokens[index])

			// if statement only
			if (isIf &&
				index + 1 < tokens.size &&
				tokens[index + 1].type == TT.ELSE_TAG
			) {
				index++
				current.add(tokens[index])
				index++
				current.add(tokens[index])

				val altBody = collect(tokens, index + 1, 1, TT.OPEN_CURLY, TT.CLOSE_CURLY)
				current.addAll(altBody.second)
				index = altBody.first
				current.add(tokens[index])
			}

		} else {
			current.add(tokens[index])
		}

		index++
	}

	return sections
}

fun parseSection(section: MutableList<Token>): Command {
	val tokens = section.toMutableList()

	var command = Command()
	if (tokens.size == 0) {
		return command
	}

	val first = tokens[0]
	if (first.type == TT.NAME) {
		if (tokens.size > 1 && tokens[1].type == TT.ASSIGNER) {
			// variable assignment
			val varName = first.value ?: "undefined"
			// remove name and assignment operator leaving only value
			tokens.removeFirst()
			tokens.removeFirst()

			// parse value into commands
			command = VariableAssignment(varName, parseSection(tokens))
		} else if (tokens.size > 1 && tokens[1].type == TT.OPEN) {
			// method call
			val funcName = first.value ?: "undefined"

			// removes method name, and brackets, leaving only arguments
			tokens.removeFirst()
			tokens.removeFirst()
			tokens.removeLast()

			val argumentCommands = collectArguments(tokens).map {
				parseSection(it)
			}

			command = MethodCall(funcName, argumentCommands)
		} else if (tokens.size > 1 && tokens[1].type == TT.OPEN_SQUARE) {
			// either array item access or assignment
			val variableName = first.value ?: "undefined"
			// removes name and '['
			tokens.removeFirst()
			tokens.removeFirst()

			// collects all tokens for the index value
			val indexTokens = mutableListOf<Token>()
			while (tokens[0].type != TT.CLOSE_SQUARE) {
				indexTokens.add(tokens[0])
				tokens.removeFirst()
			}

			// remove ']'
			tokens.removeFirst()

			// if there are still tokens left it means there should be assignment
			command = if (tokens.isNotEmpty()) {
				// removes assigment operator, leaving just the value's tokens
				tokens.removeFirst()
				ArrayItemAssignment(variableName, parseSection(indexTokens), parseSection(tokens))
			} else {
				ArrayItem(variableName, parseSection(indexTokens))
			}
		} else {
			// variable access
			command = Variable(first.value ?: "undefined")
		}
	} else if (
		first.type == TT.STRING_LITERAL ||
		first.type == TT.CHAR ||
		first.type == TT.INTEGER ||
		first.type == TT.DOUBLE
	) {
		// raw data
		// any values defined explicitly in source code
		command = RawValue(first.value ?: "undefined", first.type)
	} else if (first.type == TT.IF_TAG || first.type == TT.WHILE_TAG) {
		// if statement or while loop
		// remove if tag/while tag
		tokens.removeFirst()

		// finds boolean expression tokens
		val booleanExpression = mutableListOf<Token>()
		while (tokens[0].type != TT.OPEN_CURLY) {
			booleanExpression.add(tokens[0])
			tokens.removeFirst()
		}
		tokens.removeFirst()

		val body = collect(tokens, 0, 1, TT.OPEN_CURLY, TT.CLOSE_CURLY)

		if (first.type == TT.WHILE_TAG) {
			val bodyCommands = splitIntoSections(body.second).map {
				parseSection(it)
			}
			command = WhileStatement(parseSection(booleanExpression), bodyCommands)
		} else {
			for (i in 0..body.first) {
				tokens.removeFirst()
			}

			// if there are still tokens left it must be due to an else statement
			val altBody = mutableListOf<Token>()
			if (tokens.isNotEmpty() && tokens[0].type == TT.ELSE_TAG) {
				// removes else tag, '{', and '}'
				tokens.removeFirst()
				tokens.removeFirst()
				tokens.removeLast()
				// remaining tokens are all for false body
				altBody.addAll(tokens)
			}

			// takes true and false, turns into sections and parses them into commands
			val mainBodyCommands = splitIntoSections(body.second).map {
				parseSection(it)
			}

			val altBodyCommands = splitIntoSections(altBody).map {
				parseSection(it)
			}

			command = IfElseStatement(
				parseSection(booleanExpression),
				mainBodyCommands,
				altBodyCommands
			)
		}
	} else if (first.type == TT.OPEN_CURLY) {
		// explicit array definition
		// removes '{'
		tokens.removeFirst()

		if (tokens[0].type == TT.CLOSE_CURLY) {
			// array defined by size (or an empty array)
			// if the curly brackets are empty then it's defined by size
			// removes '}', '[', and ']', leaving tokens for size
			tokens.removeFirst()
			command = if (tokens.isEmpty()) {
				// user is defining an empty array
				RawArrayDefinition(mutableListOf())
			} else {
				// size defined
				tokens.removeFirst()
				tokens.removeLast()

				RawSizedArrayDefinition(parseSection(tokens))
			}
		} else {
			// array defined by initial items
			// removes '}'
			tokens.removeLast()

			// list of all items as their commands
			val itemCommands = collectArguments(tokens).map {
				parseSection(it)
			}

			command = RawArrayDefinition(itemCommands)
		}
	}
	return command
}

fun collectArguments(tokens: List<Token>): MutableList<MutableList<Token>> {
	// collects all commands for each argument
	val arguments = mutableListOf<MutableList<Token>>()
	// how many brackets (of any time) we are in,
	// so we know when to ignore a comma
	var nestCount = 0
	// hold tokens for current argument
	val accumulator = mutableListOf<Token>()

	for (token in tokens) {
		if (token.type == TT.COMMA && nestCount == 0) {
			// adds argument tokens to arguments
			arguments.add(accumulator.toMutableList())
			accumulator.clear()
		} else {
			if (token.type == TT.OPEN ||
				token.type == TT.OPEN_CURLY ||
				token.type == TT.OPEN_SQUARE
			) {
				nestCount++
			} else if (token.type == TT.CLOSE ||
				token.type == TT.CLOSE_CURLY ||
				token.type == TT.CLOSE_SQUARE
			) {
				nestCount--
			}

			accumulator.add(token)
		}
	}
	// if item remaining, parse it and add it item commands
	if (accumulator.isNotEmpty()) {
		arguments.add(accumulator)
	}

	return arguments
}

open class Command {
	override fun toString(): String {
		return "UNDEFINED"
	}
}

class VariableAssignment(val variableName: String, val value: Command) : Command() {
	override fun toString(): String {
		return "{var ass | name \"$variableName\"\n\tva, value \"$value\"}"
	}
}

class Variable(val variableName: String) : Command() {
	override fun toString(): String {
		return "{var | \"$variableName\"}"
	}
}

class MethodCall(val functionName: String, val arguments: List<Command>) : Command() {
	override fun toString(): String {
		return "{func call | name \"$functionName\", \n\t-fc, args \"$arguments\"}"
	}
}

class RawValue(val value: String, val type: TT) : Command() {
	override fun toString(): String {
		return "{raw val | val \"$value\" | type \"${type.name}\"}"
	}
}

class MainMethod(val body: List<Command>) : Command() {
	override fun toString(): String {
		return "{main\n\t${body.joinToString("\n\t")}\n}"
	}
}

class MethodDeclaration(
	val methodName: String,
	val parameterNames: List<String>,
	val body: List<Command>, val returnStatement: Command
) : Command() {
	override fun toString(): String {
		return "{method | name \"$methodName\" | params \"$parameterNames\" | ret \"$returnStatement\" , \n\t${
			body.joinToString(
				"\n\t"
			)
		}\n}"
	}
}

class IfElseStatement(val booleanCondition: Command, val trueBody: List<Command>, val falseBody: List<Command>) :
	Command() {
	override fun toString(): String {
		return "{if | cond \"$booleanCondition\" | true, \n\t${trueBody.joinToString("\n\t")}\n\t false, \n\t${
			falseBody.joinToString(
				"\n\t"
			)
		}}"
	}
}

class WhileStatement(val booleanCondition: Command, val body: List<Command>) : Command() {
	override fun toString(): String {
		return "{while | cond \"$booleanCondition\", \n\t${body.joinToString("\n\t")}"
	}
}

class RawSizedArrayDefinition(val size: Command) : Command() {
	override fun toString(): String {
		return "{array size init | size \"$size\"}"
	}
}

class RawArrayDefinition(val items: List<Command>) : Command() {
	override fun toString(): String {
		return "{array init | items \"$items\"}"
	}
}

class ArrayItemAssignment(val variableName: String, val index: Command, val value: Command) : Command() {
	override fun toString(): String {
		return "{array item ass | name \"$variableName\" | index \"$index\" | value \"$value\"}}"
	}
}

class ArrayItem(val variableName: String, val index: Command) : Command() {
	override fun toString(): String {
		return "{array item | name \"$variableName\" | index \"$index\"}}"
	}
}
