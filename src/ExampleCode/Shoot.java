package ExampleCode;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import javax.swing.*;

@SuppressWarnings("serial")
public class Shoot extends JFrame implements Runnable, KeyListener {
    private BufferedImage bi = null;
    private ArrayList<Missile> missileList = null;
    private ArrayList<Enemy> enemyList = null;
    private ArrayList<Bomb> bombList = null;
    private ArrayList<SplitPiece> splitList = null;
    private boolean left = false, right = false, up = false, down = false, fire = false, bomb = false, nextBomb = true;
    private boolean start = false, end = false, playing = false, tutorial = true;
    private int w = 300, h = 500, x = 130, y = 450, xw = 20, xh = 20;
    private int score = 0, bombCount = 3;
    private int time = 0;

    public Shoot() {
        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        // 리스트에 무한히 담을 수 있도록 ArrayList를 사용하였음.
        missileList = new ArrayList<Missile>();
        enemyList = new ArrayList<Enemy>();
        bombList = new ArrayList<Bomb>();
        splitList = new ArrayList<SplitPiece>();

        this.addKeyListener(this);
        this.setSize(w, h);
        this.setTitle("Shooting Game");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);  
    }
    
    // 쓰레드로 동작하는 부분 (미사일 발사, 키 입력, 적들의 공격, 부딪혀서 점수 증가 등이 개별적으로 이루어져야 하기 때문)
    public void run() {
        try {
            int missileCount = 0;
            int enemyCount = 0;
            int count = 0;
            while(true) {
                Thread.sleep(10);
                
                if(start) {
                	count = 0;
                	time++;
                    // 적의 등장 시간 조정하는 부분 숫자 줄이면 빨리등장
                    if(enemyCount > 2000-score) {
                        enCreate();
                        enemyCount = 0;
                    }

                    // 총알의 발사 간격 조정 숫자 줄이면 빠르게 발사
                    if(missileCount >= 50) {
                        fireMs();
                        missileCount = 0;
                    }
                    // 폭탄 발사 한번 누르면 한번 나감
                    shootB();
                    missileCount += 10;
                    enemyCount += 10;
                    keyControl();
                    crashChk();
                }
                
                // GAME OVER 후 시간을 셈
                if(end && count < 2000)
                	count += 10;
                // GAME OVER 후 1초가 되면 화면 멈춤
                if (count < 1000)
                	draw();      
                
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void shootB() {
    	if(bomb && nextBomb && bombCount >= 1) {
    		Bomb b = new Bomb(this.x, this.y);
    		bombList.add(b);
    		nextBomb = false;
    		bombCount--;
    	}
    }

    // 미사일 리스트의 전체 크기가 100 이하일 때, 새로운 미사일을 리스트에 추가함
    public void fireMs() {
        if(fire) {
            if(missileList.size() < 100) {
                Missile m = new Missile(this.x, this.y);
                missileList.add(m);
            }
        }
    }

    // 랜덤하게 적의 위치를 추가하여 리스트에 x값, y값을 추가함
    public void enCreate() {
        for(int i = 0; i < 20; i++) {
            double rx = Math.random() * (w - xw);
            double ry = Math.random() * 50;
            Enemy en = new Enemy((int)rx, (int)ry);
            enemyList.add(en);
        }
    }

    // 미사일과 적이 닿았는지 체크하는 부분
    public void crashChk() {
        //Graphics g = this.getGraphics();
        Polygon p = null;

        // 현재 화면에 뿌려진 미사일의 전체 리스트와 내려오는 적들의 전체 리스트를 가져옴.
        for(int i = 0; i < missileList.size(); i++) {
            Missile m = (Missile)missileList.get(i);

            for(int j = 0; j < enemyList.size(); j++) {
                Enemy e = (Enemy)enemyList.get(j);
                // 미사일과 적의 교차점을 계산함.
                int[] xpoints = {m.x, (m.x + m.w), (m.x + m.w), m.x};
                int[] ypoints = {m.y, m.y, (m.y + m.h), (m.y + m.h)};
                p = new Polygon(xpoints, ypoints, 4);

                // 해당 폴리곤이 교차점으로 계산되었으면 그 미사일과 적을 리스트에서 제거하고 점수에 1을 더해줌
                if(p.intersects((double)e.x, (double)e.y, (double)e.w, (double)e.h)) {
                    missileList.remove(i);
                    enemyList.remove(j);
                    score += 1;
                    // 10점마다 폭탄 1개 얻음 최대 개수 5
                    if(score % 10 == 0 && bombCount < 5)
                    	bombCount += 1;
                }
            }
        }
        
        for(int i = 0; i < bombList.size(); i++) {
            Bomb b = (Bomb)bombList.get(i);

            for(int j = 0; j < enemyList.size(); j++) {
                Enemy e = (Enemy)enemyList.get(j);
                // 미사일과 적의 교차점을 계산함.
                int[] xpoints = {b.x, (b.x + b.w), (b.x + b.w), b.x};
                int[] ypoints = {b.y, b.y, (b.y + b.h), (b.y + b.h)};
                p = new Polygon(xpoints, ypoints, 4);

                // 해당 폴리곤이 교차점으로 계산되었으면 그 미사일과 적을 리스트에서 제거하고 점수에 1을 더해줌
                if(p.intersects((double)e.x, (double)e.y, (double)e.w, (double)e.h)) {
                	b.booomb();
                    enemyList.remove(j);
                    score += 1;
                    // 10점마다 폭탄 1개 얻음 최대 개수 5
                    if(score % 10 == 0 && bombCount < 5)
                    	bombCount += 1;
                }
            }
        }

        // 적의 전체 리스트 가져옴
        for(int i = 0; i < enemyList.size(); i++) {
            Enemy e = (Enemy)enemyList.get(i);
            int[] xpoints = {x, (x + xw), (x + xw), x};
            int[] ypoints = {y, y, (y + xh), (y + xh)};
            p = new Polygon(xpoints, ypoints, 4);

            // 위와 동일하나 미사일과 적이 아닌 사용자와 적의 위치 교차점을 계산하여 닿게 된다면 게임이 종료되게 함.
            if(p.intersects((double)e.x, (double)e.y, (double)e.w, (double)e.h)) {
                enemyList.remove(i);
                start = false;
                end = true;
            }
        }
    }
    
    // 이부분이 화면에 그리는 부분.
    public void draw() {
        Graphics gs = bi.getGraphics();

        gs.setColor(Color.white);
        gs.fillRect(0, 0, w, h);

        gs.setColor(Color.black);
        gs.drawString("" + (double)time/100, 30, 50);
        gs.drawString("Enemy의 수 : " + enemyList.size(), 180, 50);
        gs.drawString("Missile의 수 : " + missileList.size(), 180, 70);
        gs.drawString("Bomb의 수: " + bombCount, 180, 90);
        gs.drawString("점수 : " + score, 180, 110);
        gs.drawString("게임 시작 : Enter", 180, 130);
        
        if(end) {
            gs.drawString("G A M E     O V E R", 100, 250);
            if(playing) {
            	SplitPiece piece1 = new SplitPiece(x, y, xw/2, xh/2);
            	SplitPiece piece2 = new SplitPiece(x, y+xh/2, xw/2, xh/2);
            	SplitPiece piece3 = new SplitPiece(x+xw/2, y, xw/2, xh/2);
            	SplitPiece piece4 = new SplitPiece(x+xw/2, y+xh/2, xw/2, xh/2);
            	splitList.add(piece1);
            	splitList.add(piece2);
            	splitList.add(piece3);
            	splitList.add(piece4);
            }
            playing = false;
            
            for(int i = 0; i < splitList.size(); i++) {
            	SplitPiece p = (SplitPiece)splitList.get(i);
            	gs.setColor(Color.red);
            	gs.fillRect(p.x, p.y, p.w, p.h);
            	
            	if(i==0) p.split(true, true);
            	if(i==1) p.split(true, false);
            	if(i==2) p.split(false, true);
            	if(i==3) p.split(false, false);
            }
        }
        else {
        	gs.setColor(Color.black);
        	gs.fillRect(x, y, xw, xh);
        }
        	
        
        // 전체 미사일 리스트 가져와서 미사일을 파란색으로 만들고 화면 위에 뿌림
        for(int i = 0; i < missileList.size(); i++) {
            Missile m = (Missile)missileList.get(i);
            gs.setColor(Color.blue);
            gs.drawOval(m.x, m.y, m.w, m.h);

            // 화면 끝에 도착했으면 미사일을 리스트에서 지우고 삭제함.
            if(m.y < 0) {
                missileList.remove(i);
            }
            // 계속 y축 위로 움직임
            m.moveMs();
        }
        
        for(int i=0; i < bombList.size(); i++) {
        	Bomb b = (Bomb)bombList.get(i);
        	gs.setColor(Color.red);
        	gs.drawOval(b.x, b.y, b.w, b.h);
        	
        	// 폭탄이 한번 터졌으면 삭제
        	if(b.fire > 7) {
                bombList.remove(i);
            }
        	
        	// 계속 y축 위로 움직임
        	if(b.countDown > 0) {
        		b.moveB();
        	}
        	else {
        		b.booomb();
        	}
            
        }

        // 검은색으로 설정하여 적들이 리스트에서 뽑혀져 화면에 표시됨.
        gs.setColor(Color.black);
        
        for(int i = 0; i < enemyList.size(); i++) {
            Enemy e = (Enemy)enemyList.get(i);
            gs.fillRect(e.x, e.y, e.w, e.h);

            // 바닥까지 도달했으면 사라짐.
            if(e.y > h) {
                enemyList.remove(i);
            }
            // 계속 y축 아래로 움직임.
            e.moveEn();
        }
        
        if(tutorial) {
        	gs.drawRect(50, 180, 190, 180);
        	gs.setFont(new Font("SansSerif",Font.BOLD, 30));
        	gs.drawString("RULE", 100, 220);
        	gs.setFont(new Font("SansSerif",Font.PLAIN, 13));
        	gs.drawString("공격: A", 70, 250);
        	gs.drawString("폭탄: SPACE", 70, 270);
        	gs.drawString("적 10명 처치시 폭탄 획득", 70, 290);
        	gs.drawString("최대 개수 5개", 140, 310);
        	gs.setFont(new Font("SansSerif",Font.BOLD, 13));
        	gs.drawString("PRESS ENTER", 100, 350);
        	
        }
        
        Graphics ge = this.getGraphics();
        ge.drawImage(bi, 0, 0, w, h, this);
    }
    
    public void reset() {
    	time = 0;
    	score = 0;
    	bombCount = 3;
        start = true;
        end = false;
        playing = true;
        tutorial = false;
        w = 300;
        h = 500;
        x = 130;
        y = 450;
        xw = 20;
        xh = 20;
        enemyList.clear();
        missileList.clear();
        bombList.clear();
        splitList.clear();
    }
    
    // 키 입력시 한번에 움직이는 거리 조정하는 부분
    public void keyControl() {
        if(0 < x) {
            if(left) x -= 3;
        }

        if(w > x + xw) {
            if(right) x += 3;
        }

        if(25 < y) {
            if(up) y -= 3;
        }

        if(h > y + xh) {
            if(down) y += 3;
        }
    }
    
    // 키 누를때
    public void keyPressed(KeyEvent ke) {
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            left = true;
            break;
            case KeyEvent.VK_RIGHT:
            right = true;
            break;
            case KeyEvent.VK_UP:
            up = true;
            break;
            case KeyEvent.VK_DOWN:
            down = true;
            break;
            case KeyEvent.VK_A:
            fire = true;
            break;
            case KeyEvent.VK_SPACE:
            bomb = true;
            break;
            case KeyEvent.VK_ENTER:
            if(!playing)
           		reset();
            break;
        }
    }

    // 키 뗄때
    public void keyReleased(KeyEvent ke) {
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            left = false;
            break;
            case KeyEvent.VK_RIGHT:
            right = false;
            break;
            case KeyEvent.VK_UP:
            up = false;
            break;
            case KeyEvent.VK_DOWN:
            down = false;
            break;
            case KeyEvent.VK_A:
            fire = false;
            break;
            case KeyEvent.VK_SPACE:
            bomb = false;
            nextBomb = true;
            break;
        }
    }
    
    public void keyTyped(KeyEvent ke) { }
    
    public static void main(String[] args) {
        Thread t = new Thread(new Shoot());
        t.start();
    }
}

// 미사일 클래스 미사일 기본 크기와 생성자, 계속 아래로 움직이게 하는 메소드 moveMs()
class Missile {
    int x;
    int y;
    int w = 5;
    int h = 5;

    public Missile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveMs() {
        y--;
    }
}

// 적 클래스 적 기본 크기와 생성자, 계속 위로 움직이게 하는 메소드 moveEn()
class Enemy {
    int x;
    int y;
    int w = 15;
    int h = 15;
    
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void moveEn() {
        y++;
    } 
}

class Bomb {
	int x;
	int y;
	int w = 5;
	int h = 5;
	int countDown = 2000;
	int fire = 0;
	boolean crush = false;
	
	public Bomb(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void moveB() {
		y--;
		countDown -= 10;
	}
	
	public void booomb() {
		countDown = 0;
		if(fire == 0) {
			x = x - 75;
			y = y - 75;
			w = 150;
			h = 150;
		}
		fire += 1;
	}
}

class SplitPiece {
	int x;
	int y;
	int w;
	int h;
	
	public SplitPiece(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public void split(boolean left, boolean up) {
		if(left) x -= 3;
		else x += 3;
		if(up) y -= 3;
		else y += 3;
	}
}