import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Random;

public class Tetris extends MIDlet {
    private TetrisCanvas canvas;

    public void startApp() {
        if (canvas == null) canvas = new TetrisCanvas();
        Display.getDisplay(this).setCurrent(canvas);
        canvas.startGameLoop();
    }

    public void pauseApp() {
        if (canvas != null) canvas.pauseGame();
    }

    public void destroyApp(boolean unconditional) {
        if (canvas != null) canvas.pauseGame();
    }

    private class TetrisCanvas extends Canvas implements Runnable {
        private static final int COLS = 10;
        private static final int ROWS = 20;
        private static final int CELL = 10;

        // Seven standard tetrominoes, represented as 4x4 matrices.
        private final int[][][] shapes = {
            {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}}, // I
            {{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // J
            {{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // L
            {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}, // O
            {{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}}, // S
            {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // T
            {{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}  // Z
        };

        private int[][] board = new int[ROWS][COLS];
        private int[][] piece = new int[4][4];
        private int pieceX, pieceY;
        private int score, lines;
        private boolean running, gameOver, started;
        private Thread thread;
        private Random random = new Random();

        TetrisCanvas() {
            setFullScreenMode(true);
            resetGame();
        }

        private void resetGame() {
            int r, c;
            for (r = 0; r < ROWS; r++)
                for (c = 0; c < COLS; c++)
                    board[r][c] = 0;
            score = 0;
            lines = 0;
            gameOver = false;
            started = false;
            spawnPiece();
            repaint();
        }

        private void spawnPiece() {
            int index = (random.nextInt() & 0x7fffffff) % shapes.length;
            int r, c;
            for (r = 0; r < 4; r++)
                for (c = 0; c < 4; c++)
                    piece[r][c] = shapes[index][r][c];
            pieceX = 3;
            pieceY = 0;
            if (collides(pieceX, pieceY, piece)) {
                gameOver = true;
                started = false;
            }
        }

        private boolean collides(int x, int y, int[][] p) {
            int r, c, bx, by;
            for (r = 0; r < 4; r++) {
                for (c = 0; c < 4; c++) {
                    if (p[r][c] != 0) {
                        bx = x + c;
                        by = y + r;
                        if (bx < 0 || bx >= COLS || by >= ROWS) return true;
                        if (by >= 0 && board[by][bx] != 0) return true;
                    }
                }
            }
            return false;
        }

        private void move(int dx, int dy) {
            if (!started || gameOver) return;
            if (!collides(pieceX + dx, pieceY + dy, piece)) {
                pieceX += dx;
                pieceY += dy;
            } else if (dy > 0) {
                lockPiece();
            }
            repaint();
        }

        private void rotatePiece() {
            if (!started || gameOver) return;
            int[][] rotated = new int[4][4];
            int r, c;
            for (r = 0; r < 4; r++)
                for (c = 0; c < 4; c++)
                    rotated[c][3 - r] = piece[r][c];

            if (!collides(pieceX, pieceY, rotated)) {
                for (r = 0; r < 4; r++)
                    for (c = 0; c < 4; c++)
                        piece[r][c] = rotated[r][c];
            }
            repaint();
        }

        private void lockPiece() {
            int r, c, bx, by;
            for (r = 0; r < 4; r++) {
                for (c = 0; c < 4; c++) {
                    if (piece[r][c] != 0) {
                        bx = pieceX + c;
                        by = pieceY + r;
                        if (by >= 0 && by < ROWS && bx >= 0 && bx < COLS)
                            board[by][bx] = 1;
                    }
                }
            }
            clearLines();
            spawnPiece();
        }

        private void clearLines() {
            int r, c, full, write = ROWS - 1;
            int cleared = 0;
            for (r = ROWS - 1; r >= 0; r--) {
                full = 1;
                for (c = 0; c < COLS; c++) {
                    if (board[r][c] == 0) {
                        full = 0;
                        break;
                    }
                }
                if (full == 1) {
                    cleared++;
                } else {
                    if (write != r) {
                        for (c = 0; c < COLS; c++)
                            board[write][c] = board[r][c];
                    }
                    write--;
                }
            }
            while (write >= 0) {
                for (c = 0; c < COLS; c++) board[write][c] = 0;
                write--;
            }
            if (cleared > 0) {
                lines += cleared;
                score += cleared * cleared * 100;
            }
        }

        public void run() {
            while (running) {
                if (started && !gameOver) {
                    moveDownTick();
                }
                repaint();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // The loop checks running again.
                }
            }
        }

        private void moveDownTick() {
            if (!collides(pieceX, pieceY + 1, piece)) {
                pieceY++;
            } else {
                lockPiece();
            }
        }

        void startGameLoop() {
            if (thread == null || !thread.isAlive()) {
                running = true;
                thread = new Thread(this);
                thread.start();
            }
        }

        void pauseGame() {
            running = false;
        }

        protected void keyPressed(int keyCode) {
            int action = getGameAction(keyCode);

            if (keyCode == KEY_NUM5 || action == FIRE) {
                if (gameOver) resetGame();
                started = true;
                startGameLoop();
                repaint();
                return;
            }

            if (keyCode == KEY_NUM4 || action == LEFT) {
                move(-1, 0);
            } else if (keyCode == KEY_NUM6 || action == RIGHT) {
                move(1, 0);
            } else if (keyCode == KEY_NUM8 || action == DOWN) {
                moveDownTick();
                repaint();
            } else if (keyCode == KEY_NUM2 || action == UP) {
                rotatePiece();
            }
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            int boardW = COLS * CELL;
            int boardH = ROWS * CELL;
            int left = (w - boardW) / 2;
            int top = (h - boardH) / 2 + 8;
            int r, c;

            g.setColor(0, 0, 0);
            g.fillRect(0, 0, w, h);

            g.setColor(255, 255, 255);
            g.drawString("TETRIS", w / 2, 0, Graphics.TOP | Graphics.HCENTER);
            g.drawString("S:" + score + " L:" + lines, w / 2, 15,
                         Graphics.TOP | Graphics.HCENTER);

            g.setColor(80, 80, 80);
            g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

            for (r = 0; r < ROWS; r++) {
                for (c = 0; c < COLS; c++) {
                    if (board[r][c] != 0) {
                        drawCell(g, left + c * CELL, top + r * CELL, 80, 170, 255);
                    }
                }
            }

            if (!gameOver) {
                for (r = 0; r < 4; r++) {
                    for (c = 0; c < 4; c++) {
                        if (piece[r][c] != 0) {
                            int px = pieceX + c;
                            int py = pieceY + r;
                            if (py >= 0)
                                drawCell(g, left + px * CELL, top + py * CELL,
                                         255, 190, 50);
                        }
                    }
                }
            }

            if (!started) {
                g.setColor(255, 255, 255);
                String msg = gameOver ? "GAME OVER" : "TETRIS";
                g.drawString(msg, w / 2, h / 2 - 18,
                             Graphics.HCENTER | Graphics.TOP);
                g.drawString(gameOver ? "5 - RESTART" : "5 - START",
                             w / 2, h / 2 + 2,
                             Graphics.HCENTER | Graphics.TOP);
            } else {
                g.setColor(200, 200, 200);
                g.drawString("2 ROTATE  4/6 MOVE  8 DOWN",
                             w / 2, h - 15,
                             Graphics.BOTTOM | Graphics.HCENTER);
            }
        }

        private void drawCell(Graphics g, int x, int y, int red, int green, int blue) {
            g.setColor(red, green, blue);
            g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
            g.setColor(255, 255, 255);
            g.drawRect(x + 1, y + 1, CELL - 2, CELL - 2);
        }
    }
}
