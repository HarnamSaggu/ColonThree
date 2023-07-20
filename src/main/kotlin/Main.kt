import java.io.File

fun main(args: Array<String>) {
	if (args.isEmpty()) {
		print("Path: ")
		run(File(readln()).readText())
	} else if (args.size == 1) {
		println(args.toList())
		run(File(args[0]).readText())
	}
}
