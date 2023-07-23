import java.awt.*;
import java.awt.event.*;
import java.applet.*;
/*<applet code="BreakOutGame" height="930" width="1850"></applet>*/
public class BreakOut extends Applet implements Runnable,KeyListener,FocusListener {
  static final int paddlewidth=50;
  static final int paddleheight=4;
  static final int paddlestep=5;

  static final int balldiameter=10;
  static final int ballstep=2;

  // second screen for the double buffering technique:
  Image screenImage;//A technique called double buffering permits one set of data to be used while another is collected. It is used with graphics displays, where one frame buffer holds the current screen image while another acquires the bits that will make up the next image.
  

  Graphics screenGraphics;  

  int wt,ht;

  BrickWall brickwl;
  Paddle pdl;
  Ball ball;

  boolean running,suspended,gameover,waitingforspace;

  volatile boolean rgtpressed,lftpressed,space_pressed;

  void waitForSpace() {
    waitingforspace=true;
    repaint();
    // when we sleep we consume less resources
    space_pressed=false;
    while (!space_pressed) {
      try {
        Thread.currentThread().sleep(100);
      }
      catch (InterruptedException e) {};
    }
    waitingforspace=false;
  }

  // If the user clicks our applet, we start or resume the game
  public void focusGained(FocusEvent e) {
    if (!running) {
      suspended=false;
      running=true;
      (new Thread(BreakOut.this)).start();
    } else {
      suspended=false;
    }
    repaint();
  }

  // If the user clicks somewhere else, we suspend the game
  public void focusLost(FocusEvent e) {
    if (running) {
      suspended=true;
      repaint();
    }
  }

 
  
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  lftpressed=true; break;
      case KeyEvent.VK_RIGHT: rgtpressed=true; break;
      case KeyEvent.VK_SPACE: space_pressed=true; break;
    }
  }

  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  lftpressed=false; break;
      case KeyEvent.VK_RIGHT: rgtpressed=false; break;
    }
  }

  public void keyTyped(KeyEvent e) {  }

  
  public void init() {   
    setBackground(Color.white);

    running=false;
    wt=getSize().width;
    ht=getSize().height;

    screenImage = createImage(wt,ht);
    screenGraphics = screenImage.getGraphics();

    addKeyListener(this);    
    addFocusListener(this);

    waitingforspace=false;

    repaint();
  }

	
	 public void paint(Graphics g) {   
    if (running)
     {
      g.drawImage(screenImage, 0, 0, this);    
      if (suspended) 
      {
        g.setColor(Color.black);
        g.drawString("Click here.",(wt-70)/2,ht/2);
      }
       else if (waitingforspace)
       {
        g.setColor(Color.white);
        g.drawString("Press SPACE.",(wt-70)/2,ht/2);
      }

    } else 
    {
        g.setColor(Color.black); 
        g.drawString("Click here to start.",(wt-90)/2,ht/2);
    }
  }

  public void update(Graphics g) 
  {
    paint(g);
  }
   
  public void run() 
  {
    while (true) 
    {
      screenGraphics.setColor(Color.white);
      screenGraphics.fillRect(0,0,wt,ht);

      gameover=false;

      brickwl=new BrickWall(10,4,wt/10,ht/(3*4),screenGraphics);
      brickwl.paint();
      pdl=new Paddle(paddlewidth,paddleheight,(wt-paddlewidth)/2,ht-1- paddleheight,0,wt,paddlestep,screenGraphics);
      pdl.paint();
      ball=new Ball(wt/2,ht/3,0,ballstep,balldiameter,ballstep,screenGraphics,0,wt,0,ht);
      ball.paint();
   
      repaint();

      waitForSpace();
        
      while (!gameover) {
        try {
          Thread.currentThread().sleep(10);
        }
        catch (InterruptedException e) {};

        if (!suspended) {
          pdl.clear(); 
          ball.clear();

          if ((lftpressed)&&(!rgtpressed))
           pdl.moveLeft();
          if ((rgtpressed)&&(!lftpressed)) 
            pdl.moveRight();

          gameover=ball.move(pdl,brickwl);
          if (brickwl.bricks_Left()==0) gameover=true;

          pdl.paint();
          ball.paint();
     
          repaint();
        }
      }

      screenGraphics.setColor(Color.black);

      if (brickwl.bricks_Left()==0) 
        screenGraphics.drawString("CONGRATULATIONS!!!!!!!!!!",(wt-120)/2,ht/2-20); 
      else 
        screenGraphics.drawString("GAME OVER!!!!!!!!!!!!1",(wt-76)/2,ht/2-20); 

      waitForSpace();
    }
  }
}


class BrickWall {
  private boolean brick_Visible[][];
  private int bricks_In_Row,bricks_In_Column,brick_Width,brick_Height,bricks_Left;
  Graphics g;

  public BrickWall(int bricks_In_Row_,int bricks_In_Column_,int brick_Width_,int brick_Height_,Graphics g_) {
    bricks_In_Row=bricks_In_Row_;
    bricks_In_Column=bricks_In_Column_;
    brick_Width=brick_Width_;
    brick_Height=brick_Height_;
    g=g_;

    brick_Visible=new boolean[bricks_In_Row][bricks_In_Column];
    bricks_Left=0;

    int x,y;
    for (x=0;x<bricks_In_Row;x++)
      for (y=0;y<bricks_In_Column;y++) {
        brick_Visible[x][y]=true;
        bricks_Left++;
      }
  }

  public void paint() {
    int x,y;

    for (x=0;x<bricks_In_Row;x++)
      for (y=0;y<bricks_In_Column;y++)
        if (brick_Visible[x][y]) {
          g.setColor(Color.green);
          g.fillRect(x*brick_Width,y*brick_Height,brick_Width-1,brick_Height-1);
        }
  }

  public int inBrick(int x,int y) {
    int nx1,ny1;
 
    nx1=(x/brick_Width);
    ny1=(y/brick_Height);

    if ((nx1<0)||(nx1>=bricks_In_Row)||(ny1<0)||(ny1>=bricks_In_Column)) 
      return 0;

    if (brick_Visible[nx1][ny1]) 
      return 1;
     else 
      return 0;
  }

  public void hitBrick(int x,int y) {
    int nx1,ny1;
 
    nx1=(x/brick_Width);
    ny1=(y/brick_Height);

    if ((nx1<0)||(nx1>=bricks_In_Row)||(ny1<0)||(ny1>=bricks_In_Column)) 
      return;

    if (brick_Visible[nx1][ny1]) {
      brick_Visible[nx1][ny1]=false;
      bricks_Left--;
      g.setColor(Color.white);
      g.fillRect(nx1*brick_Width,ny1*brick_Height,brick_Width-1,brick_Height-1);
    }
  }     

  public int bricks_Left() {
    return bricks_Left;
  }
}



class Paddle {
  private int wt,ht,x1,y1,maxx1,minx1,step1;
  private Graphics g;

  public Paddle(int width1_,int height1_,int x1_,int y1_,int minx1_,int maxx1_,int step1_,Graphics g_) {
    wt=width1_; ht=height1_; x1=x1_; y1=y1_; minx1=minx1_; maxx1=maxx1_; g=g_; step1=step1_;
  }
  
  public void paint() {
    g.setColor(Color.black);
    g.fillRect(x1,y1,wt,ht);
  }
  
  public void clear() {
    g.setColor(Color.white);
    g.fillRect(x1,y1,wt,ht);
  }

  public void moveLeft() {
    if (x1-step1>minx1) x1-=step1; 
    else x1=minx1;
  }
  
  public void moveRight() {
    if (x1+step1<maxx1-wt) x1+=step1; 
    else x1=maxx1-wt;
  } 

  public int leftCorner() {
    return x1;
  }

  public int rightCorner() {
    return x1+wt;
  }
  
  public int middle() {
    return x1+wt/2;
  }

  public int getY() {
    return y1;
  }
}

/*-------------------------------------------------------------------------------------------------
   class Ball - manages the ball in the breakout game
   +
   methods: 
     void paint() - paints the ball
     void clear() - clears the ball
     boolean move(Paddle paddle,BrickWall brickwall) - moves the ball, returns true 
                                                       if the ball goes off the screen

-------------------------------------------------------------------------------------------------*/

class Ball {
  private int x1,y1,dx1,dy1,diameter1,minx1,maxx1,miny1,maxy1,step1;
  private Graphics g;

  public Ball(int x1_,int y1_,int dx1_,int dy1_,int diameter1_,int step1_,Graphics g_,int minx1_,int maxx1_,int miny1_,int maxy1_) {
    x1=x1_; y1=y1_; dx1=dx1_; dy1=dy1_; diameter1=diameter1_; step1=step1_; g=g_;
    minx1=minx1_; maxx1=maxx1_; miny1=miny1_; maxy1=maxy1_;
  }
  
  public void paint() {
    g.setColor(Color.black);
    g.fillOval(x1,y1,diameter1,diameter1);
  }
  
  public void clear() {
    g.setColor(Color.white);
    g.fillOval(x1,y1,diameter1,diameter1);
  }

  public boolean move(Paddle pdl,BrickWall brickwl) {
    boolean ballgoesout=false;

    // If there is wall => bounce
    if ((x1+dx1<minx1)||(x1+dx1+diameter1>maxx1)) dx1=-dx1;
    if (y1+dy1<0) dy1=-dy1;
  
    if (y1+dy1+diameter1>=pdl.getY()) {
      if ((x1+dx1+diameter1<pdl.leftCorner())||(x1+dx1>pdl.rightCorner()))
         ballgoesout=true;
       else {
         dy1=-dy1;
         if (x1+dx1+diameter1/2<pdl.middle()) 
           dx1=-step1;
         else
           dx1=step1;
       }
    }

    switch (brickwl.inBrick(x1,y1)+2*brickwl.inBrick(x1+diameter1,y1)+4*brickwl.inBrick(x1,y1+diameter1)+8*brickwl.inBrick(x1+diameter1,y1+diameter1)) {
      case 0: break;
      case 5: case 10: dx1=-dx1; break;
      case 3: case 12: dy1=-dy1; break;
      case 1: dx1=step1; dy1=step1; break;
      case 2: dx1=-step1; dy1=step1; break;
      case 4: dx1=step1; dy1=-step1; break;
      case 8: dx1=-step1; dy1=-step1; break;
      default: dx1=-dx1; dy1=-dy1; break;
    }
           
    brickwl.hitBrick(x1,y1); 
    brickwl.hitBrick(x1+diameter1,y1); 
    brickwl.hitBrick(x1,y1+diameter1); 
    brickwl.hitBrick(x1+diameter1,y1+diameter1);
   
    x1+=dx1;
    y1+=dy1;
               
    return ballgoesout;
  }
}
	