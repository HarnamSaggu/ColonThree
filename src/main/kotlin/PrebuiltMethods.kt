import rjm.BigDecimalMath
import rjm.BigIntegerMath
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

var output: ((Any) -> Unit) = {
	x -> print(x)
}

fun changeOutput(method: ((Any) -> Unit)) {
	output = method
}

var input: (() -> String) = {
	readln()
}

fun changeInput(method: (() -> String)) {
	input = method
}

fun evaluatePrebuiltMethod(name: String, args: List<Any>): Any = when (name) {
	"add" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.add(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.add(b)
		} else if (args[0] is MutableList<*>) {
			// appends a value arg[1] onto the array args[0]
			(args[0] as MutableList<*>) + mutableListOf(args[1])
		} else {
			0
		}
	}

	"sub" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.subtract(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.subtract(b)
		} else {
			0
		}
	}

	"mult" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.multiply(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.multiply(b)
		} else {
			0
		}
	}

	"div" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.divide(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.divide(b)
		} else {
			0
		}
	}

	"pow" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.pow(b.toInt())
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			BigDecimalMath.pow(a, b)
		} else {
			0
		}
	}

	"gcd" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.gcd(b)
		} else {
			0
		}
	}

	"lcm" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			BigIntegerMath.lcm(a, b)
		} else {
			0
		}
	}

	"mod" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.mod(b)
		} else {
			0
		}
	}

	"ln" -> {
		if (args[0] is BigDecimal) {
			BigDecimalMath.log(args[0] as BigDecimal)
		} else {
			0
		}
	}

	"logb" -> {
		if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			BigDecimalMath.log(a).divide(BigDecimalMath.log(b))
		} else {
			0
		}
	}

	"sqrt" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.sqrt(a)
		} else {
			0
		}
	}

	"nthrt" -> {
		if (args[0] is BigDecimal && args[1] is BigInteger) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigInteger
			BigDecimalMath.root(b.toInt(), a)
		} else {
			0
		}
	}

	"abs" -> {
		if (args[0] is BigInteger) {
			val a = args[0] as BigInteger
			a.abs()
		} else if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			a.abs()
		} else {
			0
		}
	}

	"sin" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.sin(a)
		} else {
			0
		}
	}

	"cos" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.cos(a)
		} else {
			0
		}
	}

	"tan" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.tan(a)
		} else {
			0
		}
	}

	"asin" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.asin(a)
		} else {
			0
		}
	}

	"acos" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.acos(a)
		} else {
			0
		}
	}

	"atan" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			BigDecimalMath.atan(a)
		} else {
			0
		}
	}

	"pi" -> {
		// user can specify how many digits of pi they require, default is 128
		val a = if (args.isNotEmpty() && args[0] is BigInteger) {
			(args[0] as BigInteger).toInt()
		} else {
			128
		}
		BigDecimalMath.pi(MathContext(a))
	}

	"e" -> {
		// user can specify how many digits of e they require, default is 128
		val a = if (args.isNotEmpty() && args[0] is BigInteger) {
			(args[0] as BigInteger).toInt()
		} else {
			128
		}
		BigDecimalMath.exp(MathContext(a))
	}

	// random number x, 0 <= x < 1
	"rand" -> {
		BigDecimal(Math.random())
	}

	"ceil" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			a.add(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP)
		} else {
			0
		}
	}

	"round" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			a.setScale(0, RoundingMode.HALF_UP)
		} else {
			0
		}
	}

	"floor" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			a.subtract(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP)
		} else {
			0
		}
	}

	"min" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.min(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.min(b)
		} else {
			0
		}
	}

	"max" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			a.max(b)
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			a.max(b)
		} else {
			0
		}
	}

	// int -> double
	"double" -> {
		if (args[0] is BigInteger) {
			val a = args[0] as BigInteger
			BigDecimal(a)
		} else {
			0
		}
	}

	// double -> int, char -> int, string -> int
	"int" -> {
		if (args[0] is BigDecimal) {
			val a = args[0] as BigDecimal
			a.toBigInteger()
		} else if (args[0] is Char) {
			(args[0] as Char).code
		} else if (args[0] is String) {
			BigInteger((args[0] as String).replace("_", ""))
		} else {
			0
		}
	}

	// int -> char
	"char" -> {
		if (args[0] is BigInteger) {
			val a = args[0] as BigInteger
			a.toInt().toChar()
		} else {
			0
		}
	}

	"readln" -> {
		input()
	}

	"print" -> {
		if (args[0] is Int) {
			output("null (0)")
		} else {
			output(args[0])
		}
	}

	"println" -> {
		if (args[0] is Int) {
			output("null (0)\n")
		} else {
			output("${args[0]}\n")
		}
	}

	"equals" -> {
		if (args[0] == args[1]) {
			BigInteger("1")
		} else {
			BigInteger("0")
		}
	}

	// args[0] > args[1]
	"gt" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a > b) BigInteger("1") else BigInteger("0")
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			if (a > b) BigInteger("1") else BigInteger("0")
		} else {
			0
		}
	}

	// args[0] < args[1]
	"lt" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a < b) BigInteger("1") else BigInteger("0")
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			if (a < b) BigInteger("1") else BigInteger("0")
		} else {
			0
		}
	}

	// args[0] >= args[1]
	"gte" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a >= b) BigInteger("1") else BigInteger("0")
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			if (a >= b) BigInteger("1") else BigInteger("0")
		} else {
			0
		}
	}

	// args[0] <= args[1]
	"lte" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a <= b) BigInteger("1") else BigInteger("0")
		} else if (args[0] is BigDecimal && args[1] is BigDecimal) {
			val a = args[0] as BigDecimal
			val b = args[1] as BigDecimal
			if (a <= b) BigInteger("1") else BigInteger("0")
		} else {
			0
		}
	}

	"not" -> {
		if (args[0] is BigInteger) {
			val a = args[0] as BigInteger
			if (a.signum() == 1) {
				BigInteger("0")
			} else {
				BigInteger("1")
			}
		} else {
			0
		}
	}

	"and" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a.signum() == 1 && b.signum() == 1) {
				BigInteger("1")
			} else {
				BigInteger("0")
			}
		} else {
			0
		}
	}

	"or" -> {
		if (args[0] is BigInteger && args[1] is BigInteger) {
			val a = args[0] as BigInteger
			val b = args[1] as BigInteger
			if (a.signum() == 1 || b.signum() == 1) {
				BigInteger("1")
			} else {
				BigInteger("0")
			}
		} else {
			0
		}
	}

	"join" -> {
		args.joinToString("")
	}

	"length" -> {
		if (args[0] is String) {
			BigInteger((args[0] as String).length.toString())
		} else if (args[0] is MutableList<*>) {
			BigInteger((args[0] as MutableList<*>).size.toString())
		} else {
			0
		}
	}

	"charAt" -> {
		if (args[0] is String && args[1] is BigInteger) {
			(args[0] as String)[(args[1] as BigInteger).toInt()]
		} else {
			0
		}
	}

	"contains" -> {
		if (args[0] is String && args[1] is String) {
			if ((args[0] as String).contains(args[1] as String)) BigInteger("1") else BigInteger("0")
		} else if (args[0] is MutableList<*>) {
			if ((args[0] as MutableList<*>).contains(args[1])) BigInteger("1") else BigInteger("0")
		} else {
			0
		}
	}

	"replace" -> {
		if (args[0] is String && args[1] is String && args[2] is String) {
			(args[0] as String).replaceFirst(args[1] as String, args[2] as String)
		} else {
			0
		}
	}

	"replaceAll" -> {
		if (args[0] is String && args[1] is String && args[2] is String) {
			(args[0] as String).replace(args[1] as String, args[2] as String)
		} else {
			0
		}
	}

	"replaceRegex" -> {
		if (args[0] is String && args[1] is String && args[2] is String) {
			(args[0] as String).replaceFirst(Regex(args[1] as String), args[2] as String)
		} else {
			0
		}
	}

	"replaceAllRegex" -> {
		if (args[0] is String && args[1] is String && args[2] is String) {
			(args[0] as String).replace(Regex(args[1] as String), args[2] as String)
		} else {
			0
		}
	}

	"split" -> {
		if (args[0] is String && args[1] is String) {
			(args[0] as String).split(Regex(args[1] as String))
		} else {
			0
		}
	}

	"substring" -> {
		if (args[0] is String && args[1] is BigInteger && args[2] is BigInteger) {
			val a = args[0] as String
			val b = (args[2] as BigInteger).toInt()
			val c = (args[2] as BigInteger).toInt()
			a.substring(b, c)
		} else {
			0
		}
	}

	"string" -> {
		args[0].toString()
	}

	"addAll" -> {
		if (args[0] is MutableList<*> && args[1] is MutableList<*>) {
			val a = args[0] as MutableList<*>
			val b = args[1] as MutableList<*>
			a + b
		} else {
			0
		}
	}

	"removeAt" -> {
		if (args[0] is MutableList<*> && args[1] is BigInteger) {
			val a = args[0] as MutableList<*>
			val b = (args[1] as BigInteger).toInt()
			a.filterIndexed { index, _ -> index != b }
		} else {
			0
		}
	}

	"sublist" -> {
		if (args[0] is MutableList<*> && args[1] is BigInteger && args[2] is BigInteger) {
			val a = args[0] as MutableList<*>
			val b = (args[1] as BigInteger).toInt()
			val c = (args[2] as BigInteger).toInt()
			a.subList(b, c)
		} else {
			0
		}
	}

	"class" -> {
		args[0]::class
	}

	"sleep" -> {
		if (args[0] is BigInteger) {
			Thread.sleep((args[0] as BigInteger).toLong())
		} else {
			0
		}
	}

	"readFile" -> {
		if (args[0] is String) {
			File(args[0] as String).readText()
		} else {
			0
		}
	}

	"writeFile" -> {
		if (args[0] is String && args[1] is String) {
			File(args[0] as String).writeText(args[1] as String)
		} else {
			0
		}
	}

	"readBytes" -> {
		if (args[0] is String) {
			File(args[0] as String).readBytes().map { BigInteger(it.toString()) }.toMutableList()
		} else {
			0
		}
	}

	"writeBytes" -> {
		if (args[0] is String && args[1] is MutableList<*>) {
			File(args[0] as String).writeBytes((args[1] as MutableList<*>).map {
				(it as BigInteger).toString().toByte()
			}.toByteArray())
		} else {
			0
		}
	}

	else -> 0
}
