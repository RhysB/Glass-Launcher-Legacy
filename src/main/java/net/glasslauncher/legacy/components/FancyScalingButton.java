package net.glasslauncher.legacy.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FancyScalingButton extends ScalingButton {

    public FancyScalingButton() {
        setContentAreaFilled(false);
        setForeground(new Color(218, 218, 218));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.setColor(new Color(8, 5, 6, 255));
        g2d.drawRoundRect(0, 0, width-1, height-1, 4, 4);
        g2d.setColor(new Color(18, 13, 9, 255));
        g2d.fillRoundRect(0, 0, width-1, height-1, 4, 4);
        super.paintComponent(g);
    }
}