import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Random;

public class Snake extends MIDlet {
    private SnakeGame game;

    public void startApp() {
        if (game == null) game = new SnakeGame();
        Display.getDisplay(this).setCurrent(game);
    }

    public void pauseApp() {
        if (game != null) game.stopGame();
    }

    public void destroyApp(boolean unconditional) {
        if (game != null) game.stopGame();
    }

    private class SnakeGame extends Canvas implements Runnable {
        private final int CELL = 10;
        private final int MAX = 750;
        private Random random = new Random();
        private int[] sx = new int[MAX];
        private int[] sy = new int[MAX];
        private int length, foodX, foodY, dx, dy, score;
        private boolean running, started, gameOver;
        private Thread thread;

        SnakeGame() {
            setFullScreenMode(true);
            resetGame();
        }

        private void resetGame() {
            length = 5;
            score = 0;
            dx = CELL;
            dy = 0;
            int w = getWidth();
            int h = getHeight();
            sx[0] = (w / CELL / 2) * CELL;
            sy[0] = (h / CELL / 2) * CELL;
            for (int i = 1; i < length; i++) {
                sx[i] = sx[0] - i * CELL;
                sy[i] = sy[0];
            }
            started = false;
            gameOver = false;
            createFood();
            repaint();
        }

        private void createFood() {
            int cols = getWidth() / CELL;
            int rows = getHeight() / CELL;

            boolean valid;

            do {
                valid = true;

                foodX = (random.nextInt() & 0x7fffffff) % cols * CELL;
                foodY = (random.nextInt() & 0x7fffffff) % rows * CELL;

                for (int i = 0; i < length; i++) {
                    if (sx[i] == foodX && sy[i] == foodY) {
                        valid = false;
                        break;
                    }
                }

            } while (!valid);
        }

        public void run() {
            while (running) {
                if (started && !gameOver) updateGame();
                repaint();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                }
            }
        }

        private void updateGame() {
            int nx = sx[0] + dx;
            int ny = sy[0] + dy;
            if (nx < 0 || ny < 25 || nx >= getWidth() || ny >= getHeight()) {
                endGame();
                return;
            }
            boolean eating = (nx == foodX && ny == foodY);
            for (int i = 0; i < length; i++) {
                if (nx == sx[i] && ny == sy[i]) {
                    if (i == length - 1 && !eating) continue;
                    endGame();
                    return;
                }
            }
            if (eating && length < MAX) {
                length++;
                score += 10;
                createFood();
            }
            for (int i = length - 1; i > 0; i--) {
                sx[i] = sx[i - 1];
                sy[i] = sy[i - 1];
            }
            sx[0] = nx;
            sy[0] = ny;
        }

        private void endGame() {
            gameOver = true;
            started = false;
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, w, h);
            g.setColor(0, 100, 0);
            g.fillRect(0, 0, w, 25);
            g.setColor(255, 255, 255);
            g.drawString("SCORE: " + score, 5, 5, Graphics.TOP | Graphics.LEFT);
            g.setColor(255, 50, 50);
            g.fillArc(foodX, foodY, CELL, CELL, 0, 360);
            for (int i = 0; i < length; i++) {
                if (i == 0) g.setColor(100, 255, 100);
                else g.setColor(0, 180, 0);
                g.fillRect(sx[i] + 1, sy[i] + 1, CELL - 2, CELL - 2);
            }
            if (!started) {
                g.setColor(255, 255, 255);
                String message = gameOver ? "GAME OVER" : "SNAKE GAME";
                g.drawString(message, w / 2, h / 2 - 25, Graphics.HCENTER | Graphics.TOP);
                g.drawString(gameOver ? "5 - RESTART" : "5 - START",
                    w / 2, h / 2 + 5, Graphics.HCENTER | Graphics.TOP);
            }
        }

        protected void keyPressed(int keyCode) {
            int action = getGameAction(keyCode);
            if (keyCode == KEY_NUM5 || action == FIRE) {
                if (gameOver) resetGame();
                started = true;
                if (thread == null || !thread.isAlive()) {
                    running = true;
                    thread = new Thread(this);
                    thread.start();
                }
                repaint();
                return;
            }
            if (action == UP || keyCode == KEY_NUM2) {
                if (dy == 0) { dx = 0; dy = -CELL; }
            } else if (action == DOWN || keyCode == KEY_NUM8) {
                if (dy == 0) { dx = 0; dy = CELL; }
            } else if (action == LEFT || keyCode == KEY_NUM4) {
                if (dx == 0) { dx = -CELL; dy = 0; }
            } else if (action == RIGHT || keyCode == KEY_NUM6) {
                if (dx == 0) { dx = CELL; dy = 0; }
            }
        }

        void stopGame() {
            running = false;
        }
    }
}
