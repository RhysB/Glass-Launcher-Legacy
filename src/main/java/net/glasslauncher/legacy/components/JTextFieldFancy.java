package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;

import javax.swing.border.*;
import java.awt.*;

public class JTextFieldFancy extends HintTextField {

    public JTextFieldFancy(String hint) {
        super(hint, Config.getLauncherConfig().isThemeDisabled()? Color.BLACK : new Color(218, 218, 218), Color.GRAY);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setOpaque(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            g2d.setColor(new Color(22, 13, 16, 153));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, 4, 4);
            g2d.setColor(new Color(37, 30, 24, 255));
            g2d.fillRoundRect(1, 1, width - 2, height - 2, 4, 4);
        }
        super.paintComponent(g);
    }
}
