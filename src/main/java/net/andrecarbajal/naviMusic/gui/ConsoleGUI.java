package net.andrecarbajal.naviMusic.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleGUI {
    private final JFrame frame;
    private final JTextArea textArea;
    private boolean isVisible;

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
        if (isVisible) {
            OutputStream out = new OutputStream() {
                @Override
                public void write(int b) {
                    updateTextArea(String.valueOf((char) b));
                }

                @Override
                public void write(byte @NotNull [] b, int off, int len) {
                    updateTextArea(new String(b, off, len, StandardCharsets.UTF_16));
                }

                @Override
                public void write(byte @NotNull [] b) {
                    write(b, 0, b.length);
                }
            };
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_16));
            System.setErr(new PrintStream(out, true, StandardCharsets.UTF_16));
        }
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        this.isVisible = visible;
        redirectSystemStreams();
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}