import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
@SuppressWarnings("unchecked")

class Paint4 extends Frame implements MouseListener,MouseMotionListener,ActionListener{
	int x,y;
	Vector<Figure> objList;
	CheckboxGroup cbg;
	CheckboxGroup Z;
	Checkbox c1,c2,c3,c4,c5,c6,c7,i1,i2,n1;
	Button end;
	Button save;
	Button read;
	int a = 1;
	int mode = 0;
	Figure obj;
	public static void main(String args[]){
		Paint4 f = new Paint4();
		f.setSize(640,800);
		f.setTitle("Paint Sample");
		f.addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){
				System.exit(0);
			}});
		f.setVisible(true);
		
		if(args.length==1)f.load(args[0]);
	}
	Paint4(){
		objList = new Vector<Figure>();
		cbg =new CheckboxGroup();
		Z =new CheckboxGroup();
		c1=new Checkbox("丸",cbg,true);
		c2=new Checkbox("円",cbg,false);
		c3=new Checkbox("四角",cbg,false);
		c4=new Checkbox("線",cbg,false);
		c5=new Checkbox("丸(塗りつぶし)",cbg,false);
		c6=new Checkbox("円(塗りつぶし)",cbg,false);
		c7=new Checkbox("四角(塗りつぶし)",cbg,false);
		i1=new Checkbox("青",Z,true);
		i2=new Checkbox("赤",Z,false);
		end=new Button("終了");
		save=new Button("保存");
		read=new Button ("読込");
		c1.setBounds(560,30,60,30);
		c2.setBounds(560,60,60,30);
		c3.setBounds(560,90,60,30);
		c4.setBounds(560,120,60,30);
		c5.setBounds(560,150,60,30);
		c6.setBounds(560,180,60,30);
		c7.setBounds(560,210,60,30);
		i1.setBounds(560,240,60,30);
		i2.setBounds(560,270,60,30);
		end.setBounds(560,420,60,30);
		save.setBounds(560,450,60,30);
		read.setBounds(560,480,60,30);
		setLayout(null);
		add(c1);
		add(c2);
		add(c3);
		add(c4);
		add(c5);
		add(c6);
		add(c7);
		add(i1);
		add(i2);
		add(end);
		add(save);
		add(read);
		addMouseListener(this);
		addMouseMotionListener(this);
		end.addActionListener(this);
		save.addActionListener(this);
		read.addActionListener(this);
	}
	
	public void save(String fname){
		try {
			FileOutputStream fos =new FileOutputStream(fname);
			ObjectOutputStream oos =new ObjectOutputStream(fos);
			oos.writeObject(objList);
			oos.close();
			fos.close();
		}catch (IOException e){
		}
	}
	public void load(String fname){
		try {
			FileInputStream fis=new FileInputStream(fname);
			ObjectInputStream ois =new ObjectInputStream(fis);
			objList=(Vector<Figure>)ois.readObject();
			ois.close();
			fis.close();
		}catch(IOException e){
		}catch(ClassNotFoundException e){
		}
		repaint();
	}
	@Override public void paint(Graphics g){
		Figure f;
		for(int i = 0;i < objList.size();i++){
			f = (Figure)objList.elementAt(i);
			f.paint(g);
		}
		if(mode>= 1) obj.paint(g);
	}
	@Override public void actionPerformed(ActionEvent e){
		save("paint.dat");
		System.exit(0);
	}
	@Override public void mousePressed(MouseEvent e){
		Checkbox c;
		Checkbox cl;
		x = e.getX();
		y = e.getY();
		c=cbg.getSelectedCheckbox();
		cl=Z.getSelectedCheckbox();
		obj=null;
		
		if(cl==i1){
			a = 1;
		}else if(cl==i2){
			a = 2;
		}
		if(c==c1){
			mode=1;
			obj =new Ring(a);
		}else if(c==c2){
			mode=2;
			obj =new Circle();
		}else if(c==c3){
			mode=2;
			obj=new Box();
		}else if(c==c4){
			mode=2;
			obj=new Line();
		}else if(c==c5){
			mode=2;
			obj=new Ringa();
		}else if(c==c6){
			mode=2;
			obj=new Circlea();
		}else if(c==c7){
			mode=2;
			obj=new Boxa();
		}
		if(obj!=null){
			obj.moveto(x,y);
			repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e){
		x = e.getX();
		y = e.getY();
		if(mode==1)       obj.moveto(x,y);
		else if(mode==2)  obj.setWH(x-obj.x,y-obj.y);
		if(mode>=1){
			objList.add(0,obj);
			obj=null;
		}
		mode=0;
		repaint();
	}
	@Override public void mouseClicked(MouseEvent e){}
	@Override public void mouseEntered(MouseEvent e){}
	@Override public void mouseExited(MouseEvent e){}
	@Override public void mouseDragged(MouseEvent e){
		x = e.getX();
		y = e.getY();
		if(mode==1){
			obj.moveto(x,y);
		}else if(mode==2){
			obj.setWH(x-obj.x,y-obj.y);
		}
		repaint();
	}
	@Override public void mouseMoved(MouseEvent e){}
}
class Coord {
	int x,y;
	Coord(){
		x=y=0;
	}
	public void move(int dx,int dy){
		x += dx;
		y += dy;
	}
	public void moveto(int x,int y){
		this.x=x;
		this.y=y;
	}
}
class Figure extends Coord{
	int color;
	int w,h;
	Figure(){
		color = 0;
		w=h=0;
	}
	public void paint(Graphics g){}
	public void setWH(int w,int h){
		this.w=w;
		this.h=h;
	}
}
class Ring extends Figure implements Serializable{
	int size;
	Ring(int color){
		size = 40;
		this.color=color;
	}
	@Override public void paint(Graphics g){
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		g.drawOval(x - size/2,y - size/2, size, size);
	}
}
class Ringa extends Figure implements Serializable{
	int size;
	Ringa(){
		size = 10;
		this.color=color;
	}
	@Override public void paint(Graphics g){
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else if(color==2){
			g.setColor(new Color(255,0,0));
		}
		g.fillOval(x - size/2,y - size/2, size, size);
	}
}
class Circle extends Figure implements Serializable{
	Circle(){
		this.color=color;
	}
	@Override public void paint(Graphics g){
		int r = (int)Math.sqrt((double)(w*w+h*h));
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		g.drawOval(x-r,y-r,r*2,r*2);
	}
}
class Circlea extends Figure implements Serializable{
	Circlea(){
		this.color=color;
	}
	@Override public void paint(Graphics g){
		int r = (int)Math.sqrt((double)(w*w+h*h));
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		g.fillOval(x-r,y-r,r*2,r*2);
	}
}
class Box extends Figure implements Serializable{
	Box(){
	this.color=color;
  }
	@Override public void paint(Graphics g){
		if(color == 1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		if(w>0&&h>0){
			g.drawRect(x,y,w,h);
		}
		else if(w<0&&h>0){
			g.drawRect(x+w,y,-w,h);
		}
		else if(w>0&&h<0){
			g.drawRect(x,y+h,w,-h);
		}
		else if(w<0&&h<0){
			g.drawRect(x+w,y+h,-w,-h);
		}	
	}
}
class Boxa extends Figure implements Serializable{
	Boxa(){
	this.color=color;
	}
	@Override public void paint(Graphics g){
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		if(w>0&&h>0){
			g.fillRect(x,y,w,h);
		}
		else if(w<0&&h>0){
			g.fillRect(x+w,y,-w,h);
		}
		else if(w>0&&h<0){
			g.fillRect(x,y+h,w,-h);
		}
		else if(w<0&&h<0){
			g.fillRect(x+w,y+h,-w,-h);
		}
	}
}
class Line extends Figure implements Serializable{
	Line(){
	this.color=color;
	}
	@Override public void paint(Graphics g){
		if(color==1){
			g.setColor(new Color(0,0,255));
		}else{
			g.setColor(new Color(255,0,0));
		}
		g.drawLine(x,y,x+w,y+h);
	}
}
