import java.awt.*
import java.awt.event.*
import java.awt.event.KeyEvent.*
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
import kotlin.system.exitProcess


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
	val editorBackground = Color(20, 20, 20)
	private val variableColor = Color(0x9591FF)
	private val keywordColor = Color(243, 69, 49)
	private val assignmentColor = Color(250, 180, 40)
	private val bracketColor = Color(250, 110, 50)
	private val normalBracketColor = Color(0x6E8DA4)
	private val semicolonColor = Color(215, 72, 148)
	private val commentColor = Color(120, 95, 95)
	private val literalColor = Color(145, 185, 35)
	private val numberColor = Color(0x6AC522)
	private val caretColor = Color(250, 180, 40)
	private val scrollBarColor = Color(50, 50, 50)
	private val scrollButtonColor = Color(35, 35, 35)
	private val fontSize = 16
	val autoSaveTimer = 1_500

	init {
		defaultCloseOperation = DO_NOTHING_ON_CLOSE
		layout = BorderLayout()
		minimumSize = Dimension(500, 330)
		iconImage = if (File("src/main/resources/icon.png").exists()) {
			ImageIcon("src/main/resources/icon.png").image
		} else {
			ImageIcon("icon.png").image
		}
		background = editorBackground

		val leftPanel = JPanel()
		leftPanel.preferredSize = Dimension(900, 700)
		leftPanel.background = editorBackground
		leftPanel.layout = BorderLayout()

		editorPane = createTextPane()
		editorPane.background = editorBackground
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

		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent) {
				File("autosave.txt").writeText(editorPane.text)
				exitProcess(0)
			}
		})

		val noWrapPanel = JPanel(BorderLayout())
		noWrapPanel.add(editorPane)

		val editorScroller = JScrollPane(noWrapPanel)
		editorScroller.background = editorBackground
		editorScroller.verticalScrollBar.background = editorBackground
		editorScroller.horizontalScrollBar.background = editorBackground
		editorScroller.verticalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				scrollBarWidth = 15
				thumbColor = scrollBarColor
			}

			override fun createDecreaseButton(orientation: Int): JButton? {
				val button = super.createDecreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}

			override fun createIncreaseButton(orientation: Int): JButton? {
				val button = super.createIncreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}
		})
		editorScroller.horizontalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				scrollBarWidth = 15
				thumbColor = scrollBarColor
			}

			override fun createDecreaseButton(orientation: Int): JButton? {
				val button = super.createDecreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}

			override fun createIncreaseButton(orientation: Int): JButton? {
				val button = super.createIncreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}
		})
		editorScroller.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		leftPanel.add(editorScroller, BorderLayout.CENTER)

		val font = Font("Fira code", Font.PLAIN, fontSize)

		outputPane = JTextArea()
		outputPane.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		outputPane.background = editorBackground
		outputPane.font = font
		outputPane.foreground = fontForeground

		outputScroller = JScrollPane(outputPane)
		outputScroller.background = editorBackground
		outputScroller.verticalScrollBar.background = editorBackground
		outputScroller.horizontalScrollBar.background = editorBackground
		outputScroller.verticalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				scrollBarWidth = 15
				thumbColor = scrollBarColor
			}

			override fun createDecreaseButton(orientation: Int): JButton? {
				val button = super.createDecreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}

			override fun createIncreaseButton(orientation: Int): JButton? {
				val button = super.createIncreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}
		})
		outputScroller.horizontalScrollBar.setUI(object : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				scrollBarWidth = 15
				thumbColor = scrollBarColor
			}

			override fun createDecreaseButton(orientation: Int): JButton? {
				val button = super.createDecreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}

			override fun createIncreaseButton(orientation: Int): JButton? {
				val button = super.createIncreaseButton(orientation)
				button.background = scrollButtonColor
				button.foreground = editorBackground
				return button
			}
		})
		outputScroller.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		val rightPanel = JPanel()
		rightPanel.preferredSize = Dimension(350, 700)
		rightPanel.background = editorBackground
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
						g.color = scrollBarColor
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
		menuBar.background = editorBackground

		val fileMenu = JMenu("File")
		fileMenu.font = font
		fileMenu.foreground = fontForeground
		fileMenu.background = editorBackground
		fileMenu.mnemonic = VK_F
		menuBar.add(fileMenu)

		val runMenu = JMenu("Run")
		runMenu.font = font
		runMenu.foreground = fontForeground
		runMenu.background = editorBackground
		runMenu.mnemonic = VK_R
		menuBar.add(runMenu)

		val runItem = JMenuItem("Run")
		runItem.font = font
		runItem.foreground = fontForeground
		runItem.background = editorBackground
		runItem.mnemonic = VK_R
		runMenu.add(runItem)

		val terminateItem = JMenuItem("Terminate")
		terminateItem.font = font
		terminateItem.foreground = fontForeground
		terminateItem.background = editorBackground
		terminateItem.mnemonic = VK_T
		runMenu.add(terminateItem)

		val autoScrollItem = JMenuItem("Auto scroll: false")
		autoScrollItem.font = font
		autoScrollItem.foreground = fontForeground
		autoScrollItem.background = editorBackground
		autoScrollItem.mnemonic = VK_T
		runMenu.add(autoScrollItem)

		val openItem = JMenuItem("Open")
		openItem.font = font
		openItem.foreground = fontForeground
		openItem.background = editorBackground
		openItem.mnemonic = VK_O
		fileMenu.add(openItem)

		val saveItem = JMenuItem("Save")
		saveItem.font = font
		saveItem.foreground = fontForeground
		saveItem.background = editorBackground
		saveItem.mnemonic = VK_S
		fileMenu.add(saveItem)

		val saveAsItem = JMenuItem("Save as")
		saveAsItem.font = font
		saveAsItem.foreground = fontForeground
		saveAsItem.background = editorBackground
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
			terminate(-1)
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

		editorPane.text = File("autosave.txt").readText()
		setText()
	}

	private fun createTextPane(): JTextPane {
		val textPane = JTextPane()

		val styDoc = textPane.styledDocument
		val def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE)

		val regular = styDoc.addStyle("regular", def)
		StyleConstants.setFontFamily(def, "Fira code")
		StyleConstants.setForeground(def, fontForeground)
		StyleConstants.setFontSize(def, fontSize)

		var s = styDoc.addStyle("variable", regular)
		StyleConstants.setForeground(s, variableColor)

		s = styDoc.addStyle("keyword", regular)
		StyleConstants.setForeground(s, keywordColor)
		StyleConstants.setBold(s, true)

		s = styDoc.addStyle("assignment", regular)
		StyleConstants.setForeground(s, assignmentColor)

		s = styDoc.addStyle("bracket", regular)
		StyleConstants.setForeground(s, bracketColor)
		StyleConstants.setBold(s, true)

		s = styDoc.addStyle("normal bracket", regular)
		StyleConstants.setForeground(s, normalBracketColor)

		s = styDoc.addStyle("semicolon", regular)
		StyleConstants.setForeground(s, semicolonColor)
		StyleConstants.setItalic(s, true)

		s = styDoc.addStyle("comment", regular)
		StyleConstants.setForeground(s, commentColor)

		s = styDoc.addStyle("literal", regular)
		StyleConstants.setForeground(s, literalColor)

		s = styDoc.addStyle("number", regular)
		StyleConstants.setForeground(s, numberColor)

		return textPane
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
					input.foreground = fontForeground
					input.background = editorBackground
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
				}, { x ->
					terminate(x)
					cancel(true)
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
