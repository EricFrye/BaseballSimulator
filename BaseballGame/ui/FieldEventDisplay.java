package ui;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import datatype.Coordinate3D;
import game.InningCounters;
import helpers.DebuggerInfo;
import stadium.Stadium;
import stadium.Wall;
import stats.BattingStatline;

public class FieldEventDisplay {
	
	private static final long serialVersionUID = 1L;
	
	private Stadium curStadium;
	private BasicBoard view;
	
	public FieldEventDisplay (int x, int y, int offset, Stadium curStadium) {
		
		this.curStadium = curStadium;
		this.view = new BasicBoard (x, y, offset);
		
	}
	
	public JLabel getFieldImage () {
		return view.getDisplay();
	}
	
	//draws the outfield walls and foul lines
	public void drawFieldOutline () {
		
		BufferedImage board = view.getData();
		
		int ySubtract = board.getHeight(); //this is need to flip the image over the x-axis.  otherwise the image is drawn in the 4th quadrant, which is confusing
		int basePathDistance = 90;
		
		int offset = curStadium.dim.get("f");
		
		List <Wall> walls = curStadium.getWalls();
		Wall w1 = walls.get(0);
		Wall w2 = walls.get(1);
		Wall w3 = walls.get(2);
		Wall w4 = walls.get(3);
		
		Coordinate3D leftCorner = w1.getP1();
		Coordinate3D leftCenterCorner = w2.getP1();
		Coordinate3D centerCorner = w3.getP1();
		Coordinate3D rightCenterCorner = w4.getP1();
		Coordinate3D rightCorner = w4.getP2();
				
		Graphics2D leftFieldLine = board.createGraphics();
		leftFieldLine.setColor(Color.WHITE);
		leftFieldLine.draw(new Line2D.Double(offset, ySubtract-(offset), offset, ySubtract-(leftCorner.y+offset)));
		leftFieldLine.dispose();
		
		Graphics2D rightFieldLine = board.createGraphics();
		rightFieldLine.setColor(Color.WHITE);
		rightFieldLine.draw(new Line2D.Double(offset, ySubtract-(offset), offset+rightCorner.x, ySubtract-(offset)));
		rightFieldLine.dispose();
		
		Graphics2D secondBaseLine = board.createGraphics();
		secondBaseLine.setColor(Color.WHITE);
		secondBaseLine.draw(new Line2D.Double(offset+basePathDistance, ySubtract-(offset), offset+basePathDistance, ySubtract-(offset+basePathDistance)));
		secondBaseLine.dispose();
		
		Graphics2D thirdBaseLine = board.createGraphics();
		thirdBaseLine.setColor(Color.WHITE);
		thirdBaseLine.draw(new Line2D.Double(offset+basePathDistance, ySubtract-(offset+basePathDistance), offset, ySubtract-(offset+basePathDistance)));
		thirdBaseLine.dispose();
		
		Graphics2D leftToLeftCenter = board.createGraphics();
		leftToLeftCenter.setColor(Color.WHITE);
		leftToLeftCenter.draw(new Line2D.Double(leftCorner.x+offset,ySubtract-(leftCorner.y+offset), leftCenterCorner.x+offset, ySubtract-(leftCenterCorner.y+offset)));
		leftToLeftCenter.dispose();
		
		Graphics2D leftCenterToCenter = board.createGraphics();
		leftCenterToCenter.setColor(Color.WHITE);
		leftCenterToCenter.draw(new Line2D.Double(leftCenterCorner.x+offset, ySubtract-(leftCenterCorner.y+offset), centerCorner.x+offset, ySubtract-(centerCorner.y+offset)));
		leftCenterToCenter.dispose();
		
		Graphics2D centerToRightCenter = board.createGraphics();
		centerToRightCenter.setColor(Color.WHITE);
		centerToRightCenter.draw(new Line2D.Double(centerCorner.x+offset, ySubtract-(centerCorner.y+offset), rightCenterCorner.x+offset, ySubtract-(rightCenterCorner.y+offset)));
		centerToRightCenter.dispose();
		
		Graphics2D rightCenterToRight = board.createGraphics();
		rightCenterToRight.setColor(Color.WHITE);
		rightCenterToRight.draw(new Line2D.Double(rightCenterCorner.x+offset, ySubtract-(rightCenterCorner.y+offset), rightCorner.x+offset, ySubtract-(rightCorner.y+offset)));
		rightCenterToRight.dispose();
		
	}
	
	public void removeSpot (Coordinate3D hitBall, int size) {
		view.clearObject(hitBall, size);
	}
	
	public void drawBall (Coordinate3D hitBall, int color, int size) {
		view.drawObject(hitBall, color, size);
	}
	
}
