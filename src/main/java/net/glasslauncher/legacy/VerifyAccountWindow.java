package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.mc.LaunchArgs;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class VerifyAccountWindow extends JDialog {
    @Getter boolean loginValid = false;

    protected JPasswordField password;
    protected HintTextField username;

    public VerifyAccountWindow(Window frame) {
        super(frame);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Verify Account");

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(186, 100));

        // Username field
        username = new HintTextField("Username or Email");
        if (Config.getLauncherConfig().getLastUsedName() != null) {
            username.setText(Config.getLauncherConfig().getLastUsedName());
        }
        username.setBounds(10, 14, 166, 22);
        username.addActionListener((e) -> {
            login();
        });

        // Password field
        password = new JPasswordField();
        password.setEchoChar((char) 0);
        password.setForeground(Color.gray);
        password.setText("Password");

        password.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (password.getForeground() == Color.gray) {
                    password.setText("");
                    password.setForeground(Color.black);
                    password.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ((String.valueOf(password.getPassword()).isEmpty())) {
                    password.setText("Password");
                    password.setForeground(Color.gray);
                    password.setEchoChar((char) 0);
                }
            }
        });
        password.addActionListener((e) -> {
            login();
        });

        password.setBounds(10, 40, 166, 22);

        // Login button
        JButton login = new JButton();
        login.setText("Login");
        login.setBounds(58, 64, 70, 22);
        login.setOpaque(false);
        login.addActionListener(event -> {
            login();
        });

        panel.add(username);
        panel.add(password);
        panel.add(login);

        add(panel);
        pack();
        setLocationRelativeTo(frame);

        setVisible(true);
    }

    private void login() {
        String pass = "";
        if (password.getForeground() != Color.gray) {
            pass = String.valueOf(password.getPassword());
        }
        if (!pass.isEmpty() && (new LaunchArgs()).login(username.getText(), pass) != null) {
            Config.getLauncherConfig().setLastUsedName(username.getText());
            Config.getLauncherConfig().saveFile();
            loginValid = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
            password.setText("");
        }
    }
}
