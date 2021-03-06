/**
 * Java. Game Space Invaders
 *
 * @author Sergey Iryupin
 * @version 0.3.5 dated September 14, 2016
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class GameSpaceInvaders extends JFrame {

    final String TITLE_OF_PROGRAM = "Space Invaders";
    final int POINT_SCALE = 2;
    final int FIELD_WIDTH = 224*POINT_SCALE;
    final int FIELD_HEIGHT = 256*POINT_SCALE;
    final int START_LOCATION = 150;
    final int FIELD_DX = 7; // determined experimentally
    final int FIELD_DY = 26;
    final int STEP_X = 5; // wave step left-right
    final int STEP_Y = 15; // wave step down
    final int GROUND_Y = FIELD_HEIGHT - 20;
    final int LEFT = 37; // key codes
    final int RIGHT = 39;
    final int DOWN = 40;
    final int FIRE = 32;
    final int SHOW_DELAY = 20; // delay for animation
    final int[][][][] PATTERN_OF_ALIENS = {
      {{{0,0,0,0,1,1,1,1,0,0,0,0}, {0,1,1,1,1,1,1,1,1,1,1,0}, {1,1,1,1,1,1,1,1,1,1,1,1}, // alien 1/1
        {1,1,1,0,0,1,1,0,0,1,1,1}, {1,1,1,1,1,1,1,1,1,1,1,1}, {0,0,1,1,1,0,0,1,1,1,0,0},
        {0,1,1,0,0,1,1,0,0,1,1,0}, {0,0,1,1,0,0,0,0,1,1,0,0}, {12}},
       {{0,0,0,0,1,1,1,1,0,0,0,0}, {0,1,1,1,1,1,1,1,1,1,1,0}, {1,1,1,1,1,1,1,1,1,1,1,1}, // alien 1/2
        {1,1,1,0,0,1,1,0,0,1,1,1}, {1,1,1,1,1,1,1,1,1,1,1,1}, {0,0,0,1,1,0,0,1,1,0,0,0},
        {0,0,1,1,0,1,1,0,1,1,0,0}, {1,1,0,0,0,0,0,0,0,0,1,1}}},
      {{{0,0,1,0,0,0,0,0,1,0,0,0}, {0,0,0,1,0,0,0,1,0,0,0,0}, {0,0,1,1,1,1,1,1,1,0,0,0}, // alien 2/1
        {0,1,1,0,1,1,1,0,1,1,0,0}, {1,1,1,1,1,1,1,1,1,1,1,0}, {1,0,1,1,1,1,1,1,1,0,1,0},
        {1,0,1,0,0,0,0,0,1,0,1,0}, {0,0,0,1,1,0,1,1,0,0,0,0}, {11}},
       {{0,0,1,0,0,0,0,0,1,0,0,0}, {1,0,0,1,0,0,0,1,0,0,1,0}, {1,0,1,1,1,1,1,1,1,0,1,0}, // alien 2/2
        {1,1,1,0,1,1,1,0,1,1,1,0}, {1,1,1,1,1,1,1,1,1,1,1,0}, {0,1,1,1,1,1,1,1,1,1,0,0},
        {0,0,1,0,0,0,0,0,1,0,0,0}, {0,1,0,0,0,0,0,0,0,1,0,0}}},
      {{{0,0,0,1,1,0,0,0,0,0,0,0}, {0,0,1,1,1,1,0,0,0,0,0,0}, {0,1,1,1,1,1,1,0,0,0,0,0}, // alien 3/1
        {1,1,0,1,1,0,1,1,0,0,0,0}, {1,1,1,1,1,1,1,1,0,0,0,0}, {0,0,1,0,0,1,0,0,0,0,0,0},
        {0,1,0,1,1,0,1,0,0,0,0,0}, {1,0,1,0,0,1,0,1,0,0,0,0}, {8}},
       {{0,0,0,1,1,0,0,0,0,0,0,0}, {0,0,1,1,1,1,0,0,0,0,0,0}, {0,1,1,1,1,1,1,0,0,0,0,0}, // alien 3/2
        {1,1,0,1,1,0,1,1,0,0,0,0}, {1,1,1,1,1,1,1,1,0,0,0,0}, {0,1,0,1,1,0,1,0,0,0,0,0},
        {1,0,0,0,0,0,0,1,0,0,0,0}, {0,1,0,0,0,0,1,0,0,0,0,0}}}
    };
    final int MAX_ALIEN_RAYS = 2;
    Canvas canvasPanel = new Canvas();
    Cannon cannon = new Cannon();
    Ray ray = new Ray();
    Wave wave = new Wave();
    FlashAlien flash = new FlashAlien();
    AlienRays rays = new AlienRays();
    Random random = new Random();
    int countScore, countLives = 3;
    boolean gameOver;

    public static void main(String[] args) {
        new GameSpaceInvaders().go();
    }

    GameSpaceInvaders() {
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_LOCATION, START_LOCATION, FIELD_WIDTH + FIELD_DX, FIELD_HEIGHT + FIELD_DY);
        setResizable(false);
        canvasPanel.setBackground(Color.black);
        getContentPane().add(BorderLayout.CENTER, canvasPanel);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == LEFT) || (e.getKeyCode() == RIGHT))
                    cannon.setDirection(e.getKeyCode());
                if (e.getKeyCode() == FIRE)
                    ray.start(cannon.getX() + 12, cannon.getY() - 8);
            }
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == LEFT) || (e.getKeyCode() == RIGHT))
                    cannon.setDirection(0);
            }
        });
        setVisible(true);
    }

    void go() { // main loop of game
        while (!gameOver) {
            try {
                Thread.sleep(SHOW_DELAY);
            } catch (Exception e) { e.printStackTrace(); }
            canvasPanel.repaint();
            cannon.move();
            flash.disable();
            ray.fly();
            rays.fly();
            rays.checkGround();
            rays.checkHit();
            wave.nextStep();
            wave.checkHit();
            if (wave.getSize() == 0) { // if I destroy the whole wave
                wave = new Wave();
                countLives++;
            }
        }
    }

    class Ray { // from laser cannon
        final int WIDTH = 2;
        final int HEIGHT = 8;
        final int DY = 12;
        int x, y;
        boolean exists;

        void start(int x, int y) {
            if (!exists) {
                exists = true;
                this.x = x;
                this.y = y;
            }
        }

        void fly() {
            if (exists) {
                y -= DY;
                exists = (y + DY) > 0;
            }
        }

        void disable() { exists = false; }

        boolean isEnable() { return exists; }

        int getX() { return x; }
        int getY() { return y; }

        void paint(Graphics g) {
            if (exists) g.fillRect(x, y, WIDTH, HEIGHT);
        }
    }

    class Cannon { // laser cannon
        final int WIDTH = 26;
        final int HEIGHT = 16;
        final int DX = 5;
        int x, y, direction;

        public Cannon() {
            x = 10;
            y = FIELD_HEIGHT - HEIGHT - 30;
        }

        void move() {
            if (direction == LEFT && x > 10) x -= DX;
            if (direction == RIGHT && x < FIELD_WIDTH - WIDTH - 12) x += DX;
        }

        void setDirection(int direction) { this.direction = direction; }

        int getX() { return x; }
        int getY() { return y; }

        void paint(Graphics g) {
            g.fillRect(x, y + HEIGHT/2, WIDTH, HEIGHT/2);
            g.fillRect(x + 2, y + HEIGHT/2 - 2, WIDTH - 4, HEIGHT/2);
            g.fillRect(x + 10, y + 2, WIDTH - 20, HEIGHT/2);
            g.fillRect(x + 12, y, 2, 2);
        }
    }

    class AlienRay { // from one alien
        final int width = 6;
        final int height = 10;
        final int dy = 6; // define speed of ray
        int x, y;

        AlienRay(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void fly() {
            y += dy;
        }

        boolean hitGround() {
            return y + height > GROUND_Y;
        }

        boolean hitCannon() {
            if (y + height > cannon.getY())
                if (x > cannon.getX() && x < cannon.getX() + 26)
                    return true;
            return false;
        }

        void paint(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(x + 2, y, 2, height);
            g.fillRect(x, y + height - 4, width, 2);
        }
    }

    class AlienRays { // a few rays from alien
        ArrayList<AlienRay> rays = new ArrayList<AlienRay>();

        void add(int x, int y) {
            rays.add(new AlienRay(x, y));
        }

        void fly() {
            for (AlienRay ray : rays) ray.fly();
        }

        void checkGround() {
            for (AlienRay ray : rays)
                if (ray.hitGround()) {
                    rays.remove(ray);
                    break;
                }
        }

        void checkHit() {
            for (AlienRay ray : rays)
                if (ray.hitCannon()) {
                    countLives--;
                    cannon = new Cannon();
                    gameOver = countLives == 0;
                    rays.remove(ray);
                    break;
                }
        }

        int getSize() {
            return rays.size();
        }

        void paint(Graphics g) {
            for (AlienRay ray : rays) ray.paint(g);
        }
    }

    class Alien { // for attacking wave
        int x, y, type, view = 0;
        int width, height = 8;

        Alien(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
            width = PATTERN_OF_ALIENS[type][view][8][0];
        }

        int getType() {
            return type;
        }

        boolean isHitRay() {
            if (ray.isEnable())
                if ((ray.getX() >= x) && (ray.getX() <= x + width*POINT_SCALE))
                    if (ray.getY() < y + height*POINT_SCALE) {
                        ray.disable();
                        return true;
                    }
            return false;
        }

        void nextStep(int direction) {
            view = 1 - view; // change view each step
            if (direction == RIGHT) x += STEP_X;
            else if (direction == LEFT) x -= STEP_X;
            else if (direction == DOWN) y += STEP_Y;
        }

        void bang() { 
            flash.enable(x, y - 2);
        }

        void shot() {
            rays.add(x + width/2, y + height);
        }

        void paint(Graphics g) {
            g.setColor(Color.white);
            for (int col = 0; col < width; col++)
                for (int row = 0; row < height; row++)
                    if (PATTERN_OF_ALIENS[type][view][row][col] == 1)
                        g.fillRect(col*POINT_SCALE + x, row*POINT_SCALE + y, POINT_SCALE, POINT_SCALE);
        }
    }

    class Wave { // attacking wave of aliens
        final int[][] PATTERN = {
            {2,2,2,2,2,2,2,2,2,2,2}, {1,1,1,1,1,1,1,1,1,1,1}, {1,1,1,1,1,1,1,1,1,1,1}, {0,0,0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0,0,0}};
        volatile ArrayList<Alien> wave = new ArrayList<Alien>();
        final int NUM_FRAMES = 30; // sets the speed of the wave
        int countFrames = 0;
        int direction = RIGHT;
        boolean stepDown = false;
        int startX = 50;
        int startY = 60;

        Wave() {
            for (int y = 0; y < PATTERN.length; y++)
                for (int x = 0; x < PATTERN[y].length; x++)
                    wave.add(new Alien(startX + x*POINT_SCALE*16 + PATTERN[y][x]*POINT_SCALE, startY + y*POINT_SCALE*16, PATTERN[y][x]));
        }

        void nextStep() {
            if (countFrames == NUM_FRAMES) {
                if ((startX == 10) || (startX == 17*STEP_X + 10)) { // time to change direction
                    if (!stepDown) {
                        direction = DOWN;
                    } else {
                        direction = (startX == 10)? RIGHT : LEFT;
                        stepDown = false;
                    }
                }
                for (Alien alien : wave) { // wave moves and shots
                    alien.nextStep(direction);
                    if (random.nextInt(10) == 9)
                        if (rays.getSize() < MAX_ALIEN_RAYS)
                            alien.shot();
                }
                if (direction == DOWN) {
                    startY += STEP_Y;
                    stepDown = true;
                } else {
                    startX += (direction == RIGHT)? STEP_X : -STEP_X;
                }
                countFrames = 0;
            } else {
                countFrames++;
            }
        }

        void checkHit() {
            for (Alien alien : wave)
                if (alien.isHitRay()) {
                    countScore += (alien.getType() + 1) * 10;
                    alien.bang();
                    wave.remove(alien);
                    break;
                }
        }

        int getSize() { return wave.size(); }

        void paint(Graphics g) {
            for (Alien alien : wave) alien.paint(g);
        }
    }

    class FlashAlien { // flash when the alien explodes
        final int[][] BANG = {
            {0,0,0,0,0,1,0,0,0,0,0,0}, {0,1,0,0,0,1,0,0,1,0,0,0}, {0,0,1,0,0,0,0,0,1,0,0,1}, {0,0,0,1,0,0,0,1,0,0,1,0}, {1,1,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,1,1}, {0,1,0,0,1,0,0,0,1,0,0,0}, {1,0,0,1,0,0,0,0,0,1,0,0}, {0,0,0,1,0,0,1,0,0,0,1,0}, {0,0,0,0,0,0,1,0,0,0,0,0}
        };
        boolean enable;
        int x, y;

        void enable(int x, int y) {
            this.x = x;
            this.y = y;
            enable = true;
        }

        void disable() { enable = false; }

        void paint(Graphics g) {
            if (enable)
                for (int i = 0; i < BANG.length; i++)
                    for (int j = 0; j < BANG[i].length; j++)
                        if (BANG[i][j] == 1) g.fillRect(j*POINT_SCALE + x, i*POINT_SCALE + y, POINT_SCALE, POINT_SCALE);
        }
    }

    void paintTextAndLine(Graphics g) { // paint score, lives and green line
        final int[][] SCORE = {
            {1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,1,0,1,0,0,0,0,0,0,1,0,0,0,0,1,0,1,0,0,0,0,0,0,1,1,1,1,1,1},
            {0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,0,0,0,0,0,0,1,1,1,1,1,1}
        };
        final int[][] LIVES = {
            {1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
            {1,0,0,0,0,0,0,1,0,0,1,0,1,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,1,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,0,1,0,0,0,1,0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1}
        };
        final int[][] GAME_OVER = {
            {1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,0,1,0,0,0,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
            {1,0,0,0,0,1,0,1,1,1,1,1,1,0,1,0,0,1,0,0,1,0,1,1,1,1,1,1,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,1,0,0,1,1,1,1,1,1,0,1,0,0,0,0,0},
            {1,0,0,0,0,1,0,1,0,0,0,0,1,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
            {1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,0,0,1,0,0,1,0,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,0,0,0,1,0,0,0,1,1,1,1,1,1,0,1,0,0,0,0,0},
            {0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        };
        g.setColor(Color.white);
        for (int y = 0; y < SCORE.length; y++) {
            for (int x = 0; x < SCORE[y].length; x++)
                if (SCORE[y][x] == 1) g.fillRect(x*POINT_SCALE + 30, y*POINT_SCALE + 20, POINT_SCALE, POINT_SCALE);
            for (int i = 0; i < LIVES[y].length; i++)
                if (LIVES[y][i] == 1) g.fillRect(i*POINT_SCALE + 320, y*POINT_SCALE + 20, POINT_SCALE, POINT_SCALE);
        }
        if (gameOver)
            for (int y = 0; y < GAME_OVER.length; y++)
                for (int x = 0; x < GAME_OVER[y].length; x++)
                    if (GAME_OVER[y][x] == 1) g.fillRect(x*POINT_SCALE + 170, y*POINT_SCALE + 250, POINT_SCALE, POINT_SCALE);
        g.setColor(Color.green);
        g.fillRect(10, GROUND_Y, FIELD_WIDTH - 20, 2);
    }

    void paintNumber(Graphics g, int number, int x, int y) { // paint numbers (countScore, countLives)
        final int[][][] NUMBERS = {
            {{1,1,1,1,1,1}, {1,0,0,0,0,1}, {1,0,0,0,0,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}}, // 0
            {{0,0,0,0,0,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}}, // 1
            {{1,1,1,1,1,1}, {0,0,0,0,0,1}, {1,1,1,1,1,1}, {1,0,0,0,0,0}, {1,1,1,1,1,1}}, // 2
            {{1,1,1,1,1,1}, {0,0,0,0,0,1}, {1,1,1,1,1,1}, {0,0,0,0,0,1}, {1,1,1,1,1,1}}, // 3
            {{1,0,0,0,0,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}}, // 4
            {{1,1,1,1,1,1}, {1,0,0,0,0,0}, {1,1,1,1,1,1}, {0,0,0,0,0,1}, {1,1,1,1,1,1}}, // 5
            {{1,1,1,1,1,1}, {1,0,0,0,0,0}, {1,1,1,1,1,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}}, // 6
            {{1,1,1,1,1,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}, {0,0,0,0,0,1}}, // 7
            {{1,1,1,1,1,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}}, // 8
            {{1,1,1,1,1,1}, {1,0,0,0,0,1}, {1,1,1,1,1,1}, {0,0,0,0,0,1}, {1,1,1,1,1,1}}  // 9
        };
        String numStr = Integer.toString(number);
        g.setColor(Color.green);
        for (int p = 0; p < numStr.length(); p++) {
            int n = (int) numStr.charAt(p) - 48;
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 6; j++)
                    if (NUMBERS[n][i][j] == 1) g.fillRect(x + j*POINT_SCALE + p*14, y + i*POINT_SCALE, POINT_SCALE, POINT_SCALE);
        }
    }

    class Canvas extends JPanel { // my canvas for painting
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintTextAndLine(g);
            paintNumber(g, countScore, 110, 20);
            paintNumber(g, countLives, 390, 20);
            if (!gameOver) {
                cannon.paint(g);
                ray.paint(g);
                wave.paint(g);
                flash.paint(g);
                rays.paint(g);
            }
        }
    }
}