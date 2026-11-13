import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class NokiaApp extends MIDlet {
    private Display display;
    private NokiaScreen screen;

    public void startApp() {
        display = Display.getDisplay(this);
        screen = new NokiaScreen();
        display.setCurrent(screen);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    private class NokiaScreen extends Canvas {
        private int selected = 0;
        private String[] items = {
            "Start",
            "About",
            "Settings",
            "Exit"
        };

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            g.setColor(15, 25, 40);
            g.fillRect(0, 0, w, h);

            g.setColor(0, 110, 170);
            g.fillRect(0, 0, w, 48);

            g.setColor(255, 255, 255);
            g.setFont(Font.getFont(
                Font.FACE_SYSTEM,
                Font.STYLE_BOLD,
                Font.SIZE_LARGE
            ));
            g.drawString(
                "NOKIA 6300",
                w / 2,
                12,
                Graphics.TOP | Graphics.HCENTER
            );

            g.setFont(Font.getDefaultFont());
            for (int i = 0; i < items.length; i++) {
                int y = 75 + i * 45;
                if (selected == i) {
                    g.setColor(0, 130, 210);
                    g.fillRoundRect(15, y, w - 30, 36, 8, 8);
                }
                g.setColor(255, 255, 255);
                g.drawString(
                    items[i],
                    30,
                    y + 10,
                    Graphics.TOP | Graphics.LEFT
                );
            }

            g.setColor(180, 180, 180);
            g.drawLine(0, h - 27, w, h - 27);
            g.drawString(
                "Select",
                5,
                h - 22,
                Graphics.TOP | Graphics.LEFT
            );
            g.drawString(
                "Exit",
                w - 5,
                h - 22,
                Graphics.TOP | Graphics.RIGHT
            );
        }

        protected void keyPressed(int keyCode) {
            int action = getGameAction(keyCode);

            if (action == UP) {
                selected--;
                if (selected < 0) {
                    selected = items.length - 1;
                }
            } else if (action == DOWN) {
                selected++;
                if (selected >= items.length) {
                    selected = 0;
                }
            } else if (action == FIRE) {
                if (selected == 3) {
                    notifyDestroyed();
                    return;
                }

                Alert alert;
                if (selected == 0) {
                    alert = new Alert(
                        "Welcome",
                        "Your Nokia Java application works!",
                        null,
                        AlertType.INFO
                    );
                } else if (selected == 1) {
                    alert = new Alert(
                        "About",
                        "Created with Java ME for Nokia 6300.",
                        null,
                        AlertType.INFO
                    );
                } else {
                    alert = new Alert(
                        "Settings",
                        "Version 1.0",
                        null,
                        AlertType.INFO
                    );
                }

                alert.setTimeout(Alert.FOREVER);
                Display.getDisplay(NokiaApp.this).setCurrent(alert, this);
            }

            repaint();
        }
    }
}
