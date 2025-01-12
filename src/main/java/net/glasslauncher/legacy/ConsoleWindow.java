package net.glasslauncher.legacy;

import net.glasslauncher.common.ConsoleStream;
import net.glasslauncher.common.Pastee;
import net.glasslauncher.legacy.components.templates.JButtonScaling;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ConsoleWindow extends JFrame {
    private JTextArea textArea = new JTextArea();
    private final PrintStream oldout = System.out;
    private final PrintStream olderr = System.err;
    private final ConsoleStream pin = new ConsoleStream(textArea, oldout);
    private final ConsoleStream pin2 = new ConsoleStream(textArea, olderr);

    public ConsoleWindow() {
        // create all components and add them
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setTitle("Glass Launcher Console");
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                  if (!Main.isLauncherActive()) {
                                      Main.LOGGER.info("Closing...");
                                      System.exit(0);
                                  }
                                  Main.LOGGER.info("Close the launcher using the main window!");
                              }
                          }
        );
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(ConsoleWindow.class.getResource("assets/glass.png").toURI().toURL()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        setBounds(0, 0, 600, 300);

        textArea.setEditable(false);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.lightGray);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane textContainer = new JScrollPane(textArea);

        JButtonScaling uploadButton = new JButtonScaling();
        uploadButton.setText("Upload log to paste.ee");
        uploadButton.setOpaque(false);
        uploadButton.addActionListener(event -> {
            Pastee paste = new Pastee(textArea.getText());
            String url = paste.post();
            if (url != null) {
                new OpenLinkWindow(this, url);
            } else {
                JOptionPane.showMessageDialog(null, "An error occurred while uploading. Try again later.");
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(textContainer);
        add(panel, BorderLayout.CENTER);
        add(uploadButton, BorderLayout.SOUTH);

        setLocation(5, 10);
        setVisible(true);

        System.setOut(new PrintStream(this.pin, true));
        System.setErr(new PrintStream(this.pin2, true));
    }
}