import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

// Draws LibraryPanel off-screen at three sizes (no window is needed) and writes PNG files plus
// a text report of the size of each region. Run it with:  java LayoutScreenshots
public class LayoutScreenshots {
    static void layoutAll(Component c) {
        c.doLayout();
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) layoutAll(k);
    }

    static Component find(Container c, Class<?> type) {
        for (Component k : c.getComponents()) {
            if (type.isInstance(k)) return k;
            if (k instanceof Container) { Component r = find((Container) k, type); if (r != null) return r; }
        }
        return null;
    }

    static JButton findButton(Container c, String text) {
        for (Component k : c.getComponents()) {
            if (k instanceof JButton && text.equals(((JButton) k).getText())) return (JButton) k;
            if (k instanceof Container) { JButton r = findButton((Container) k, text); if (r != null) return r; }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        new File("screenshots").mkdirs();
        int[][] sizes = { { 500, 320 }, { 800, 500 }, { 1100, 700 } };
        PrintWriter report = new PrintWriter("layout_report.txt");
        for (int[] s : sizes) {
            LibraryPanel p = new LibraryPanel();
            p.runSearch();
            p.setSize(s[0], s[1]);
            layoutAll(p);
            JScrollPane scroll = (JScrollPane) find(p, JScrollPane.class);
            JButton search = findButton(p, "Search");
            JTextField tf = (JTextField) find(p, JTextField.class);
            String line = "Window " + s[0] + "x" + s[1]
                    + " | results area " + scroll.getWidth() + "x" + scroll.getHeight()
                    + " | Search button " + search.getWidth() + "x" + search.getHeight()
                    + " | text field " + tf.getWidth() + "x" + tf.getHeight();
            System.out.println(line);
            report.println(line);
            BufferedImage img = new BufferedImage(s[0], s[1], BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE); g.fillRect(0, 0, s[0], s[1]);
            p.printAll(g);
            g.dispose();
            ImageIO.write(img, "png", new File("screenshots/resize_" + s[0] + "x" + s[1] + ".png"));
        }
        report.close();
    }
}
