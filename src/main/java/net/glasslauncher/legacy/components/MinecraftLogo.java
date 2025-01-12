package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Main;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class MinecraftLogo extends JPanel {
    private Image bgimage;

    public MinecraftLogo() {
        setOpaque(true);
        try {
            BufferedImage logoimg = ImageIO.read(Main.class.getResource("assets/logo.png"));
            int w = logoimg.getWidth();
            int h = logoimg.getHeight();
            bgimage = logoimg.getScaledInstance(w, h, 16);
            setPreferredSize(new Dimension(w + 32, h + 32));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(this.bgimage, 24, 24, null);
    }

}
