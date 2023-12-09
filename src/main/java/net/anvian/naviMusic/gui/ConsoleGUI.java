package net.anvian.naviMusic.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleGUI {
    private final JFrame frame;
    private final JTextArea textArea;

    public ConsoleGUI() {
        frame = new JFrame("Navi Music Console");
        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        redirectSystemStreams();
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(@NotNull byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(@NotNull byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}