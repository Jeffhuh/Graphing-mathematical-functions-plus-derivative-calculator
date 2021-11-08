package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import parser.BinaryTree;
import parser.Derivative;
import parser.LeftRightException;
import parser.Node;
import parser.Parser;
import parser.Simplification;


public class Main extends Application {
	private static final Color[] GRAPHCOLORS = {Color.rgb(108, 151, 196), Color.rgb(232, 100, 100), Color.rgb(217, 187, 78), Color.rgb(199, 135, 195), Color.rgb(101, 199, 104)};
	private static double x0Pos = 140d;
	private static double y0Pos = 350d;
	private static final double xTotalLength = 563d;
	private static final double yTotalLength = 470d;
	private static final double UNIT = 45d;
	private static final double MAX_UNIT = 60d;
	private static final double MIN_UNIT= 30d;
	private static final Color GRID_COLOR = Color.rgb(79, 136, 201,0.2);
	private static final double[] UNIT_VALUES = {1d,2d,5d};
	private static int unit_values_index = 0; // after 4
	private static double unit_exp = 0; // after 12
	private static double unit_valueX = 1d; 
	private static double unit_valueY = 1d;
	private static double unit_pixelX = 45d; 
	private static double unit_pixelY = 45d; 
	private static double pixelValueX = unit_valueX / unit_pixelX;
 	private static double pixelValueY = unit_valueY / unit_pixelY;
 	private static double zoomFactorX = 1.2d;
 	private static double zoomFactorY = 1.2d;
 	
 	private static double xStartVector;
 	private static double yStartVector;
 	private static double minXAxis;
 	private static double maxXAxis;
 	private static double minYAxis;
 	private static double maxYAxis;
 	
 	private boolean showDerivativeControl;
 	
 	private HashMap<Integer, List<Point>> coordinates;
	
	private Deque<Line> xGrid; //+/first -> -/last 
	private Deque<Line> yGrid;
	private Deque<Label> xGridLabel;
	private Deque<Label> yGridLabel;
	private List<Group> graphs = new ArrayList<>();
	private String[] graphsExp;
	//private Deque<> dotValues; 
	
	private HBox functionControl;
	private HBox graphControl;
	private HBox derivativeControl;
	private VBox graphFunctionControl;
	
	private VBox mainBox;
	
	private Text		textDerivative;
	private Button 		btnGetDerivative;
	private TextField 	txtFunctionInput;
	private Label 		lblFunction;
	
	private MenuBar 	menuBar;
	private Menu 		menuData;
	private MenuItem 	menuItemGraphFunction;
	
	private Pane coordinateSystem;
	
	private Parser parser;
	private Node node;
	
	@Override
	public void start(Stage primaryStage) {
		Label functionIcon = new Label("f(x)");
		Font font = Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC,13);
		functionIcon.setFont(font);
		menuBar = new MenuBar();
		menuData = new Menu("", functionIcon);
		menuItemGraphFunction = new MenuItem("Graph function");
		menuData.getItems().add(menuItemGraphFunction);
		menuBar.getMenus().add(menuData);
		menuBar.setStyle("-fx-background-color: rgb(189, 209, 240);");
				
		lblFunction = new Label("y     = ");
		lblFunction.setFont(Font.font("MJXc-TeX-main-R", FontPosture.ITALIC,17));
		txtFunctionInput = new TextField();
		btnGetDerivative = new Button("Calculate derivative of y");
		btnGetDerivative.setMinHeight(30);
		//btnGetDerivative.s
		txtFunctionInput.setMinSize(370, 30);
		xGrid = new ArrayDeque<>();
		yGrid = new ArrayDeque<>();
		xGridLabel = new ArrayDeque<>();
		yGridLabel = new ArrayDeque<>();
		
		coordinateSystem = new Pane();
		//coordinateSystem.setMinSize(xTotalLength, yTotalLength);
		coordinateSystem.setPrefSize(xTotalLength, yTotalLength);
		coordinateSystem.setBorder(new Border(
				new BorderStroke(Color.rgb(192,192,192), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		/*coordinateSystem.setOnMouseReleased((MouseEvent event) -> {
		
		});*/
		
		coordinateSystem.setStyle("-fx-effect: dropshadow(gaussian, rgb(192,192,192), 6, 0, 0, 0);");
		
		coordinateSystem.setOnMousePressed((MouseEvent event) -> {
			xStartVector = event.getX();
			yStartVector = event.getY();
		});
		
		coordinateSystem.setOnMouseDragged((MouseEvent event) -> {
			double xEndVector = event.getX();
			double yEndVector = event.getY();
			//System.out.println(x0Pos+"  :  "+y0Pos);
			x0Pos = x0Pos + 1 * (xEndVector - xStartVector);
			y0Pos = y0Pos + 1 * (yEndVector - yStartVector);
			//System.out.println((xEndVector - xStartVector) + "  :  "+ (yEndVector - yStartVector));
			xStartVector = xEndVector;
			yStartVector = yEndVector;
			generateFrame();
			  
		});
		
		coordinateSystem.setOnScroll((ScrollEvent event) -> {
			//System.out.println(event.getDeltaY()+" :: "+event.getX()+" : "+event.getY());
			double mousePosX = event.getX();
			double mousePosY = event.getY();

			if (event.getDeltaY() > 0) {
				unit_pixelX += 5d;
				unit_pixelY += 5d;
				x0Pos = (x0Pos - mousePosX) * zoomFactorX + mousePosX;
				y0Pos = (y0Pos - mousePosY) * zoomFactorY + mousePosY;
			} else {
				unit_pixelX -= 5d;
				unit_pixelY -= 5d;
				x0Pos = (x0Pos - mousePosX) / zoomFactorX + mousePosX;
				y0Pos = (y0Pos - mousePosY) / zoomFactorY + mousePosY;
			}
			
			if (unit_pixelX > MAX_UNIT) {
				unit_pixelX = MIN_UNIT;
				unit_pixelY = MIN_UNIT;
				unit_values_index = (unit_values_index + 2) % 3; // ((unit_values_index - 1) + 3) % 3
				unit_exp = unit_values_index == 2 ? unit_exp - 1 : unit_exp;
			} else if (unit_pixelX < MIN_UNIT) {
				unit_pixelX = MAX_UNIT;
				unit_pixelY = MAX_UNIT;
				unit_values_index = (unit_values_index + 1) % 3;
				unit_exp = unit_values_index == 0 ? unit_exp + 1 : unit_exp;
			}
			unit_valueX = UNIT_VALUES[unit_values_index] * Math.pow(10, unit_exp);
			unit_valueY = UNIT_VALUES[unit_values_index] * Math.pow(10, unit_exp);
			pixelValueX = unit_valueX / unit_pixelX;
			pixelValueY = unit_valueY / unit_pixelY;
			generateFrame();  
		});
		generateGrid(coordinateSystem);
		btnGetDerivative.setOnAction(event -> {
			// reset binary tree
			BinaryTree.root = new Node(null);
			
			try {
				parser = new Parser(txtFunctionInput.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Derivative dydx = new Derivative();
			Node deriv = dydx.getDerivative(BinaryTree.root.left.copy());
			BinaryTree.setParents(deriv);
			Simplification simp = new Simplification();
			Node simplifiedExp = simp.simplify(deriv);
			String strDerivative = "=   " + BinaryTree.binaryTreeToString(simplifiedExp);
			
			textDerivative.setText(strDerivative);
			derivativeControl.setVisible(true);
			derivativeControl.setManaged(true);
		});
		
		txtFunctionInput.setOnKeyPressed(event -> {
			  if (event.getCode() == KeyCode.ENTER){
				  String s = txtFunctionInput.getText();
				  removeGraphs();
				  graphsExp = s.split(";");
				  for (int i = 0; i < graphsExp.length; i++) {
					  try {
						Color c;  
					  	if (i > 4) {
					  		Random r = new Random();
					  		c = Color.rgb(r.nextInt(250)+6, r.nextInt(250)+6, r.nextInt(250)+6);
					  	} else {
					  		c = GRAPHCOLORS[i];
					  	}
						BinaryTree.root = new Node(null);
						parser = new Parser(graphsExp[i]);
						generateGraph(c);
					  } catch (Exception e) {
						  //e.printStackTrace();
					  }  
				  }
				  if (derivativeControl.isVisible()) {
					  derivativeControl.setVisible(false);
					  derivativeControl.setManaged(false);
				  }
				  displayGraphs(); 
			  }
		}); 
		InputStream stream = null;
		try {
			stream = new FileInputStream("src/res/img/dydx.png");
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
		Image image = new Image(stream);
		ImageView imageView = new ImageView();
		imageView.setImage(image);
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		textDerivative = new Text();
		textDerivative.setFont(Font.font("MJXc-TeX-main-R", 16));
		derivativeControl = new HBox(10, imageView, textDerivative);
		derivativeControl.setMargin(imageView, new Insets(0,0,0,55));
		derivativeControl.setMargin(textDerivative, new Insets(10,0,0,0));
		derivativeControl.setVisible(false);
		derivativeControl.setManaged(false);
		functionControl = new HBox(0, lblFunction, txtFunctionInput, btnGetDerivative);
		functionControl.setMargin(lblFunction, new Insets(0,0,0,50));
		functionControl.setMargin(btnGetDerivative, new Insets(0,0,0,50));
		graphControl = new HBox(0, coordinateSystem);
		graphControl.setMargin(coordinateSystem, new Insets(0,0,0,115));
		VBox.setMargin(functionControl, new Insets(34,0,0,25));
		graphFunctionControl = new VBox(30, functionControl, derivativeControl, graphControl);
		mainBox = new VBox(10, menuBar, graphFunctionControl);
		Scene scene = new Scene(mainBox, 750, 700);
		primaryStage.getIcons().add(new Image("res/img/contour_integral.png"));
		primaryStage.setScene(scene);
		primaryStage.setTitle("WolframAlpha - but worse");
		primaryStage.show();
	}
	
	public void generateGraph(Color color) {
		/*
	 	double minXAxis = pixelValueX * x0Pos; //todo 
	 	double minYAxis = pixelValueY * (yTotalLength - y0Pos)*(-1);
	 	double maxYAxis = pixelValueY * y0Pos;
	 	Iterator<Label> it = xGridLabel.descendingIterator();
	 	while (it.hasNext()) {
	 		Label l = it.next();
	 		if (l instanceof SpecialLabel) {
	 			minXAxis *= -1d;
	 		}
	 	}
	 	it = yGridLabel.descendingIterator();*/
	 	node = BinaryTree.root.left;
	 	BinaryTree.optimize(node);
	 	List<Double> xCoordinatesPixel = new ArrayList<>();
	 	List<Double> yCoordinatesPixel = new ArrayList<>();
	 	//coordinates = new HashMap<>();
	 	
	 	double xValues = minXAxis;
		for (int i = 0; i < xTotalLength; i+=1) {
			try {
				xCoordinatesPixel.add((double)i);
				BinaryTree.value = xValues;
				double valueY = BinaryTree.evaluate(node);
				//coordinates.put(i, new ArrayList<>());
				//System.out.println(false);
				//to-do recursive function for 2 far-distanced points 
				if (valueY < minYAxis) {
					yCoordinatesPixel.add(Double.NEGATIVE_INFINITY);
					xValues += pixelValueX;
					continue;
				} else if (valueY > maxYAxis) {
					yCoordinatesPixel.add(Double.POSITIVE_INFINITY);
					xValues += pixelValueX;
					continue;
				}
				//System.out.println(xValues +"  :  "+valueY);
				//coordinates.get(i).add(new Point(i, valueY));
				
				/*if(i >= 2) {
					createMiddlePoint(coordinates.get(i).get(0), coordinates.get(i - 1).get(0));
				}
				if (deltaY > 5) {
					if (b) {
						// latest index, indexes which are added
						//additionalIndexes += recursiveFunction(xCoordinatesPixel, yCoordinatesPixel, xCoordinatesPixel.size()-1, 0);
						double xAverage = (xCoordinatesPixel.get(i-2)+xCoordinatesPixel.get(i-1))/2d;
						BinaryTree.value = (xAverage-x0Pos) * pixelValueX;
						double temp_valueY = BinaryTree.evaluate(node);
						if(temp_valueY < minYAxis || temp_valueY > maxYAxis) {
							xValues += pixelValueX;
							b = false;
							continue;
						}
						double temp_pixelPos_valueY = y0Pos - temp_valueY/pixelValueY;
						System.out.println(temp_pixelPos_valueY);
						xCoordinatesPixel.add(i-1, xAverage);
						yCoordinatesPixel.add(i-1, temp_pixelPos_valueY);
						i--;
						continue;
						/*int k;
						if (deltaY > 100) {
							k = 20;
						} else {
							k = (int)Math.floor(deltaY/4);
						}
						double a = 1/(k+1);
						double temp_xValues = xValues;
						for (int j = 1; j <= k; j++) {
							temp_xValues += a;
							BinaryTree.value = temp_xValues;
							double temp_valueY = BinaryTree.evaluate(node);
							if(temp_valueY > maxYAxis || temp_valueY < minYAxis) {
								continue;
							}
							xCoordinatesPixel.add(i-1+j*a);
							
							yCoordinatesPixel.add(y0Pos-temp_valueY/pixelValueY);
							yIndex++;
						}
					} else {
						b = true;
					}
				}*/
				yCoordinatesPixel.add((maxYAxis - valueY)/pixelValueY);
				//yCoordinatesPixel.add(y0Pos-valueY/pixelValueY);
				xValues += pixelValueX;
				//double pixelPos_valueY = y0Pos - valueY/pixelValueY;
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		
		for (int i = 1; i < yCoordinatesPixel.size(); i++) {
			double y1 = yCoordinatesPixel.get(i-1);
			double y2 = yCoordinatesPixel.get(i);
			/*if (y1 != (double)(int)y1) {
				continue;
			}*/
			if (y1 == Double.NaN) {
				continue;
			}
			if (y2 > 0 && y2 < yTotalLength && (y1 == Double.POSITIVE_INFINITY || y1 == Double.NEGATIVE_INFINITY)) {
				BinaryTree.value = minXAxis + xCoordinatesPixel.get(i-1) * pixelValueX;
				//double temp_y1 = y0Pos - BinaryTree.evaluate(node)/pixelValueY;
				double temp_y1 = (maxYAxis - BinaryTree.evaluate(node))/pixelValueY;
				double k = y2-temp_y1;
				double d = temp_y1-k*xCoordinatesPixel.get(i-1);
				double x0 = ((y1 == Double.NEGATIVE_INFINITY ? yTotalLength : 0) -d)/k;
				xCoordinatesPixel.add(i, x0);
				yCoordinatesPixel.add(i, y1 == Double.POSITIVE_INFINITY ? 0 : yTotalLength);
				//System.out.println(xCoordinatesPixel.get(i));
			} else if (y1 > 0 && y1 < yTotalLength && (y2 == Double.POSITIVE_INFINITY || y2 == Double.NEGATIVE_INFINITY)) {
				BinaryTree.value = minXAxis + xCoordinatesPixel.get(i) * pixelValueX;
				//double temp_y2 = y0Pos - BinaryTree.evaluate(node)/pixelValueY;
				double temp_y2 = (maxYAxis - BinaryTree.evaluate(node))/pixelValueY;
				double k = temp_y2-y1;
				double d = y1-k*xCoordinatesPixel.get(i-1);
				double x0 = ((y2 == Double.NEGATIVE_INFINITY ? yTotalLength : 0)-d)/k;
				xCoordinatesPixel.add(i, x0);
				yCoordinatesPixel.add(i, y2 == Double.POSITIVE_INFINITY ? 0 : yTotalLength);
				//System.out.println(xCoordinatesPixel.get(i));
			}
		}
		for (int i = 2; i < xCoordinatesPixel.size(); i++) {
			double y1 = yCoordinatesPixel.get(i-2);
			double y2 = yCoordinatesPixel.get(i-1);
			if (y1 == Double.NEGATIVE_INFINITY || y1 == Double.POSITIVE_INFINITY || y1 == Double.NaN || y2 == Double.NEGATIVE_INFINITY || y2 == Double.POSITIVE_INFINITY || y2 == Double.NaN) {
				continue;
			}
			double deltaY = Math.abs(Math.abs(y1) - Math.abs(y2));
			if (deltaY > 40) {
				double xAverage = (xCoordinatesPixel.get(i-1)+xCoordinatesPixel.get(i-2))/2d;
				if (Math.abs(xAverage - xCoordinatesPixel.get(i-2)) < 0.2d) {
					continue;
				}
				BinaryTree.value = minXAxis + xAverage * pixelValueX;
				//double temp_valueY_pixelPos = y0Pos - BinaryTree.evaluate(node)/pixelValueY;
				double temp_valueY_pixelPos = (maxYAxis - BinaryTree.evaluate(node))/pixelValueY;
				System.out.println(minXAxis + xAverage * pixelValueX +"  :  "+BinaryTree.evaluate(node));
				xCoordinatesPixel.add(i-1, xAverage);
				yCoordinatesPixel.add(i-1, temp_valueY_pixelPos);
				i--;
			}
		}
		//System.out.println(xCoordinatesPixel.size());
		Group group = new Group();
		for (int i = 2; i < xCoordinatesPixel.size(); i++) {
			//System.out.println(xCoordinatesPixel.get(i-1)+"  :  "+yCoordinatesPixel.get(i-1));
			Line line = new Line(xCoordinatesPixel.get(i-1), yCoordinatesPixel.get(i-1), xCoordinatesPixel.get(i), yCoordinatesPixel.get(i));
			line.setStrokeWidth(0.75d);
			line.setStroke(color);
			group.getChildren().add(line);
		}
		graphs.add(group);
	}
	
	private void createMiddlePoint(Point p1, Point p2) {
		if(calculateDistance(p1.getY(), p2.getY()) > 5) {
			double posX = (double) ((p1.getX() + p2.getX()) / 2);
			double posY = (double) 0; // TODO: as binary tree what y is at posX
			
			Point p = new Point(posX, posY);
			
			coordinates.get(posX).add(p);
			
			createMiddlePoint(p, p1);
			createMiddlePoint(p, p2);
		}
	}
	
	private double calculateDistance(double y1, double y2) {
		return Math.abs(y1 - y2);
	}
	
	//
	/*public int recursiveFunction(List<Double> xCoordinatesPixel, List<Double> yCoordinatesPixel, int xIndex, int additionalIndexes) {
		double valueXBetweenIndex = (xCoordinatesPixel.get(xIndex) - xCoordinatesPixel.get(xIndex-1)) / 2d;
		double valueYbetweenIndex = BinaryTree.evaluate(node);
		double pxielValueY = y0Pos - valueYbetweenIndex/pixelValueY;
		xCoordinatesPixel.add(xIndex-1, valueXBetweenIndex);
		yCoordinatesPixel.add(xIndex-1, pixelValueY);
		double deltaY = Math.abs(Math.abs(yCoordinatesPixel.get(xIndex)) - Math.abs(yCoordinatesPixel.get(xIndex-1)));
		if (deltaY > 5) {
			recursiveFunction(xCoordinatesPixel, yCoordinatesPixel, xCoordinatesPixel.get(xIndex-1));
		}
	}*/
	
	public static void main(String[] args) {
		launch(args);
	}
	
	//////////////////////////////////////////////////
	public void generateGrid(Pane coordinateSystem) {
		String labelStyle = "-fx-font-size:7px";
		double pos;
		int i;
		double decimal_place = Math.pow(10, -unit_exp);
		boolean b = unit_exp < 0 ? true : false;
		if (x0Pos < 0) {
			minXAxis = -x0Pos * pixelValueX;
			maxXAxis = (-x0Pos + xTotalLength) * pixelValueX;
			pos = unit_pixelX + (x0Pos % unit_pixelX);
			i = (int)(-x0Pos / unit_pixelX) + 1;
			while (pos < xTotalLength) {
				Line line = new Line(pos,0,pos,yTotalLength);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				yGrid.offerFirst(line); //unit_exp < 0 ? Math.round(Math.pow(10, -unit_exp) * i*unit_valueX)/Math.pow(10, -unit_exp) : (int) i*unit_valueX; 
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueX)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueX));
				}
				Label label = new Label(str);
				label.setLayoutX(pos-3.5*((double)str.length()/2d));
				label.setLayoutY(y0Pos < 0 ? -9 : y0Pos > yTotalLength ? yTotalLength + 2 : y0Pos + 2);
				label.setStyle(labelStyle);
				xGridLabel.offerFirst(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i++;
				pos += unit_pixelX;
			}
		} else if (xTotalLength < x0Pos) {
			minXAxis = -x0Pos * pixelValueX;
			maxXAxis = (xTotalLength - x0Pos) * pixelValueX; 
			pos = xTotalLength - unit_pixelX + ((x0Pos - xTotalLength) % unit_pixelX);
			i = (int)((x0Pos - xTotalLength) / -unit_pixelX) - 1;
			while (0 < pos) {
				Line line = new Line(pos,0,pos,yTotalLength);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				yGrid.offerLast(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueX)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueX));
				}
				Label label = new Label(str);
				label.setLayoutX(pos-3.5*((double)str.length()/2d));
				label.setLayoutY(y0Pos < 0 ? -9 : y0Pos > yTotalLength ? yTotalLength + 2 : y0Pos + 2);
				label.setStyle(labelStyle);
				xGridLabel.offerLast(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i--;
				pos -= unit_pixelX;
			}
		} else {
			minXAxis = -x0Pos * pixelValueX;
			maxXAxis = (xTotalLength - x0Pos) * pixelValueX;
			pos = x0Pos + unit_pixelX;
			i = 1;
			/*Line x = new Line(0,y0Pos,xTotalLength,y0Pos);
			x.setStrokeWidth(1);
			x.setStroke(Color.BLACK);*/
			Line y = new Line(x0Pos,0,x0Pos,yTotalLength);
			y.setStrokeWidth(1);
			y.setStroke(Color.BLACK);
			yGrid.offerFirst(y);
			coordinateSystem.getChildren().add(y);
			while (pos < xTotalLength) {
				Line line = new Line(pos,0,pos,yTotalLength);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				yGrid.offerFirst(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueX)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueX));
				}
				Label label = new Label(str);
				label.setLayoutX(pos-3.5*((double)str.length()/2d));
				label.setLayoutY(y0Pos < 0 ? -9 : y0Pos > yTotalLength ? yTotalLength + 2 : y0Pos + 2);
				label.setStyle(labelStyle);
				xGridLabel.offerFirst(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i++;
				pos += unit_pixelX;
			}
			pos = x0Pos - unit_pixelX;
			i = -1;
			while(0 < pos) {
				Line line = new Line(pos,0,pos,yTotalLength);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				yGrid.offerLast(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueX)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueX));
				}
				Label label = new Label(str);
				label.setLayoutX(pos-3.5*((double)str.length()/2d));
				label.setLayoutY(y0Pos < 0 ? -9 : y0Pos > yTotalLength ? yTotalLength + 2 : y0Pos + 2);
				label.setStyle(labelStyle);
				xGridLabel.offerLast(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				pos -= unit_pixelX;
				i--;
			}
		}
		
		if (y0Pos < 0) {
			minYAxis = (y0Pos - yTotalLength) * pixelValueY;
			maxYAxis = y0Pos * pixelValueY;
			pos = unit_pixelY + (y0Pos % unit_pixelY);
			i = (int)(y0Pos / unit_pixelY) - 1;
			while (pos < yTotalLength) {
				Line line = new Line(0,pos,xTotalLength,pos);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				xGrid.offerFirst(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueY)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueY));
				}
				Label label = new Label(str);
				label.setLayoutX(x0Pos < 0 ? -3.5*str.length() : x0Pos > xTotalLength ? xTotalLength + 1 : x0Pos+2);
				label.setLayoutY(pos-5);
				label.setStyle(labelStyle);
				yGridLabel.offerFirst(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i--;
				pos += unit_pixelY;
			}
		} else if (yTotalLength < y0Pos) {
			minYAxis = (y0Pos - yTotalLength) * pixelValueY;
			maxYAxis = y0Pos * pixelValueY;
			pos = yTotalLength - unit_pixelY + ((y0Pos - yTotalLength) % unit_pixelY);
			i = (int)((y0Pos - yTotalLength) / unit_pixelY) + 1;
			while (0 < pos) {
				Line line = new Line(0,pos,xTotalLength,pos);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				xGrid.offerLast(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueY)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueY));
				}
				Label label = new Label(str);
				label.setLayoutX(x0Pos < 0 ? -3.5*str.length() : x0Pos > xTotalLength ? xTotalLength + 1 : x0Pos+2);
				label.setLayoutY(pos-5);
				label.setStyle(labelStyle);
				yGridLabel.offerLast(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i++;
				pos -= unit_pixelY;
			}
		} else {
			minYAxis = (y0Pos - yTotalLength) * pixelValueY;
			maxYAxis = y0Pos * pixelValueY;
			pos = y0Pos + unit_pixelY;
			i = -1;
			Line x = new Line(0,y0Pos,xTotalLength,y0Pos);
			x.setStrokeWidth(1);
			x.setStroke(Color.BLACK);
			xGrid.offerFirst(x);
			coordinateSystem.getChildren().add(x);
			while (pos < yTotalLength) {
				Line line = new Line(0,pos,xTotalLength,pos);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				xGrid.offerFirst(line);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueY)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueY));
				}
				Label label = new Label(str);
				label.setLayoutX(x0Pos < 0 ? -3.5*str.length() : x0Pos > xTotalLength ? xTotalLength + 1 : x0Pos+2);
				label.setLayoutY(pos-5);
				label.setStyle(labelStyle);
				yGridLabel.offerFirst(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				i--;
				pos += unit_pixelY;
			}
			pos = y0Pos - unit_pixelY;
			i = 1;
			while (0 < pos) {
				Line line = new Line(0,pos,xTotalLength,pos);
				line.setStrokeWidth(1);
				line.setStroke(GRID_COLOR);
				String str;
				if (b) {
					double d = Math.round(decimal_place* i * unit_valueY)/decimal_place;
					if (d % 1 == 0) {
						str = String.valueOf((int)d);
					} else {
						str = String.valueOf(d);
					}
				} else {
					str = String.valueOf((int) (i*unit_valueY));
				}
				xGrid.offerLast(line);
				Label label = new Label(str);
				label.setLayoutX(x0Pos < 0 ? -3.5*str.length() : x0Pos > xTotalLength ? xTotalLength + 1 : x0Pos+2);
				label.setLayoutY(pos-5);
				label.setStyle(labelStyle);
				yGridLabel.offerLast(label);
				coordinateSystem.getChildren().add(label);
				coordinateSystem.getChildren().add(line);
				pos -= unit_pixelY;
				i++;
			}
		}
		/*System.out.println("minXAxis: "+pixelValueX * x0Pos+"  :  "+minXAxis);
		System.out.println("maxXAxis: "+"  :  "+maxXAxis);
		System.out.println("minYAxis: "+pixelValueY * (yTotalLength - y0Pos)*(-1)+"  :  "+minYAxis);
		System.out.println("maxYAxis: "+pixelValueY * y0Pos+"  :  "+maxYAxis);*/
	}
	
	public void removeLabelAndLine() {
		Iterator<Label> itLabelX = xGridLabel.iterator();
		while (itLabelX.hasNext()) {
			coordinateSystem.getChildren().remove(itLabelX.next());
		}
		Iterator<Label> itLabelY = yGridLabel.iterator();
		while (itLabelY.hasNext()) {
			coordinateSystem.getChildren().remove(itLabelY.next());
		}
		
		Iterator<Line> itLineX = xGrid.iterator();
		while (itLineX.hasNext()) {
			coordinateSystem.getChildren().remove(itLineX.next());
		}
		Iterator<Line> itLineY = yGrid.iterator();
		while (itLineY.hasNext()) {
			coordinateSystem.getChildren().remove(itLineY.next());
		}
	}
	
	public void removeGraphs() {
		for (int i = 0; i < graphs.size(); i++) {
			coordinateSystem.getChildren().remove(graphs.get(i));
	  }
	  graphs.clear();
	}
	
	public void generateFrame() {
		removeGraphs();
		removeLabelAndLine();
		generateGrid(coordinateSystem);
		  for (int i = 0; i < graphsExp.length; i++) {
			  try {
				Color c;  
			  	if (i > 4) {
			  		Random r = new Random();
			  		c = Color.rgb(r.nextInt(250)+6, r.nextInt(250)+6, r.nextInt(250)+6);
			  	} else {
			  		c = GRAPHCOLORS[i];
			  	}
				BinaryTree.root = new Node(null);
				parser = new Parser(graphsExp[i]);
				generateGraph(c);
			  } catch (Exception e) {
			  }  
		  }
		  displayGraphs();
	}
	
	public void displayGraphs() {
		for (int i = 0; i < graphs.size(); i++) {
			coordinateSystem.getChildren().add(graphs.get(i));
		}
	}
}
