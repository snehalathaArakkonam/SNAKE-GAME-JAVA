# 🐍 Snake Game — Java Mini Project

> A classic Snake Game built with **Java Swing** and **Graphics2D**, featuring smooth animation, score tracking, and keyboard controls.

---

## 📌 Project Overview

| Field            | Details                            |
|------------------|------------------------------------|
| **Project Name** | Snake Game                         |
| **Language**     | Java (JDK 8+)                      |
| **Type**         | Mini Project / Desktop Game        |
| **Domain**       | GUI Application / Game Development |
| **Difficulty**   | Beginner–Intermediate              |

---

## 🎯 Objective

To build a fully functional Snake Game using core Java concepts such as:
- **Java Swing** for GUI window and rendering panel
- **Graphics2D** for drawing the snake, food, and grid
- **javax.swing.Timer** for game loop / animation
- **KeyListener** for keyboard input
- **ArrayList / Arrays** for snake body (linked-list style logic)
- **Random** for food placement

---

## 🛠️ Key Technologies Used

| Technology           | Purpose                                      |
|----------------------|----------------------------------------------|
| `javax.swing.JFrame` | Main game window                             |
| `javax.swing.JPanel` | Game canvas where everything is drawn        |
| `java.awt.Graphics2D`| Drawing snake segments, food, grid, text     |
| `javax.swing.Timer`  | Game loop — repaints screen every N ms       |
| `java.awt.event.KeyListener` | Captures arrow key inputs          |
| `java.util.ArrayList`| Stores snake body coordinates                |
| `java.util.Random`   | Spawns food at random positions              |

---

## 📂 Project Structure

```
SnakeGame/
│
├── SnakeGame.java       # Main class — JFrame setup + entry point
├── GamePanel.java       # Core game logic + rendering (JPanel)
└── README.md            # Project documentation
```

> 💡 You can also combine everything into a single `SnakeGame.java` file for simplicity.

---

## 🎮 Game Rules

- Snake starts at the center of the screen moving **right**
- Every time the snake eats food 🟥, it **grows by 1 segment**
- Score increases by **10 points** per food eaten
- Game ends if the snake:
  - Hits the **wall / boundary**
  - Hits its **own body**
- Press **R** to restart after game over

---

## ⌨️ Controls

| Key          | Action              |
|--------------|---------------------|
| `↑` Arrow    | Move Up             |
| `↓` Arrow    | Move Down           |
| `←` Arrow    | Move Left           |
| `→` Arrow    | Move Right          |
| `R`          | Restart Game        |

---

## 💻 Complete Source Code

### `SnakeGame.java` (Entry Point)

```java
import javax.swing.JFrame;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        GamePanel gamePanel = new GamePanel();

        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
}
```

---

### `GamePanel.java` (Core Logic + Rendering)

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ─── Constants ───────────────────────────────────────────────
    static final int SCREEN_WIDTH  = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE     = 25;   // size of each grid cell
    static final int GAME_UNITS    = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY         = 120;  // milliseconds per frame (lower = faster)

    // ─── Snake Body ───────────────────────────────────────────────
    ArrayList<Point> snake = new ArrayList<>();

    // ─── Food ─────────────────────────────────────────────────────
    Point food;

    // ─── Direction ────────────────────────────────────────────────
    char direction = 'R'; // U, D, L, R
    char newDirection = 'R';

    // ─── Game State ───────────────────────────────────────────────
    boolean running = false;
    int score = 0;

    // ─── Timer (Game Loop) ────────────────────────────────────────
    Timer timer;
    Random random = new Random();

    // ─── Constructor ─────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        startGame();
    }

    // ─── Start / Restart Game ────────────────────────────────────
    public void startGame() {
        snake.clear();
        score = 0;
        direction = 'R';
        newDirection = 'R';

        // Initial snake: 3 segments at center
        int startX = (SCREEN_WIDTH / 2 / UNIT_SIZE) * UNIT_SIZE;
        int startY = (SCREEN_HEIGHT / 2 / UNIT_SIZE) * UNIT_SIZE;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - UNIT_SIZE, startY));
        snake.add(new Point(startX - 2 * UNIT_SIZE, startY));

        spawnFood();
        running = true;

        if (timer != null) timer.stop();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    // ─── Spawn Food at Random Grid Position ──────────────────────
    public void spawnFood() {
        int x, y;
        do {
            x = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            y = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            food = new Point(x, y);
        } while (snake.contains(food)); // Avoid spawning on snake
    }

    // ─── Game Loop (called every DELAY ms by Timer) ──────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            direction = newDirection;
            move();
            checkFood();
            checkCollision();
        }
        repaint();
    }

    // ─── Move Snake ──────────────────────────────────────────────
    public void move() {
        Point head = snake.get(0);
        Point newHead;

        switch (direction) {
            case 'U': newHead = new Point(head.x, head.y - UNIT_SIZE); break;
            case 'D': newHead = new Point(head.x, head.y + UNIT_SIZE); break;
            case 'L': newHead = new Point(head.x - UNIT_SIZE, head.y); break;
            default:  newHead = new Point(head.x + UNIT_SIZE, head.y); break; // 'R'
        }

        snake.add(0, newHead);       // Add new head
        snake.remove(snake.size()-1); // Remove tail
    }

    // ─── Check if Snake Ate Food ─────────────────────────────────
    public void checkFood() {
        if (snake.get(0).equals(food)) {
            score += 10;
            // Grow: add tail copy back
            snake.add(new Point(snake.get(snake.size()-1)));
            spawnFood();
        }
    }

    // ─── Check Wall & Self Collision ─────────────────────────────
    public void checkCollision() {
        Point head = snake.get(0);

        // Wall collision
        if (head.x < 0 || head.x >= SCREEN_WIDTH ||
            head.y < 0 || head.y >= SCREEN_HEIGHT) {
            running = false;
            timer.stop();
            return;
        }

        // Self collision
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                timer.stop();
                return;
            }
        }
    }

    // ─── Rendering ───────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        if (running) {
            drawGrid(g2d);
            drawFood(g2d);
            drawSnake(g2d);
            drawScore(g2d);
        } else {
            drawGrid(g2d);
            drawGameOver(g2d);
        }
    }

    // Draw subtle grid
    public void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 30));
        for (int x = 0; x < SCREEN_WIDTH; x += UNIT_SIZE) {
            g2d.drawLine(x, 0, x, SCREEN_HEIGHT);
        }
        for (int y = 0; y < SCREEN_HEIGHT; y += UNIT_SIZE) {
            g2d.drawLine(0, y, SCREEN_WIDTH, y);
        }
    }

    // Draw food as red circle
    public void drawFood(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillOval(food.x + 2, food.y + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

        // Glow effect
        g2d.setColor(new Color(255, 80, 80, 80));
        g2d.fillOval(food.x - 2, food.y - 2, UNIT_SIZE + 4, UNIT_SIZE + 4);
    }

    // Draw snake body
    public void drawSnake(Graphics2D g2d) {
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                // Head — brighter green
                g2d.setColor(new Color(0, 220, 0));
            } else {
                // Body — darker green
                g2d.setColor(new Color(0, 150, 0));
            }
            g2d.fillRoundRect(p.x + 1, p.y + 1,
                              UNIT_SIZE - 2, UNIT_SIZE - 2,
                              8, 8); // Rounded corners
        }
    }

    // Draw score at top
    public void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        g2d.drawString("Score: " + score, 10, 22);
    }

    // Draw game over screen
    public void drawGameOver(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // "GAME OVER" text
        g2d.setFont(new Font("Consolas", Font.BOLD, 60));
        g2d.setColor(Color.RED);
        FontMetrics fm = g2d.getFontMetrics();
        String gameOver = "GAME OVER";
        int x = (SCREEN_WIDTH - fm.stringWidth(gameOver)) / 2;
        g2d.drawString(gameOver, x, SCREEN_HEIGHT / 2 - 30);

        // Score
        g2d.setFont(new Font("Consolas", Font.PLAIN, 28));
        g2d.setColor(Color.WHITE);
        String scoreText = "Score: " + score;
        fm = g2d.getFontMetrics();
        x = (SCREEN_WIDTH - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, x, SCREEN_HEIGHT / 2 + 20);

        // Restart hint
        g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2d.setColor(Color.LIGHT_GRAY);
        String restart = "Press R to Restart";
        fm = g2d.getFontMetrics();
        x = (SCREEN_WIDTH - fm.stringWidth(restart)) / 2;
        g2d.drawString(restart, x, SCREEN_HEIGHT / 2 + 65);
    }

    // ─── Key Input ───────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Prevent reversing direction
        if (key == KeyEvent.VK_UP    && direction != 'D') newDirection = 'U';
        if (key == KeyEvent.VK_DOWN  && direction != 'U') newDirection = 'D';
        if (key == KeyEvent.VK_LEFT  && direction != 'R') newDirection = 'L';
        if (key == KeyEvent.VK_RIGHT && direction != 'L') newDirection = 'R';

        // Restart
        if (key == KeyEvent.VK_R && !running) startGame();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
```

---

## ▶️ How to Run

### Step 1 — Compile

```bash
javac SnakeGame.java GamePanel.java
```

### Step 2 — Run

```bash
java SnakeGame
```

> ✅ Make sure both `.java` files are in the same folder before compiling.

---

## 📚 What You Will Learn From This Project

| Concept                | Where It's Used                                      |
|------------------------|------------------------------------------------------|
| **OOP**                | Two classes with clear responsibilities              |
| **Java Swing / JFrame**| Creating and managing the game window                |
| **JPanel + paintComponent** | Custom rendering / drawing on screen           |
| **Graphics2D**         | Drawing shapes, text, rounded rectangles, ovals      |
| **Timer (Game Loop)**  | Calling `actionPerformed` every 120ms for animation  |
| **ArrayList**          | Storing and updating snake body (linked-list feel)   |
| **KeyListener**        | Capturing arrow key events for direction change      |
| **Collision Detection**| Wall bounds + self-intersection check               |
| **Random**             | Spawning food at non-overlapping positions           |
| **State Management**   | `running` flag to switch between playing / game over |

---

## 🚀 Possible Enhancements (For Future Versions)

- [ ] Add **High Score** tracking with file save
- [ ] Add **difficulty levels** (speed increase per level)
- [ ] Add **sound effects** using `javax.sound.sampled`
- [ ] Add a **start screen** / splash screen
- [ ] Add **obstacles** on the board
- [ ] Export as **runnable JAR** file

---

## 👩‍💻 Author

**Sneha Lalitha**
CSE Student | Java Mini Project — Semester

---

## 📄 License

This project is open source and free to use for educational purposes.
