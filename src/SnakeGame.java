import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food,food2;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);
        food2 = new Tile(-1, -1);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;
        
		//game timer
		gameLoop = new Timer(100, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start();
	}	
    
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
        //Grid Lines
        for(int i = 0; i < boardWidth/tileSize; i++) {
            //(x1, y1, x2, y2)
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
            g.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
        }

        //Food
        g.setColor(Color.red);
        g.fill3DRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize, true);

        //Food 2
        g.setColor(Color.red);
        g.fill3DRect(food2.x*tileSize, food2.y*tileSize, tileSize, tileSize, true);

        //Snake Head
        g.setColor(Color.orange);
        g.fill3DRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize, true);
        
        g.setColor(Color.green);
        //Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            g.fill3DRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize, true);
		}

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + String.valueOf(snakeBody.size())  + " - Press space to play again.", tileSize - 16, tileSize);
        }
        else {
            g.setColor(Color.green);
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);

            // Draw message to unpause the game if the timer is stopped
            if (gameLoop.isRunning() == false) {
                g.drawString("Press 'P' to unpause", 230, 300);
            }
        }
	}
    
    public void placeFood(){
        int randomX = -1;
        int randomY = -1;
        int randomFood2 = -1;
        boolean canPlace = false;

        while (!canPlace) {
            randomX = random.nextInt(boardWidth/tileSize);
            randomY = random.nextInt(boardHeight/tileSize);

            canPlace = spaceUnoccupied(randomX,randomY);
        }  
        food.x = randomX;
		food.y = randomY;

        // Check if we should try to place a second food
        if (food2.x == -1 && food2.y == -1) {
            randomFood2 = random.nextInt(10);
            if (randomFood2 == 9) {
                canPlace = false;
                while (!canPlace) {
                    randomX = random.nextInt(boardWidth/tileSize);
                    randomY = random.nextInt(boardHeight/tileSize);
        
                    canPlace = spaceUnoccupied(randomX,randomY);
                }  
                food2.x = randomX;
                food2.y = randomY;
            }
        }
	}

    public Boolean spaceUnoccupied(int x, int y) {        
        // check new location doesn't collide with the snake's head
        if (snakeHead.x != x || snakeHead.y != y) {
            // New location not at the head. Check all body locations
            for (int i = 0; i < snakeBody.size(); i++) {
                if (snakeBody.get(i).x == x && snakeBody.get(i).y == y) {
                    // we have a collision with the body. 
                    return false;
                }
            }
            // check location of food 1
            if (food.x == x && food.y == y) {
                return false;
            }

            // check location of food 2
            if (food2.x == x && food2.y == y) {
                return false;
            }
        }
        else {
            return false;
        }
        return true;
    }

    public void move() {
        //eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }
        if (collision(snakeHead, food2)) {
            snakeBody.add(new Tile(food.x, food.y));
            food2.x = -1;
            food2.y = -1;
        }


        //move snake body
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { //right before the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            }
            else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }
        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //game over conditions
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            //collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        if (snakeHead.x*tileSize < 0 || snakeHead.x*tileSize > boardWidth || //passed left border or right border
            snakeHead.y*tileSize < 0 || snakeHead.y*tileSize > boardHeight ) { //passed top border or bottom border
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_P && !gameOver ) {
            // Pause the game
            if (gameLoop.isRunning()) {
                gameLoop.stop();
            }
            else {
                gameLoop.start();
            }
            repaint();
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE && gameOver) {
            // Reset the game with the spacebar upon game over
            gameOver = false;
            snakeBody.clear();
            snakeHead.x = 5;
            snakeHead.y = 6;
            velocityX = 1;
            velocityY = 0;
            food.x = 10;
            food.y = 10;
            gameLoop.restart();
            move();
            repaint();
        }
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
