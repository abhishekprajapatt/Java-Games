package Pacman;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Pacman extends JPanel implements ActionListener, KeyListener {
    
    private static class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'R';
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char newDirection, HashSet<Block> walls, int tileSize) {
            char prevDirection = this.direction;
            this.direction = newDirection;
            updateVelocity(tileSize);
            int prevX = this.x;
            int prevY = this.y;
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x = prevX;
                    this.y = prevY;
                    this.direction = prevDirection;
                    updateVelocity(tileSize);
                    return;
                }
            }
        }

        private void updateVelocity(int tileSize) {
            int speed = tileSize / 4;
            switch (this.direction) {
                case 'U':
                    velocityX = 0;
                    velocityY = -speed;
                    break;
                case 'D':
                    velocityX = 0;
                    velocityY = speed;
                    break;
                case 'L':
                    velocityX = -speed;
                    velocityY = 0;
                    break;
                case 'R':
                    velocityX = speed;
                    velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = startX;
            this.y = startY;
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }

    private static final int ROW_COUNT = 21;
    private static final int COLUMN_COUNT = 19;
    private static final int TILE_SIZE = 32;
    private static final int BOARD_WIDTH = COLUMN_COUNT * TILE_SIZE;
    private static final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;
    private static final int GAME_SPEED_MS = 50;

    private static final String[] TILE_MAP = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private HashSet<Block> walls = new HashSet<>();
    private HashSet<Block> foods = new HashSet<>();
    private HashSet<Block> ghosts = new HashSet<>();
    private Block pacman;
    private Timer gameTimer;
    private final char[] DIRECTIONS = {'U', 'D', 'L', 'R'};
    private final Random random = new Random();
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;

    private Image wallImage;
    private Image blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    public Pacman() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        loadMap();
        initializeGhostDirections();
        startGameLoop();
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("/assets/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/assets/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/assets/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/assets/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/assets/redGhost.png")).getImage();
        pacmanUpImage = new ImageIcon(getClass().getResource("/assets/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/assets/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/assets/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/assets/pacmanRight.png")).getImage();
    }

    private void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                char tile = TILE_MAP[row].charAt(col);
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                switch (tile) {
                    case 'X':
                        walls.add(new Block(wallImage, x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'b':
                        ghosts.add(new Block(blueGhostImage, x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'o':
                        ghosts.add(new Block(orangeGhostImage, x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'p':
                        ghosts.add(new Block(pinkGhostImage, x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'r':
                        ghosts.add(new Block(redGhostImage, x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'P':
                        pacman = new Block(pacmanRightImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
                    case ' ':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                }
            }
        }
    }

    private void initializeGhostDirections() {
        for (Block ghost : ghosts) {
            char dir = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            ghost.updateDirection(dir, walls, TILE_SIZE);
        }
    }

    private void startGameLoop() {
        gameTimer = new Timer(GAME_SPEED_MS, this);
        gameTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        if (pacman != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        if (gameOver) {
            g.drawString("Game Over: " + score, TILE_SIZE / 2, TILE_SIZE / 2);
        } else {
            g.drawString("Lives: x" + lives + "  Score: " + score, TILE_SIZE / 2, TILE_SIZE / 2);
        }
    }

    private void move() {
        if (gameOver) return;

        if (pacman != null) {
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;
            checkWallCollision(pacman);
        }

        for (Block ghost : ghosts) {
            if (pacman != null && collision(pacman, ghost)) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
                return;
            }

            if (ghost.y == TILE_SIZE * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U', walls, TILE_SIZE);
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = checkWallCollision(ghost) || ghost.x < 0 || ghost.x + ghost.width > BOARD_WIDTH;
            if (collided) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                char newDir = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                ghost.updateDirection(newDir, walls, TILE_SIZE);
            }
        }

        Block eatenFood = null;
        for (Block food : foods) {
            if (pacman != null && collision(pacman, food)) {
                eatenFood = food;
                score += 10;
                break;
            }
        }
        if (eatenFood != null) {
            foods.remove(eatenFood);
        }

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean checkWallCollision(Block block) {
        for (Block wall : walls) {
            if (collision(block, wall)) {
                block.x -= block.velocityX;
                block.y -= block.velocityY;
                return true;
            }
        }
        return false;
    }

    private static boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    private void resetPositions() {
        if (pacman != null) {
            pacman.reset();
        }
        for (Block ghost : ghosts) {
            ghost.reset();
            char dir = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            ghost.updateDirection(dir, walls, TILE_SIZE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameTimer.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            initializeGhostDirections();
            gameTimer.start();
            return;
        }

        char newDir = switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> 'U';
            case KeyEvent.VK_DOWN -> 'D';
            case KeyEvent.VK_LEFT -> 'L';
            case KeyEvent.VK_RIGHT -> 'R';
            default -> pacman.direction;
        };

        if (newDir != pacman.direction) {
            pacman.updateDirection(newDir, walls, TILE_SIZE);
            updatePacmanSprite();
        }
    }

    private void updatePacmanSprite() {
        pacman.image = switch (pacman.direction) {
            case 'U' -> pacmanUpImage;
            case 'D' -> pacmanDownImage;
            case 'L' -> pacmanLeftImage;
            case 'R' -> pacmanRightImage;
            default -> pacmanRightImage;
        };
    }
}