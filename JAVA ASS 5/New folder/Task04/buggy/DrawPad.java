import java.awt.*;
import java.awt.event.*;

// ORIGINAL BUGGY VERSION (imports added so that it compiles). It compiles, but does not work.
public class DrawPad extends Frame {
    int count = 0;
    Label status = new Label("Clicks: 0");

    public DrawPad() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) {          // wrong name: lower-case c
                count++;
                Graphics g = getGraphics();
                g.fillOval(e.getX(), e.getY(), 6, 6);
            }
        });
        setSize(400, 300);
        setVisible(true);
    }
}
