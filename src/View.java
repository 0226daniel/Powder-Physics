import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by kimjisub on 2017. 6. 29..
 */
public class View extends Frame {

	final static int SCRX = 500;
	final static int SCRY = 500;
	final static int FIXEDFRAME = 60;
	final static int FIXEDFRAME_MS = 1000 / FIXEDFRAME;
	Image offScreen;
	Graphics g;
	private ArrayList<String> log = new ArrayList<>();

	Type[][] map = new Type[SCRX][SCRY];

	enum Type {NONE, FIXED, WATER, STEAM, PLANT, FIRSTFIRE, FIRE, WEEKFIRE}

	Point currPoint;
	Point prevPoint;
	Point realPoint;
	Type currType = Type.WATER;
	Type realType = Type.WATER;
	int pointSize = 2;
	boolean play = true;
	long prevFrame = 0;

	Random r = new Random();

	Color blue = new Color(0x4286f4);
	Color skyBlue = new Color(0x4DC9FF);
	Color green = new Color(0x63B76C);
	Color red = new Color(0xe74c3c);
	Color orange = new Color(0xe67e22);
	Color yellow = new Color(0xf1c40f);


	View() {
		super("Powder Physics (by Kimjisub)");


		setSize(SCRX, SCRY);
		setLayout(new FlowLayout());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				keyPress(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == e.BUTTON1)
					realType = currType;
				else realType = Type.NONE;
				currPoint = e.getPoint();
				realPoint = null;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				realType = currType;
				currPoint = new Point(-1, -1);
				realPoint = e.getPoint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {
				realPoint = null;
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				realPoint = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if (e.getButton() == e.BUTTON1)
					realType = currType;
				else realType = Type.NONE;
				currPoint = e.getPoint();
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int change = e.getWheelRotation();
				if (change < 0) {
					if (pointSize + change >= 2)
						pointSize += change;
					else
						pointSize = 2;
				} else {

					if (pointSize + change <= 100)
						pointSize += change;
					else
						pointSize = 100;
				}
			}
		});

		init();

		setVisible(true);


		long prevTime = System.currentTimeMillis();
		while (true) {
			long currTime = System.currentTimeMillis();
			if (currTime - prevTime >= FIXEDFRAME_MS) {
				prevTime = currTime;

				getInput();
				if(play) {
					calc();
					calc();
				}
				repaint();
			}

		}
	}

	void keyPress(KeyEvent e) {
		switch (e.getKeyChar()) {
			case '1':
				currType = Type.WATER;
				break;
			case '2':
				currType = Type.STEAM;
				break;
			case '3':
				currType = Type.PLANT;
				break;
			case '4':
				currType = Type.FIRSTFIRE;
				break;
			case '5':
				currType = Type.FIXED;
				break;
			case '6':
				currType = Type.NONE;
				break;
			case ' ':
				play = !play;
				break;
			case 0x08:
				init();
				break;
		}

		realType = currType;
	}

	void getInput() {
		if (currPoint != null && prevPoint != null) {
			int currX = (int) currPoint.getX();
			int currY = (int) currPoint.getY();
			int prevX = (int) prevPoint.getX();
			int prevY = (int) prevPoint.getY();

			if (currX >= 0 && currY >= 0 && currX < SCRX && currY < SCRY &&
				prevX >= 0 && prevY >= 0 && prevX < SCRX && prevY < SCRY) {

				if (!((currX == prevX) && (currY == prevY))) {

					int minX;
					int minY;

					int maxX;
					int maxY;

					if (currX >= prevX) {
						minX = prevX;
						minY = prevY;

						maxX = currX;
						maxY = currY;
					} else {
						minX = currX;
						minY = currY;

						maxX = prevX;
						maxY = prevY;
					}


					int incrX = maxX - minX;
					int incrY = maxY - minY;
					float inclination = incrY / (float) incrX;

					if (Math.abs(inclination) <= 1) {

						int[] arr = forArray(0, incrX);
						for (int i : arr) {
							paintBrush(minX + i, (int) (minY + (inclination * i)));
							//map[minX + i][(int) (minY + (inclination * i))] = currType;
						}
						//addLog(" " + incrX + ",\t" + incrY + "\t = " + inclination);
					} else {

						inclination = incrX / (float) incrY;

						int[] arr = forArray(0, incrY);
						for (int j : arr) {
							paintBrush((int) (minX + (inclination * j)), minY + j);
							//map[(int) (minX + (inclination * j))][minY + j] = currType;
						}
						//addLog("_" + incrX + "\t, " + incrY + "\t = " + inclination);
					}
				} else
					paintBrush(currX, currY);
				//map[currX][currY] = currType;
			}
		}


		prevPoint = currPoint;
	}

	void init() {
		for (int x = 0; x < SCRX; x++)
			for (int y = 0; y < SCRY; y++)
				map[x][y] = Type.NONE;
	}

	void calc() {


		// 아래쪽부터계산
		for (int y = SCRY - 1; y >= 0; y--) {

			boolean lr = r.nextBoolean();

			int x = lr ? 0 : SCRX - 1;
			while (lr ? x < SCRX : x >= 0) {

				Type me = map[x][y];
				if (me != Type.NONE && me != Type.FIXED && me != Type.STEAM) {
					Type up;
					Type bottom;
					Type left;
					Type right;

					if (y <= 0) up = map[x][SCRY - 1];
					else up = map[x][y - 1];
					if (y >= SCRY - 1) bottom = map[x][0];
					else bottom = map[x][y + 1];
					if (x <= 0) left = Type.FIXED;
					else left = map[x - 1][y];
					if (x >= SCRX - 1) right = Type.FIXED;
					else right = map[x + 1][y];

					Type[] around = {up, bottom, left, right};

					switch (me) {
						case WATER:
							if (bottom != Type.NONE) {


								if (bottom == Type.STEAM && r.nextInt(3) == 0) {
									bottom = Type.WATER;
									me = Type.STEAM;
								} else if (left == Type.NONE && right == Type.NONE) {
									if (r.nextBoolean())
										left = Type.WATER;
									else
										right = Type.WATER;
									me = Type.NONE;
								} else if (left == Type.NONE) {
									left = Type.WATER;
									me = Type.NONE;
								} else if (right == Type.NONE) {
									right = Type.WATER;
									me = Type.NONE;
								} else
									;


							} else {
								bottom = Type.WATER;
								me = Type.NONE;
							}

							break;


						case PLANT:
							for (int i = 0; i < around.length; i++) {
								if (around[i] == Type.WATER && r.nextInt(10) == 0)
									around[i] = Type.PLANT;
							}

							up = around[0];
							bottom = around[1];
							left = around[2];
							right = around[3];
							/*if (up == Type.WATER)
								if (r.nextInt(10) == 0)
									up = Type.PLANT;
							if (bottom == Type.WATER)
								if (r.nextInt(10) == 0)
									bottom = Type.PLANT;
							if (left == Type.WATER)
								if (r.nextInt(10) == 0)
									left = Type.PLANT;
							if (right == Type.WATER)
								if (r.nextInt(10) == 0)
									right = Type.PLANT;*/
							break;
						case FIRSTFIRE:
							me = Type.FIRE;
							break;

						case FIRE:

							if (r.nextInt(10) == 0)
								me = Type.WEEKFIRE;

							for (int i = 0; i < around.length; i++) {
								if (around[i] == Type.WATER && r.nextInt(3) == 0)
									around[i] = Type.STEAM;
							}
							for (int i = 0; i < around.length; i++) {
								if (around[i] == Type.PLANT)
									around[i] = Type.FIRSTFIRE;
							}

							up = around[0];
							bottom = around[1];
							left = around[2];
							right = around[3];
							break;

						case WEEKFIRE:
							if (r.nextInt(10) == 0)
								me = Type.NONE;
							break;

					}


					map[x][y] = me;
					if (y <= 0) map[x][SCRY - 1] = up;
					else map[x][y - 1] = up;
					if (y >= SCRY - 1) map[x][0] = bottom;
					else map[x][y + 1] = bottom;
					if (x <= 0) ;//map[SCRY-1][y] = left;
					else map[x - 1][y] = left;
					if (x >= SCRX - 1) ;//map[0][y] = right;
					else map[x + 1][y] = right;
				}

				if (lr)
					x++;
				else
					x--;
			}
		}


		//윗쪽부터 계산
		for (int y = 0; y < SCRY; y++) {

			boolean lr = r.nextBoolean();

			int x = lr ? 0 : SCRX - 1;
			while (lr ? x < SCRX : x >= 0) {

				Type me = map[x][y];
				if (me == Type.STEAM) {
					Type up;
					Type bottom;
					Type left;
					Type right;

					if (y <= 0) up = map[x][SCRY - 1];
					else up = map[x][y - 1];
					if (y >= SCRY - 1) bottom = map[x][0];
					else bottom = map[x][y + 1];
					if (x <= 0) left = Type.FIXED;
					else left = map[x - 1][y];
					if (x >= SCRX - 1) right = Type.FIXED;
					else right = map[x + 1][y];

					Type[] around = {up, bottom, left, right};

					switch (me) {
						case STEAM:
							int aroundHum = 0;
							for (int i = 0; i < around.length; i++) {
								if (around[i] == Type.STEAM)
									aroundHum++;
								else if (around[i] == Type.WATER)
									aroundHum += 2;
							}

							if (r.nextInt(500 + (8 - aroundHum) * 100) == 0) {

								me = Type.WATER;

							} else if (up != Type.NONE) {

								if (up == Type.WATER && r.nextInt(3) == 0) {
									up = Type.STEAM;
									me = Type.WATER;
								} else if (left == Type.NONE && right == Type.NONE) {
									if (r.nextBoolean())
										left = Type.STEAM;
									else
										right = Type.STEAM;
									me = Type.NONE;
								} else if (left == Type.NONE) {
									left = Type.STEAM;
									me = Type.NONE;
								} else if (right == Type.NONE) {
									right = Type.STEAM;
									me = Type.NONE;
								} else
									;


							} else {
								up = Type.STEAM;
								me = Type.NONE;
							}
							break;

					}


					map[x][y] = me;
					if (y <= 0) map[x][SCRY - 1] = up;
					else map[x][y - 1] = up;
					if (y >= SCRY - 1) map[x][0] = bottom;
					else map[x][y + 1] = bottom;
					if (x <= 0) ;//map[SCRY-1][y] = left;
					else map[x - 1][y] = left;
					if (x >= SCRX - 1) ;//map[0][y] = right;
					else map[x + 1][y] = right;
				}

				if (lr)
					x++;
				else
					x--;
			}
		}
	}

	@Override
	public void paint(Graphics graphics) {
		if (offScreen == null) offScreen = createImage(SCRX, SCRY);
		if (g == null) g = offScreen.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, SCRX, SCRY);


		for (int x = 0; x < SCRX; x++) {
			for (int y = 0; y < SCRY; y++) {
				switch (map[x][y]) {
					case NONE:
						break;
					case FIXED:
						g.setColor(Color.black);
						g.drawOval(x, y, 1, 1);
						break;
					case WATER:
						g.setColor(blue);
						g.drawOval(x, y, 1, 1);

						break;
					case STEAM:
						g.setColor(skyBlue);
						g.drawOval(x, y, 1, 1);

						break;
					case PLANT:
						g.setColor(green);
						g.drawOval(x, y, 1, 1);

						break;
					case FIRSTFIRE:
						g.setColor(red);
						g.drawOval(x, y, 1, 1);

						break;
					case FIRE:
						g.setColor(orange);
						g.drawOval(x, y, 1, 1);

						break;
					case WEEKFIRE:
						g.setColor(yellow);
						g.drawOval(x, y, 1, 1);

						break;

				}

			}
		}

		if (realPoint != null) {
			g.setColor(Color.black);
			int halfSize = (int) ((float) pointSize / 2);
			g.drawOval(realPoint.x - halfSize, realPoint.y - halfSize, pointSize, pointSize);
		}


		//UI

		g.setColor(Color.black);

		long currFrame = System.currentTimeMillis();
		g.drawString((int) (1000 / ((float) (currFrame - prevFrame))) + "fps", 400, 50);
		prevFrame = currFrame;


		String[] status = {
			"　space : 재생/정지 (" + (play ? "재생중" : "정지") + ")",
			(realType == Type.WATER ? "▷" : "　") + "1 : 물",
			(realType == Type.STEAM ? "▷" : "　") + "2 : 수증기",
			(realType == Type.PLANT ? "▷" : "　") + "3 : 식물",
			(realType == Type.FIRSTFIRE ? "▷" : "　") + "4 : 불",
			(realType == Type.FIXED ? "▷" : "　") + "5 : 고정체",
			(realType == Type.NONE ? "▷" : "　") + "6 : 지우개",
			"　",
			"　⌫ : 화면 초기화",
			"　붓 크기 : " + pointSize
		};
		for (int i = 0; i < status.length; i++) {
			g.drawString(status[i], 0, 50 + i * 12);
		}
		int num = Math.min(15, log.size());
		for (int i = 0; i < num; i++) {
			String str = log.get(i);
			g.drawString(str, 0, 300 + (12 - i) * 12);
			System.out.println(str);
		}


		graphics.drawImage(offScreen, 0, 0, this);
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	private void addLog(String str) {
		log.add(0, str);
	}

	void paintBrush(int a, int b) {
		int halfSize = (int) ((float) pointSize / 2);
		for (int i = -halfSize; i < halfSize; i++) {
			for (int j = -halfSize; j < halfSize; j++) {
				int x = i + a;
				int y = j + b;

				if (i * i + j * j <= halfSize * halfSize && x >= 0 && y >= 0 && x < SCRX && y < SCRY) {
					if (map[x][y] != Type.FIXED || currType == Type.NONE)
						map[x][y] = realType;
				}
			}
		}
	}


	int[] forArray(int a, int b) {
		int length = Math.abs(a - b) + 1;
		int[] ret = new int[length];
		if (a < b)
			for (int i = 0; i < length; i++)
				ret[i] = a + i;
		else
			for (int i = 0; i < length; i++)
				ret[i] = a - i;

		return ret;
	}
}
