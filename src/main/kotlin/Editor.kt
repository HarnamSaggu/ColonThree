import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.KeyListener
import java.io.File
import java.io.PrintStream
import javax.swing.*
import javax.swing.border.Border
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.plaf.basic.BasicSplitPaneUI
import javax.swing.text.DefaultCaret
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument

fun main() {
	Editor()
}

class Editor : JFrame("ColonThree IDE") {
	val editorPane: JTextPane
	val doc: StyledDocument
	val outputPane: JTextArea
	private val outputScroller: JScrollPane
	private var filePath: String? = null
	private var runThread: SwingWorker<Any?, Any?> = createNewWorker()

	val fontForeground = Color(210, 210, 210)
	val editorBackground = Color(40, 40, 40)
	private val variableColor = Color(0xE7BEAA)
	private val keywordColor = Color(243, 69, 49)
	private val assignmentColor = Color(250, 180, 40)
	private val bracketColor = Color(250, 110, 50)
	private val normalBracketColor = Color(0x6E8DA4)
	private val semicolonColor = Color(215, 72, 148)
	private val commentColor = Color(120, 95, 95)
	private val literalColor = Color(145, 185, 35)
	private val numberColor = Color(138, 139, 255)
	private val caretColor = Color(250, 180, 40)
	private val fontSize = 16
	val autoSaveTimer = 1_500

	init {
		defaultCloseOperation = EXIT_ON_CLOSE
		layout = BorderLayout()
		minimumSize = Dimension(500, 330)
		iconImage = if (File("src/main/resources/icon.png").exists()) {
			ImageIcon("src/main/resources/icon.png").image
		} else {
			ImageIcon("icon.png").image
		}
		background = BACKGROUND

		addKeyListener(object : KeyListener {
			override fun keyTyped(e: KeyEvent?) {
				// unused
			}

			override fun keyPressed(e: KeyEvent?) {
				// unused
			}

			override fun keyReleased(e: KeyEvent?) {
				// unused
			}

		})

		val leftPanel = JPanel()
		leftPanel.preferredSize = Dimension(900, 700)
		leftPanel.background = BACKGROUND
		leftPanel.layout = BorderLayout()

		editorPane = createTextPane()
		editorPane.background = BACKGROUND
		editorPane.border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
		editorPane.caretColor = caretColor
		doc = editorPane.styledDocument

		editorPane.addKeyListener(object : KeyListener {
			override fun keyTyped(e: KeyEvent?) {
				if (e != null && e.keyChar == '\n') {
					val text = doc.getText(0, doc.length)
					val pos = editorPane.caretPosition
					val line = text.substring(0, pos - 1).split("\n").last()
					var indentCount = line.length - line.trimStart().length
					if (pos > 2 && text[pos - 2] == '{') {
						indentCount += 3
					}
					val indent = " ".repeat(indentCount)
					doc.insertString(pos, indent, doc.getStyle("regular"))
				}
			}

			override fun keyPressed(e: KeyEvent?) {
				// unused
			}

			override fun keyReleased(e: KeyEvent?) {
				// unused
			}

		})

		val noWrapPanel = JPanel(BorderLayout())
		noWrapPanel.add(editorPane)

		val editorScroller = JScrollPane(noWrapPanel)
		editorScroller.background = BACKGROUND
		editorScroller.verticalScrollBar.background = BACKGROUND
		editorScroller.horizontalScrollBar.background = BACKGROUND
		editorScroller.verticalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				thumbColor = Color(100, 100, 100)
			}
		})
		editorScroller.horizontalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				thumbColor = Color(100, 100, 100)
			}
		})
		editorScroller.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		leftPanel.add(editorScroller, BorderLayout.CENTER)

		val font = Font("Fira code", Font.PLAIN, fontSize)

		outputPane = JTextArea()
		outputPane.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		outputPane.background = BACKGROUND
//		outputPane.preferredSize = Dimension(350, 700)
		outputPane.font = font
		outputPane.foreground = FONT_FOREGROUND
//		(outputPane.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE

		outputScroller = JScrollPane(outputPane)
		outputScroller.background = BACKGROUND
		outputScroller.verticalScrollBar.background = BACKGROUND
		outputScroller.horizontalScrollBar.background = BACKGROUND
		outputScroller.verticalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				thumbColor = Color(100, 100, 100)
			}
		})
		outputScroller.horizontalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				thumbColor = Color(100, 100, 100)
			}
		})
		outputScroller.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val rightPanel = JPanel()
		rightPanel.preferredSize = Dimension(350, 700)
		rightPanel.background = BACKGROUND
		rightPanel.layout = BorderLayout()
		rightPanel.add(outputScroller, BorderLayout.CENTER)

		val splitPane = JSplitPane(SwingConstants.VERTICAL, leftPanel, rightPanel)
		splitPane.setUI(object : BasicSplitPaneUI() {
			override fun createDefaultDivider(): BasicSplitPaneDivider {
				return object : BasicSplitPaneDivider(this) {
					override fun setBorder(b: Border) {
						// unused
					}

					override fun paint(g: Graphics) {
						g.color = Color(100, 100, 100)
						g.fillRect(0, 0, size.width, size.height)
						super.paint(g)
					}
				}
			}
		})
		splitPane.border = null
		add(splitPane, BorderLayout.CENTER)

		doc.setCharacterAttributes(0, editorPane.text.length, doc.getStyle("regular"), true)

		var prevText = ""
		var prevDot = 0
		var time = System.currentTimeMillis()
		object : SwingWorker<Any?, Any?>() {
			override fun doInBackground(): Any? {
				val run = true
				while (run) {
					if (editorPane.text != prevText || editorPane.caret.dot != prevDot) {
						setText()
						prevText = editorPane.text
						prevDot = editorPane.caret.dot

						if (System.currentTimeMillis() - time >= autoSaveTimer) {
							File("autosave.txt").writeText(editorPane.text)

							time = System.currentTimeMillis()
						}
					}
				}
				return null
			}
		}.execute()

		val menuBar = JMenuBar()
		menuBar.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		menuBar.background = BACKGROUND

		val fileMenu = JMenu("File")
		fileMenu.font = font
		fileMenu.foreground = FONT_FOREGROUND
		fileMenu.background = BACKGROUND
		fileMenu.mnemonic = VK_F
		menuBar.add(fileMenu)

		val runMenu = JMenu("Run")
		runMenu.font = font
		runMenu.foreground = FONT_FOREGROUND
		runMenu.background = BACKGROUND
		runMenu.mnemonic = VK_R
		menuBar.add(runMenu)

		val runItem = JMenuItem("Run")
		runItem.font = font
		runItem.foreground = FONT_FOREGROUND
		runItem.background = BACKGROUND
		runItem.mnemonic = VK_R
		runMenu.add(runItem)

		val terminateItem = JMenuItem("Terminate")
		terminateItem.font = font
		terminateItem.foreground = FONT_FOREGROUND
		terminateItem.background = BACKGROUND
		terminateItem.mnemonic = VK_T
		runMenu.add(terminateItem)

		val autoScrollItem = JMenuItem("Auto scroll: false")
		autoScrollItem.font = font
		autoScrollItem.foreground = FONT_FOREGROUND
		autoScrollItem.background = BACKGROUND
		autoScrollItem.mnemonic = VK_T
		runMenu.add(autoScrollItem)

		val openItem = JMenuItem("Open")
		openItem.font = font
		openItem.foreground = FONT_FOREGROUND
		openItem.background = BACKGROUND
		openItem.mnemonic = VK_O
		fileMenu.add(openItem)

		val saveItem = JMenuItem("Save")
		saveItem.font = font
		saveItem.foreground = FONT_FOREGROUND
		saveItem.background = BACKGROUND
		saveItem.mnemonic = VK_S
		fileMenu.add(saveItem)

		val saveAsItem = JMenuItem("Save as")
		saveAsItem.font = font
		saveAsItem.foreground = FONT_FOREGROUND
		saveAsItem.background = BACKGROUND
		saveAsItem.mnemonic = VK_A
		fileMenu.add(saveAsItem)

		val refreshItem = JMenuItem("Refresh")
		refreshItem.font = font
		refreshItem.foreground = fontForeground
		refreshItem.background = editorBackground
		refreshItem.mnemonic = VK_R
		fileMenu.add(refreshItem)

		leftPanel.add(menuBar, BorderLayout.SOUTH)

		openItem.addActionListener {
			val dialog = FileDialog(this, "Select File to Open")
			dialog.mode = FileDialog.LOAD
			dialog.isVisible = true
			filePath = dialog.directory + dialog.file
			dialog.dispose()
			if (filePath != null) {
				editorPane.text = File(filePath!!).readText()
				setText()
			}
		}

		saveItem.addActionListener {
			save()
		}

		saveAsItem.addActionListener {
			val dialog = FileDialog(this, "Select File to Save to")
			dialog.mode = FileDialog.SAVE
			dialog.isVisible = true
			filePath = dialog.directory + dialog.file
			dialog.dispose()
			save()
		}

		refreshItem.addActionListener {
			val sourceCode = doc.getText(0, doc.length)
			setText("")
			setText(sourceCode)
		}

		runThread = createNewWorker()

		runItem.addActionListener {
			runThread.cancel(true)
			outputPane.text = ""
			runThread = createNewWorker()
			runThread.execute()
		}

		terminateItem.addActionListener {
			runThread.cancel(true)
			runThread = createNewWorker()
		}

		autoScrollItem.addActionListener {
			if (autoScrollItem.text == "Auto scroll: false") {
				autoScrollItem.text = "Auto scroll: true"
				(outputPane.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE

			} else {
				autoScrollItem.text = "Auto scroll: false"
				(outputPane.caret as DefaultCaret).updatePolicy = DefaultCaret.NEVER_UPDATE
			}
		}

		pack()
		setLocationRelativeTo(null)
		isVisible = true
	}

	private fun createNewWorker(): SwingWorker<Any?, Any?> {
		return object : SwingWorker<Any?, Any?>() {
			override fun doInBackground(): Any? {
				outputPane.text = ""
				System.setOut(object : PrintStream(System.out) {
					override fun println(x: Any?) {
						outputPane.text += "$x\n"
						super.println(x)
					}

					override fun print(x: Any?) {
						outputPane.text += x
						super.print(x)
					}
				})

				val tokens = lex(editorPane.text)
				val commands = parse(tokens)
				run(commands, { x -> outputPane.text += x }, {
					val input = JTextField()
					input.font = font
					input.foreground = FONT_FOREGROUND
					input.background = BACKGROUND
					val inputArray = arrayOf<JComponent>(input)
					val result = JOptionPane.showConfirmDialog(
						this@Editor,
						inputArray,
						"ColonThree",
						JOptionPane.PLAIN_MESSAGE
					)
					if (result == JOptionPane.OK_OPTION) {
						outputPane.text += input.text + "\n"
						input.text
					} else {
						""
					}
				})

				return null
			}
		}
	}

	private fun terminate(x: Int) {
		outputPane.text += "\nExit code: $x\n"
		runThread.cancel(true)
		runThread = createNewWorker()
	}

	private fun save() {
		if (filePath != null) {
			File(filePath!!).writeText(doc.getText(0, doc.length))
		}
	}

	private fun setText(text: String = doc.getText(0, doc.length)) {
		val interpreted = interpretText(text)
		val sections = interpreted.first
		val styles = interpreted.second
		for (i in sections.indices) {
			doc.setCharacterAttributes(
				sections[i].first,
				sections[i].second - sections[i].first,
				doc.getStyle(styles[i]),
				true
			)
		}
	}

	private fun interpretText(text: String): Pair<MutableList<Pair<Int, Int>>, MutableList<String>> {
		val segments = mutableListOf<Pair<Int, Int>>()
		val styles = mutableListOf<String>()

		val leftOverBrackets = mutableListOf<Int>()
		var index = 0
		val prevSize = 0
		while (index < text.length) {
			val character = text[index]
			if (character.isWhitespace()) {
				segments.add(index to (index + 1))
				styles.add("regular")
				index++
				continue
			}

			// comment
			if (character == '#') {
				// identifies the end of the comment
				val indexOfNext = text.substring(index, text.length).indexOf("\n")
				styles.add("comment")
				if (indexOfNext == -1) {
					segments.add(index to text.length)

					index = text.length
				} else {
					segments.add(index to (index + indexOfNext))

					index += indexOfNext
				}
				continue
			}

			when (character) {
				'(', '[', '{' -> {
					if (editorPane.caret.dot == index + 1) {
						segments.add(index to (index + 1))
						styles.add("bracket")

						var lookAhead = index + 1
						var count = 1
						while (lookAhead < text.length) {
							when (text[lookAhead]) {
								'(', '[', '{' -> {
									if (text[lookAhead - 1] != '\\') {
										count++
									}
								}

								')', ']', '}' -> {
									if (text[lookAhead - 1] != '\\') {
										count--
									}
								}
							}

							if (count == 0) {
								leftOverBrackets.add(lookAhead)
								break
							} else {
								lookAhead++
							}
						}
					} else {
						segments.add(index to (index + 1))
						styles.add("normal bracket")
					}
				}

				')', ']', '}' -> {
					if (editorPane.caret.dot == index) {
						segments.add(index to (index + 1))
						styles.add("bracket")

						var lookBehind = index - 1
						var count = 1
						while (lookBehind >= 0) {
							when (text[lookBehind]) {
								')', ']', '}' -> {
									if (text[lookBehind - 1] != '\\') {
										count++
									}
								}

								'(', '[', '{' -> {
									if (text[lookBehind - 1] != '\\') {
										count--
									}
								}
							}

							if (count == 0) {
								leftOverBrackets.add(lookBehind)
								break
							} else {
								lookBehind--
							}
						}
					} else {
						segments.add(index to (index + 1))
						styles.add("normal bracket")
					}
				}

				';' -> {
					segments.add(index to (index + 1))
					styles.add("semicolon")
				}

				',', '-' -> {
					segments.add(index to (index + 1))
					styles.add("regular")
				}
			}

			// checks for '<-'
			if (index < text.length - 1 &&
				character == '<' && text[index + 1] == '-'
			) {
				segments.add(index to (index + 2))
				styles.add("assignment")
				// (2 characters so an extra increment needed)
				index++
			}

			// checks for ':3'
			if (index < text.length - 1 &&
				character == ':' && text[index + 1] == '3'
			) {
				segments.add(index to (index + 2))
				styles.add("semicolon")
				index++
			}

			// checks for regular characters
			if (index < text.length - 2 &&
				character == '\'' && text[index + 2] == '\''
			) {
				segments.add(index to (index + 3))
				styles.add("literal")
				index += 2
			}

			// checks for special characters
			if (index < text.length - 3 &&
				character == '\'' && text[index + 1] == '\\' && text[index + 3] == '\''
			) {
				when (text[index + 2]) {
					't', 'b', 'n', 'r', '\'', '"', '\\' -> {
						segments.add(index to (index + 4))
						styles.add("literal")
					}
				}
			}

			if (character.isLetter()) {
				// this means it could either be a tag or name
				// extract string part, if ends with ':' then tag else name
				var handle = "" + character
				var lookAheadIndex = index + 1
				var lookAheadCharacter: Char
				while (lookAheadIndex < text.length) {
					lookAheadCharacter = text[lookAheadIndex]
					if ("$lookAheadCharacter".matches("\\w".toRegex())) {
						// [a-zA-Z0-9_] character
						handle += lookAheadCharacter
						lookAheadIndex++
					} else break
				}

				// change index to the index before the lookAheadIndex
				// (as index++ is at the end)
				when (handle) {
					"f", "if", "else", "while", "main" -> {
						segments.add(index to lookAheadIndex)
						styles.add("keyword")
					}

					else -> {
						segments.add(index to lookAheadIndex)
						if (lookAheadIndex < text.length && text[lookAheadIndex] != '(') {
							styles.add("variable")
						} else {
							styles.add("regular")
						}
					}
				}
				index = --lookAheadIndex
			}

			// string literal
			if (character == '"') {
				var lookAheadIndex = index + 1
				if (lookAheadIndex < text.length && text[lookAheadIndex] == '"') {
					segments.add(index to (lookAheadIndex + 1))
					styles.add("literal")
					index = lookAheadIndex
				} else {
					while (lookAheadIndex < text.length) {
						val lookAheadCharacter = text[lookAheadIndex]
						if (lookAheadCharacter != '"') {
							if (lookAheadIndex + 1 < text.length &&
								lookAheadCharacter == '\\'
							) {

								lookAheadIndex++
							}
							lookAheadIndex++
						} else break
					}
					// change index to index of second '"'
					// (as there is an index++ at end)
					segments.add(index to (lookAheadIndex + 1))
					styles.add("literal")
					index = lookAheadIndex
				}
			}

			// number
			if (character.isDigit()) {
				val negative = index - 1 > 0 && text[index - 1] == '-'
				var number = "" + character
				var lookAheadIndex = index + 1
				while (lookAheadIndex < text.length) {
					val lookAheadCharacter = text[lookAheadIndex]
					if ("$lookAheadCharacter".matches("[0-9_.]".toRegex())) {
						// checks for digit or '_' or '.'
						number += lookAheadCharacter
						lookAheadIndex++
					} else break
				}
				// removes '_' as they are only 'cosmetic'
				number = number.replace("_", "")
				if (number.matches("^[0-9]\\d*(\\.\\d+)?\$".toRegex())) {
					if (negative) {
						segments.add((index - 1) to lookAheadIndex)
						styles.add("number")
					} else {
						segments.add(index to lookAheadIndex)
						styles.add("number")
					}
				}
				// change index to one before lookAheadIndex
				// (as index++ at the end)
				index = --lookAheadIndex
			}

			if (prevSize == segments.size) {
				segments.add(index to (index + 1))
				styles.add("comment")
			}

			index++
		}
		leftOverBrackets.forEach {
			segments.add(it to (it + 1))
			styles.add("bracket")
		}

		return segments to styles
	}
}
