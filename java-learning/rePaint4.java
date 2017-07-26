import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Checkbox;
import java.awt.event.WindowAdapter;
import java.awt.CheckboxGroup;

@SuppressWarnings("unchecked")
class rePaint4 extends Frame implements MouseListener, MouseMotionListener, ActionListener {

  int x, y;
  Vector<Figure> objList;
  CheckboxGroup cbg;
  CheckboxGroup state;
  CheckboxGroup backcolor;
  /* if you want other attributes, you check below codes and #mousePressed (line 132)*/
  String[] boxname = new String[] {"丸", "円", "四角", "線", "丸(塗りつぶし)", "円(塗りつぶし)", "四角(塗りつぶし)"};
  String[] colorname = new String[] {"blue", "red"};
  String[] backname = new String[] {"white", "gray", "black"};
  Checkbox[] c = new Checkbox[boxname.length];
  Checkbox[] i = new Checkbox[colorname.length];
  Checkbox[] b = new Checkbox[backname.length];
  Button end;
  Button save;
  Button read;
  int a, mode;
  Figure obj;

  public static void main(String[] args) {
    /* ground */
    rePaint4 f = new rePaint4();
    /* ground-settings */
    f.setSize(640, 800);
    f.setTitle("Paint Sample_re");
    /* window-manage */
    f.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });
    f.setVisible(true);
    if (args.length == 1) f.load(args[0]);
  }

  rePaint4() {
    /* object */
    objList = new Vector<Figure>();
    cbg = new CheckboxGroup();
    state = new CheckboxGroup();
    backcolor = new CheckboxGroup();
    for (int i = 0; i < c.length; i++) {
      c[i] = new Checkbox(boxname[i], cbg, false);
      c[i].setBounds(500, 30 * (i + 2), 120, 30);
    }
    c[0].setState(true);
    for (int j = 0; j < i.length; j++) {
      i[j] = new Checkbox(colorname[j], state, false);
      i[j].setBounds(500, 30 * (c.length + j + 3), 120, 30);
    }
    i[0].setState(true);
    for (int j = 0; j < b.length; j++) {
      b[j] = new Checkbox(backname[j], backcolor, false);
      b[j].setBounds(500, 30 * (c.length + i.length + j + 4), 120, 30);
    }
    b[0].setState(true);
    end = new Button("終了");
    save = new Button("保存");
    read = new Button("読込");
    end.setBounds(500, 30 * (c.length + i.length + b.length + 5), 120, 30);
    save.setBounds(500, 30 * (c.length + i.length + b.length + 6), 120, 30);
    read.setBounds(500, 30 * (c.length + i.length + b.length + 7), 120, 30);
    setLayout(null);
    for (int i = 0; i < c.length; i++) add(c[i]);
    for (int j = 0; j < i.length; j++) add(i[j]);
    for (int i = 0; i < b.length; i++) add(b[i]);
    add(end);
    add(save);
    add(read);
    addMouseListener(this);
    addMouseMotionListener(this);
    end.addActionListener(this);
    save.addActionListener(this);
    read.addActionListener(this);
  }

  public void save(String fname) {
    try {
      FileOutputStream fos = new FileOutputStream(fname);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(objList);
      oos.close();
      fos.close();
    } catch (IOException e) {
    }
  }

  public void load(String fname) {
    try {
      FileInputStream fis = new FileInputStream(fname);
      ObjectInputStream ois = new ObjectInputStream(fis);
      objList = (Vector<Figure>) ois.readObject();
      ois.close();
      fis.close();
    } catch (IOException e) {
    } catch (ClassNotFoundException e) {
    }
    repaint();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    save("paint.dat");
    System.exit(0);
  }

  @Override
  public void paint(Graphics g) {
    Figure f;
    /* paint objects which generated until now */
    for (int i = 0; i < objList.size(); i++) {
      f = (Figure) objList.elementAt(i);
      f.paint(g);
    }
    /* paint a new object */
    if (mode >= 1) obj.paint(g);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    Checkbox cbg_select;
    Checkbox state_select;
    Checkbox backcolor_select;
    x = e.getX();
    y = e.getY();
    cbg_select = cbg.getSelectedCheckbox();
    state_select = state.getSelectedCheckbox();
    backcolor_select = backcolor.getSelectedCheckbox();
    obj = null;

    // item-color settings : i[0] = blue , i[1] = red
    if (state_select == i[0]) a = 1;
    else a = 2;

    // object-type settings
    if (cbg_select == c[0]) {
      mode = 1;
      obj = new Ring(a);
    } else if (cbg_select == c[1]) {
      mode = 2;
      obj = new Circle();
    } else if (cbg_select == c[2]) {
      mode = 2;
      obj = new Box();
    } else if (cbg_select == c[3]) {
      mode = 2;
      obj = new Line();
    } else if (cbg_select == c[4]) {
      mode = 2;
      obj = new Ringa();
    } else if (cbg_select == c[5]) {
      mode = 2;
      obj = new Circlea();
    } else {
      mode = 2;
      obj = new Boxa();
    }

    // background-color settings
    if (backcolor_select == b[0]) this.setBackground(Color.white);
    else if (backcolor_select == b[1]) this.setBackground(Color.gray);
    else this.setBackground(Color.black);

    for (int i = 0; i < c.length; i++)
      if (obj != null) {
        obj.moveto(x, y);
        repaint();
      }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    x = e.getX();
    y = e.getY();
    if (mode == 1) obj.moveto(x, y);
    else if (mode == 2) obj.setWH(x - obj.x, y - obj.y);
    if (mode >= 1) {
      objList.add(0, obj);
      obj = null;
    }
    mode = 0;
    repaint();
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {
    x = e.getX();
    y = e.getY();
    if (mode == 1) {
      obj.moveto(x, y);
    } else if (mode == 2) {
      obj.setWH(x - obj.x, y - obj.y);
    }
    repaint();
  }

  @Override
  public void mouseMoved(MouseEvent e) {}
}

class Coord {
  int x, y;

  Coord() {
    x = y = 0;
  }

  public void move(int dx, int dy) {
    x += dx;
    y += dy;
  }

  public void moveto(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

class Figure extends Coord {
  int color;
  int w, h;

  Figure() {
    color = 0;
    w = h = 0;
  }

  public void paint(Graphics g) {}

  public void setWH(int w, int h) {
    this.w = w;
    this.h = h;
  }
}

class Ring extends Figure implements Serializable {
  int size;

  Ring(int color) {
    size = 40;
    this.color = color;
  }

  @Override
  public void paint(Graphics g) {
    if (color == 1) {
      g.setColor(new Color(0, 0, 255));
    } else {
      g.setColor(new Color(255, 0, 0));
    }
    g.drawOval(x - size / 2, y - size / 2, size, size);
  }
}

class Ringa extends Figure implements Serializable {
  int size;

  Ringa() {
    size = 10;
  }

  @Override
  public void paint(Graphics g) {
    g.fillOval(x - size / 2, y - size / 2, size, size);
  }
}

class Circle extends Figure implements Serializable {
  Circle() {}

  @Override
  public void paint(Graphics g) {
    int r = (int) Math.sqrt((double) (w * w + h * h));
    g.drawOval(x - r, y - r, r * 2, r * 2);
  }
}

class Circlea extends Figure implements Serializable {
  Circlea() {}

  @Override
  public void paint(Graphics g) {
    int r = (int) Math.sqrt((double) (w * w + h * h));
    g.fillOval(x - r, y - r, r * 2, r * 2);
  }
}

class Box extends Figure implements Serializable {
  Box() {}

  @Override
  public void paint(Graphics g) {
    if (w > 0 && h > 0) {
      g.drawRect(x, y, w, h);
    } else if (w < 0 && h > 0) {
      g.drawRect(x + w, y, -w, h);
    } else if (w > 0 && h < 0) {
      g.drawRect(x, y + h, w, -h);
    } else if (w < 0 && h < 0) {
      g.drawRect(x + w, y + h, -w, -h);
    }
  }
}

class Boxa extends Figure implements Serializable {
  Boxa() {}

  @Override
  public void paint(Graphics g) {
    if (w > 0 && h > 0) {
      g.fillRect(x, y, w, h);
    } else if (w < 0 && h > 0) {
      g.fillRect(x + w, y, -w, h);
    } else if (w > 0 && h < 0) {
      g.fillRect(x, y + h, w, -h);
    } else if (w < 0 && h < 0) {
      g.fillRect(x + w, y + h, -w, -h);
    }
  }
}

class Line extends Figure implements Serializable {
  Line() {}

  @Override
  public void paint(Graphics g) {
    g.drawLine(x, y, x + w, y + h);
  }
}
