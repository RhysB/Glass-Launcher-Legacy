package net.glasslauncher.legacy.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HintTextField extends JTextField {
    private final Color defaultColor;
    private final Color hintColor;
    private final String hint;

    public HintTextField(String hint) {
        this(hint, Color.BLACK, Color.GRAY);
    }

    public HintTextField(String hint, Color defaultColor, Color hintColor) {
        this.defaultColor = defaultColor;
        this.hintColor = hintColor;
        this.hint = hint;
        this.setText(hint);
        this.setToolTipText(hint);
        this.setForeground(hintColor);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (getForeground() == hintColor) {
                    HintTextField.super.setText("");
                    setForeground(defaultColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(hint);
                    setForeground(hintColor);
                }
            }
        });
    }

    public void setText(String text) {
        if (text.isEmpty()) {
            setText(hint);
            setForeground(hintColor);
        }
        else {
            super.setText(text);
            setForeground(defaultColor);
        }
    }

    public String getText() {
        if (defaultColor != getForeground() && super.getText().equals(hint)) {
            return "";
        }
        else {
            return super.getText();
        }
    }
}
