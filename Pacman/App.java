package Pacman;


import javax.swing.JFrame;

public class App {
    public static void main(String[] args) {
        final int ROW_COUNT = 21;
        final int COLUMN_COUNT = 19;
        final int TILE_SIZE = 32;
        final int BOARD_WIDTH = COLUMN_COUNT * TILE_SIZE;
        final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;

        JFrame frame = new JFrame("Pac-Man");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Pacman gamePanel = new Pacman();
        frame.add(gamePanel);
        frame.pack();
        gamePanel.requestFocusInWindow();
        frame.setVisible(true);
    }
}