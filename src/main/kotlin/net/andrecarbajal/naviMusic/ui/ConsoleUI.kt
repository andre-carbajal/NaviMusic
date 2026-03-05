package net.andrecarbajal.naviMusic.ui

import java.awt.BorderLayout
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class ConsoleUI {
    private val frame: JFrame = JFrame("Navi Music Console")
    private val textArea: JTextArea = JTextArea()
    private var isVisible: Boolean = false

    init {
        textArea.isEditable = false
        frame.add(JScrollPane(textArea), BorderLayout.CENTER)
        frame.setSize(500, 300)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        redirectSystemStreams()
    }

    private fun redirectSystemStreams() {
        if (isVisible) {
            val out = object : OutputStream() {
                override fun write(b: Int) {
                    updateTextArea(b.toChar().toString())
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    updateTextArea(String(b, off, len, StandardCharsets.UTF_8))
                }

                override fun write(b: ByteArray) {
                    write(b, 0, b.size)
                }
            }
            System.setOut(PrintStream(out, true, StandardCharsets.UTF_8))
            System.setErr(PrintStream(out, true, StandardCharsets.UTF_8))
        }
    }

    fun setVisible(visible: Boolean) {
        frame.isVisible = visible
        this.isVisible = visible
        redirectSystemStreams()
    }

    private fun updateTextArea(text: String) {
        SwingUtilities.invokeLater { textArea.append(text) }
    }
}
