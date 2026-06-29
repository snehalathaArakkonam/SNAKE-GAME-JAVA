import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

// ============================================================
//   SNAKE GAME - Java Swing + Graphics2D
//   Concepts Covered:
//   -> Game Loop (javax.swing.Timer)
//   -> 2D Graphics Rendering (Graphics2D, RenderingHints)
//   -> Keyboard Input (KeyListener)
//   -> Data Structures (LinkedList for snake body)
//   -> OOP: Inner Classes, Encapsulation
// ============================================================

public class SnakeGame extends JFrame {

    public SnakeGame() {
        setTitle("🐍 Snake Game - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();

        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    public static void main(String[] args) {
        // Run on Event Dispatch Thread (Swing rule)
        SwingUtilities.invokeLater(() -> new SnakeGame());
    }
}

// ============================================================
//   GamePanel: Main game logic + rendering lives here
// ============================================================
class GamePanel extends JPanel implements ActionListener, KeyListener {

    // --- CONSTANTS ---
    static final int TILE_SIZE = 25;       // Each cell = 25px
    static final int BOARD_WIDTH = 24;     // 24 tiles wide  = 600px
    static final int BOARD_HEIGHT = 24;    // 24 tiles tall  = 600px
    static final int SCREEN_W = TILE_SIZE * BOARD_WIDTH;
    static final int SCREEN_H = TILE_SIZE * BOARD_HEIGHT;
    static final int GAME_SPEED = 130;     // Timer delay in ms (lower = faster)

    // --- DIRECTION CONSTANTS ---
    static final char UP    = 'U';
    static final char DOWN  = 'D';
    static final char LEFT  = 'L';
    static final char RIGHT = 'R';

    // --- GAME STATE ---
    LinkedList<Point> snake;   // Snake body: head = index 0
    Point food;
    char direction;
    char nextDirection;        // Buffered direction to prevent 180° reversal
    boolean running;
    boolean gameOver;
    int score;
    int highScore = 0;
    int level;
    javax.swing.Timer gameTimer;
    Random random;

    // --- COLORS (Dark Luxury Theme) ---
    Color BG_COLOR        = new Color(15, 15, 25);
    Color GRID_COLOR      = new Color(30, 30, 50);
    Color SNAKE_HEAD      = new Color(0, 230, 120);
    Color SNAKE_BODY_1    = new Color(0, 180, 90);
    Color SNAKE_BODY_2    = new Color(0, 140, 70);
    Color FOOD_COLOR      = new Color(255, 80, 80);
    Color FOOD_GLOW       = new Color(255, 80, 80, 60);
    Color SCORE_COLOR     = new Color(200, 200, 255);
    Color GAMEOVER_COLOR  = new Color(255, 60, 60);
    Color TITLE_COLOR     = new Color(0, 230, 120);

    // --------------------------------------------------------
    //   Constructor: initialize everything
    // --------------------------------------------------------
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setBackground(BG_COLOR);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        initGame();
    }

    // --------------------------------------------------------
    //   initGame: Reset all variables, start fresh
    // --------------------------------------------------------
    void initGame() {
        snake = new LinkedList<>();
        direction     = RIGHT;
        nextDirection = RIGHT;
        score  = 0;
        level  = 1;
        running   = true;
        gameOver  = false;

        // Start snake at center with 3 segments
        int startX = BOARD_WIDTH / 2;
        int startY = BOARD_HEIGHT / 2;
        snake.add(new Point(startX,     startY));  // Head
        snake.add(new Point(startX - 1, startY));  // Body
        snake.add(new Point(startX - 2, startY));  // Tail

        spawnFood();

        // Start game timer
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new javax.swing.Timer(GAME_SPEED, this);
        gameTimer.start();
    }

    // --------------------------------------------------------
    //   spawnFood: Place food at a random empty tile
    // --------------------------------------------------------
    void spawnFood() {
        int fx, fy;
        do {
            fx = random.nextInt(BOARD_WIDTH);
            fy = random.nextInt(BOARD_HEIGHT);
        } while (snake.contains(new Point(fx, fy))); // Don't spawn on snake
        food = new Point(fx, fy);
    }

    // --------------------------------------------------------
    //   actionPerformed: Called every GAME_SPEED ms (Game Loop)
    // --------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkFood();
            checkCollision();
        }
        repaint(); // Redraw screen
    }

    // --------------------------------------------------------
    //   move: Move snake one step in current direction
    // --------------------------------------------------------
    void move() {
        direction = nextDirection; // Apply buffered direction

        Point head = snake.getFirst();
        Point newHead;

        switch (direction) {
            case UP:    newHead = new Point(head.x, head.y - 1); break;
            case DOWN:  newHead = new Point(head.x, head.y + 1); break;
            case LEFT:  newHead = new Point(head.x - 1, head.y); break;
            default:    newHead = new Point(head.x + 1, head.y); break; // RIGHT
        }

        snake.addFirst(newHead);  // Add new head
        snake.removeLast();       // Remove tail (unless food eaten — handled in checkFood)
    }

    // --------------------------------------------------------
    //   checkFood: Did head touch food?
    // --------------------------------------------------------
    void checkFood() {
        if (snake.getFirst().equals(food)) {
            score += 10 * level;
            if (score > highScore) highScore = score;

            // Grow: add a segment at the tail
            Point tail = snake.getLast();
            snake.addLast(new Point(tail.x, tail.y));

            // Level up every 5 foods
            if ((score / (10 * level)) % 5 == 0) {
                level++;
                // Increase speed with level
                gameTimer.setDelay(Math.max(60, GAME_SPEED - (level - 1) * 10));
            }

            spawnFood();
        }
    }

    // --------------------------------------------------------
    //   checkCollision: Wall or self collision = game over
    // --------------------------------------------------------
    void checkCollision() {
        Point head = snake.getFirst();

        // Wall collision
        if (head.x < 0 || head.x >= BOARD_WIDTH ||
            head.y < 0 || head.y >= BOARD_HEIGHT) {
            endGame();
            return;
        }

        // Self collision (skip head itself — check from index 1)
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                endGame();
                return;
            }
        }
    }

    void endGame() {
        running  = false;
        gameOver = true;
        gameTimer.stop();
    }

    // --------------------------------------------------------
    //   paintComponent: ALL drawing happens here
    // --------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawFood(g2d);
        drawSnake(g2d);
        drawHUD(g2d);

        if (gameOver) drawGameOver(g2d);
        if (!running && !gameOver) drawStartScreen(g2d);
    }

    // --------------------------------------------------------
    //   drawBackground: Dark grid pattern
    // --------------------------------------------------------
    void drawBackground(Graphics2D g2d) {
        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // Subtle grid lines
        g2d.setColor(GRID_COLOR);
        for (int x = 0; x < BOARD_WIDTH; x++) {
            g2d.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, SCREEN_H);
        }
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            g2d.drawLine(0, y * TILE_SIZE, SCREEN_W, y * TILE_SIZE);
        }

        // Border glow
        g2d.setColor(SNAKE_HEAD);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(1, 1, SCREEN_W - 2, SCREEN_H - 2);
    }

    // --------------------------------------------------------
    //   drawFood: Glowing red food circle
    // --------------------------------------------------------
    void drawFood(Graphics2D g2d) {
        int fx = food.x * TILE_SIZE;
        int fy = food.y * TILE_SIZE;
        int padding = 3;

        // Glow effect (larger translucent circle behind)
        g2d.setColor(FOOD_GLOW);
        g2d.fillOval(fx - 4, fy - 4, TILE_SIZE + 8, TILE_SIZE + 8);

        // Main food circle
        g2d.setColor(FOOD_COLOR);
        g2d.fillOval(fx + padding, fy + padding,
                     TILE_SIZE - padding * 2, TILE_SIZE - padding * 2);

        // Shine highlight
        g2d.setColor(new Color(255, 200, 200, 180));
        g2d.fillOval(fx + padding + 3, fy + padding + 3, 5, 5);
    }

    // --------------------------------------------------------
    //   drawSnake: Segments with gradient color effect
    // --------------------------------------------------------
    void drawSnake(Graphics2D g2d) {
        int padding = 2;

        for (int i = 0; i < snake.size(); i++) {
            Point seg = snake.get(i);
            int sx = seg.x * TILE_SIZE + padding;
            int sy = seg.y * TILE_SIZE + padding;
            int size = TILE_SIZE - padding * 2;

            if (i == 0) {
                // Head: brighter green with eyes
                g2d.setColor(SNAKE_HEAD);
                g2d.fillRoundRect(sx, sy, size, size, 8, 8);

                // Draw eyes based on direction
                drawSnakeEyes(g2d, seg, direction);

            } else {
                // Body: alternating shades for scale effect
                Color bodyColor = (i % 2 == 0) ? SNAKE_BODY_1 : SNAKE_BODY_2;
                g2d.setColor(bodyColor);
                g2d.fillRoundRect(sx, sy, size, size, 6, 6);
            }
        }
    }

    // --------------------------------------------------------
    //   drawSnakeEyes: Eyes on head facing direction
    // --------------------------------------------------------
    void drawSnakeEyes(Graphics2D g2d, Point head, char dir) {
        int hx = head.x * TILE_SIZE;
        int hy = head.y * TILE_SIZE;
        int half = TILE_SIZE / 2;

        g2d.setColor(Color.BLACK);
        int eyeSize = 4;

        switch (dir) {
            case RIGHT:
                g2d.fillOval(hx + half + 4, hy + 4,       eyeSize, eyeSize);
                g2d.fillOval(hx + half + 4, hy + half + 4, eyeSize, eyeSize);
                break;
            case LEFT:
                g2d.fillOval(hx + 4, hy + 4,       eyeSize, eyeSize);
                g2d.fillOval(hx + 4, hy + half + 4, eyeSize, eyeSize);
                break;
            case UP:
                g2d.fillOval(hx + 4,       hy + 4, eyeSize, eyeSize);
                g2d.fillOval(hx + half + 4, hy + 4, eyeSize, eyeSize);
                break;
            case DOWN:
                g2d.fillOval(hx + 4,        hy + half + 4, eyeSize, eyeSize);
                g2d.fillOval(hx + half + 4, hy + half + 4, eyeSize, eyeSize);
                break;
        }
    }

    // --------------------------------------------------------
    //   drawHUD: Score, High Score, Level display
    // --------------------------------------------------------
    void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(SCORE_COLOR);
        g2d.drawString("SCORE: " + score, 10, 20);

        String hsText = "BEST: " + highScore;
        FontMetrics fm = g2d.getFontMetrics();
        int hsX = SCREEN_W - fm.stringWidth(hsText) - 10;
        g2d.drawString(hsText, hsX, 20);

        String lvlText = "LVL " + level;
        int lvlX = (SCREEN_W - fm.stringWidth(lvlText)) / 2;
        g2d.setColor(SNAKE_HEAD);
        g2d.drawString(lvlText, lvlX, 20);
    }

    // --------------------------------------------------------
    //   drawGameOver: Overlay screen on death
    // --------------------------------------------------------
    void drawGameOver(Graphics2D g2d) {
        // Dark overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // GAME OVER text
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(GAMEOVER_COLOR);
        String goText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int gx = (SCREEN_W - fm.stringWidth(goText)) / 2;
        g2d.drawString(goText, gx, SCREEN_H / 2 - 60);

        // Final score
        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2d.setColor(SCORE_COLOR);
        String finalScore = "Score: " + score + "  |  Best: " + highScore;
        fm = g2d.getFontMetrics();
        int sx = (SCREEN_W - fm.stringWidth(finalScore)) / 2;
        g2d.drawString(finalScore, sx, SCREEN_H / 2);

        // Restart prompt
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(new Color(150, 150, 200));
        String restart = "Press  ENTER  to Play Again";
        fm = g2d.getFontMetrics();
        int rx = (SCREEN_W - fm.stringWidth(restart)) / 2;
        g2d.drawString(restart, rx, SCREEN_H / 2 + 50);

        // Controls reminder
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(100, 100, 140));
        String ctrl = "Arrow Keys / WASD to move";
        fm = g2d.getFontMetrics();
        int cx = (SCREEN_W - fm.stringWidth(ctrl)) / 2;
        g2d.drawString(ctrl, cx, SCREEN_H / 2 + 80);
    }

    // --------------------------------------------------------
    //   drawStartScreen: First launch screen
    // --------------------------------------------------------
    void drawStartScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, SCREEN_W, SCREEN_H);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(TITLE_COLOR);
        String title = "SNAKE";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (SCREEN_W - fm.stringWidth(title)) / 2, SCREEN_H / 2 - 40);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(SCORE_COLOR);
        String start = "Press  ENTER  to Start";
        fm = g2d.getFontMetrics();
        g2d.drawString(start, (SCREEN_W - fm.stringWidth(start)) / 2, SCREEN_H / 2 + 20);
    }

    // --------------------------------------------------------
    //   keyPressed: Handle keyboard input
    // --------------------------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Movement — Arrow Keys
        if ((key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) && direction != DOWN)  nextDirection = UP;
        if ((key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) && direction != UP)    nextDirection = DOWN;
        if ((key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) && direction != RIGHT) nextDirection = LEFT;
        if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != LEFT)  nextDirection = RIGHT;

        // Enter = restart / start
        if (key == KeyEvent.VK_ENTER) {
            initGame();
        }

        // ESC = quit
        if (key == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}