import java.math.BigDecimal
import java.math.BigInteger

fun run(sourceCode: String) {
	val tokens = lex(sourceCode)
	val commands = parse(tokens)
	Runner(commands)
}

fun run(commands: List<Command>, output: ((Any) -> Unit), input: (() -> String)) {
	Runner(commands, output, input)
}

class Runner(commands: List<Command>, output: ((Any) -> Unit)? = null, input: (() -> String)? = null) {
	// variables -------------------------------------------------------------------------------------
	// variables are organised on different levels corresponding to the scope of which it's defined in
	// the level is an array, each integer represents the index of that scope level within it's scope
	// a variable defined in main would have level [0]
	// a variable defined in an if statement in main has level [0. 0]
	// a variable defined in an if statement in main after the previous has level [0, 1]
	private val variables: MutableMap<MutableList<Int>, MutableMap<String, Any>> = mutableMapOf()

	// stores all user defined methods
	private val methods: Map<String, MethodDeclaration> = commands.filterIsInstance<MethodDeclaration>().associateBy {
		it.methodName
	}

	// every new function call gets a new method level
	private var methodLevel = mutableListOf(0)

	init {
		// redefines I/O
		if (output != null) {
			changeOutput(output)
		}
		if (input != null) {
			changeInput(input)
		}

		// defines variable level for the main method
		val mainMethod = commands.filterIsInstance<MainMethod>()[0]
		variables[methodLevel.toMutableList()] = mutableMapOf()

		runSection(mainMethod.body, methodLevel.toMutableList(), null)
	}

	// as a method counts as a section, a return statement is needed
	private fun runSection(
		commands: List<Command>,
		variableLevel: MutableList<Int>,
		returnStatement: Command?,
	): Any {
		// subLevel keeps track of how many levels precede it for the last int in the level signature
		var subLevel = 0

		// each section has only a certain number of commands
		// which can have whatever subcommands
		commands.forEach {
			if (it is VariableAssignment) {
				val actualLevel = findVariable(it.variableName, variableLevel.toMutableList())
				variables[actualLevel]?.let { x ->
					val value = evaluate(it.value, variableLevel)

					x[it.variableName] = value
				}
			} else if (it is ArrayItemAssignment) {
				val actualLevel = findVariable(it.variableName, variableLevel.toMutableList())
				variables[actualLevel]?.let { x ->
					val value = evaluate(it.value, variableLevel)
					val index = (evaluate(it.index, variableLevel) as BigInteger).toInt()
					// i will try to avoid casting Any? to Mutable<Any>
					(x[it.variableName] as MutableList<Any>)[index] = value
				}
			} else if (it is MethodCall) {
				// returns the return of a method
				// if the prebuilt returns null (0), then either it was:
				// * the method was not recognised
				// * invalid data types passed
				// therefore we check if a user defined function matches the arguments better
				if (evaluatePrebuiltMethod(it.functionName, it.arguments.map { x ->
						evaluate(x, variableLevel)
					}) == 0) {
					runUserDefinedMethod(it, variableLevel)
				}

			} else if (it is IfElseStatement) {
				// if statement

				// new variable stack needs to be made, not necessary as whether the section runs is undetermined
				val newVarLevel = (variableLevel + mutableListOf(subLevel)).toMutableList()
				subLevel++
				variables[newVarLevel] = mutableMapOf()

				val booleanExpression = evaluate(it.booleanCondition, variableLevel) as BigInteger
				if (booleanExpression > BigInteger("0")) {
					// runs main body for if statement
					runSection(it.trueBody, newVarLevel, null)
				} else if (it.falseBody.isNotEmpty()) {
					// if else statement exists then run it
					runSection(it.falseBody, newVarLevel, null)
				}
			} else if (it is WhileStatement) {
				var booleanExpression = evaluate(it.booleanCondition, variableLevel) as BigInteger
				// creates new level for loop
				val newVarLevel = (variableLevel + mutableListOf(subLevel)).toMutableList()
				subLevel++
				while (booleanExpression.signum() == 1) {
					// wipes variable level clean each loop
					variables[newVarLevel] = mutableMapOf()

					// runs body and reevaluates boolean expression
					runSection(it.body, newVarLevel, null)
					booleanExpression = evaluate(it.booleanCondition, variableLevel) as BigInteger
				}
			}
		}

		// returns null (0) if no return specified
		return if (returnStatement != null) {
			evaluate(returnStatement, variableLevel)
		} else {
			0
		}
	}

	private fun runUserDefinedMethod(methodCall: MethodCall, variableLevel: MutableList<Int>): Any {
		// try to retrieve command for method
		val method = methods[methodCall.functionName]
		return if (method != null) {
			// creates new level
			methodLevel[0]++
			variables[methodLevel.toMutableList()] = mutableMapOf()

			// user defined method does exist

			// loops through the parameters needed and assigns them value in the variable level
			// for the arguments provided
			for (index in method.parameterNames.indices) {
				variables[methodLevel]?.let { x ->
					val value = evaluate(methodCall.arguments[index], variableLevel)
					x[method.parameterNames[index]] = value
				}
			}

			val ret = runSection(method.body, methodLevel.toMutableList(), method.returnStatement)

			// cleans up variable level
			variables[methodLevel.toMutableList()] = mutableMapOf()
			methodLevel[0]--

			ret
		} else {
			0
		}
	}

	// takes a value which needs to be returned
	private fun evaluate(command: Command, variableLevel: MutableList<Int>): Any {
		when (command) {
			is MethodCall -> {
				// returns the return of a method
				// if the prebuilt returns null (0), then either it was:
				// * the method was not recognised
				// * invalid data types passed
				// therefore we check if a user defined function matches the arguments better
				val prebuiltReturn = evaluatePrebuiltMethod(command.functionName, command.arguments.map { x ->
					evaluate(x, variableLevel)
				})
				return if (prebuiltReturn == 0) {
					runUserDefinedMethod(command, variableLevel)
				} else {
					prebuiltReturn
				}
			}

			is RawValue -> {
				// an explicit value
				// casts string in source code to corresponding type
				return when (command.type) {
					TT.INTEGER -> BigInteger(command.value)
					TT.DOUBLE -> BigDecimal(command.value)
					TT.CHAR -> command.value[0]
					TT.STRING_LITERAL -> command.value
					// 0 is essentially null as integer values are all handled via BigInteger
					// therefore any method needing an integer will attempt to cast it and throw an exception
					else -> 0
				}
			}

			is Variable -> {
				return getVariable(command.variableName, variableLevel)
			}

			is RawSizedArrayDefinition -> {
				// a sized definition is an empty array of BigInteger 0s
				val size = (evaluate(command.size, variableLevel) as BigInteger).toInt()
				return MutableList<Any>(size) { BigInteger("0") }
			}

			is RawArrayDefinition -> {
				// takes items as commands, evaluates them and returns an array
				return command.items.map { evaluate(it, variableLevel) }.toMutableList()
			}

			is ArrayItem -> {
				return getArrayItem(
					command.variableName,
					(evaluate(command.index, variableLevel) as BigInteger).toInt(),
					variableLevel
				)
			}
		}

		return 0
	}

	private fun getVariable(variableName: String, variableLevel: MutableList<Int>): Any {
		val actualVarLevel = findVariable(variableName, variableLevel)
		// return 0 if variable not found in the level 'containing' it
		val level = variables[actualVarLevel] ?: return 0
		val variable = level[variableName] ?: 0
		// returns value of variable and not the object itself
		return if (variable is List<*>) variable.toMutableList() else variable
	}

	private fun getArrayItem(variableName: String, index: Int, variableLevel: MutableList<Int>): Any {
		val actualVarLevel = findVariable(variableName, variableLevel)
		val level = variables[actualVarLevel]
		// return 0 if variable not found in the level 'containing' it
		return (level?.get(variableName) as MutableList<*>)[index] ?: 0
	}

	private fun findVariable(variableName: String, variableLevel: MutableList<Int>): MutableList<Int> {
		// starts off by looking at the first level in the level stack
		// if not found, moves down a level etc. until found
		val searchLocation = variableLevel.toMutableList()
		val currentLevel = mutableListOf<Int>()

		while (searchLocation.isNotEmpty()) {
			currentLevel.add(searchLocation[0])
			searchLocation.removeFirst()

			variables[currentLevel]?.let {
				val possibleValue: Any? = it[variableName]
				if (possibleValue != null) {
					// variable found
					return currentLevel
				}
			}
		}

		// if variable not found, then it is to be initialized
		// therefore return the variableLevel as it was given
		return variableLevel
	}
}
