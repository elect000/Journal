import java.util.*;
import java.awt.*;
import java.awt.event.*;
@SuppressWarnings("unchecked")
class Paint2 extends Frame implements MouseListener,MouseMotionListener{
    int x,y;
    Vector objList;
    Figure obj;
    public static void main(String args[]){
        Paint2 f = new Paint2();
        f.setSize(1000,1000);
        f.setTitle("Paint Sample");
        f.addWindowListener(new WindowAdapter(){
                @Override public void windowClosing(WindowEvent e){
                    System.exit(0);
                }});
        f.setVisible(true);
    }
    Paint2(){
        objList = new Vector();
        // mouse syori no toroku
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    @Override public void paint(Graphics g){
        Figure f;
        for(int i = 0;i < objList.size();i++){
            int R=(int)(Math.random()*256);
            int G=(int)(Math.random()*256);
            int B=(int)(Math.random()*256);
            g.setColor(new Color(R,G,B));
            f = (Figure)objList.elementAt(i);
            f.paint(g);
        }
        if(obj != null) obj.paint(g);
    }
    @Override public void mousePressed(MouseEvent e){
        // 案3 (ボタンを押された範囲で四角を作るかどうか) 
        // x = e.getX() % 1000;
        // y = e.getY() % 1000;
        x = e.getX();
        y = e.getY();
        obj = new Circle();
        obj.moveto(x,y);
        repaint();
    }
    @Override public void mouseReleased(MouseEvent e){
        x = e.getX();
        y = e.getY();
        obj.moveto(x,y);
        objList.add(obj);
        obj = null;
        repaint();
        int count = 0;
        for(int a=0;a < objList.size();a++){
            count ++;
        }
        System.out.println("maru:"+count);



    }
    @Override public void mouseClicked(MouseEvent e){}
    @Override public void mouseEntered(MouseEvent e){}
    @Override public void mouseExited(MouseEvent e){}
    @Override public void mouseDragged(MouseEvent e){
        x = e.getX();
        y = e.getY();
        if(obj != null) obj.moveto(x,y);
        repaint();
    }
    @Override public void mouseMoved(MouseEvent e){}
}
class Coord{
    int x,y;
    Coord(){
        x=y=0;
    }
    public void move(int dx,int dy){
        // 案１　(範囲外に出た時の処理)
        // this.x = (this.x + dx) % 1000
        // this.y = (this.y + dy) % 1000
        x += dx;
        y += dy;
    }
    public void moveto(int x,int y){
        // 案２　（範囲外に出た時の処理）
        // this.x = x % 1000
        // this.y = y % 1000
        this.x=x;
        this.y=y;
    }
}
class Figure extends Coord{
    int color;
    Figure(){
        color = 0;
    }
    public void paint(Graphics g){}
}
class Circle extends Figure{
    int size;
    Circle(){
        size = 50;
    }
    @Override public void paint(Graphics g){
        g.drawRect(x - size/2,y - size/2, size, size);
    }
}
