import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import fr.dgac.ivy.*; 
import javax.swing.JOptionPane; 
import java.io.*; 
import java.util.Stack; 
import java.util.StringTokenizer; 
import java.io.Serializable; 
import java.util.Stack; 

import fr.dgac.ivy.*; 
import fr.dgac.ivy.tools.*; 
import gnu.getopt.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OneDollarIvy extends PApplet {

/*
 *  OneDollarIvy -> Demonstration with ivy middleware
 * v. 1.3
 *   can import/export templates
 * 
 * (c) Ph. Truillet, October 2020
 * Last Revision: 22/10/2020
 * 
 * $1 Dollar Recognizer - http://depts.washington.edu/aimgroup/proj/dollar/
*/





// Attributes
Ivy bus;
FSM mae;

int ORANGE = color(255,127,0);

float Infinity = 1e9f;

// Recognizer class constants
int NumTemplates = 16;
int NumPoints = 64;
float SquareSize = 250.0f;
float HalfDiagonal = 0.5f * sqrt(250.0f * 250.0f + 250.0f * 250.0f);
float AngleRange = 45.0f;
float AnglePrecision = 2.0f;
float Phi = 0.5f * (-1.0f + sqrt(5.0f)); // Golden Ratio

Recognizer recognizer;
Recorder recorder;
Result result;
String s_result;

PFont font;
PImage sketch_icon;

public void setup() {
  
  surface.setTitle("Gesture Recognizer");
  surface.setLocation(50,50);
  sketch_icon = loadImage("onedollar.jpg");
  surface.setIcon(sketch_icon);
  
  result = null;
  s_result = "";
  
  recognizer = new Recognizer();
  recorder = new Recorder();
  
  
  font = loadFont("TwCenMT-Regular-16.vlw");
  textFont(font);
  
  // === START WITH NO TEMPLATES ===
  try {
    bus = new Ivy("OneDollarIvy", " OneDollarIvy is ready", null);
    bus.start("127.255.255.255:2010");
  }
  catch (IvyException ie) {}
    mae = FSM.INITIAL;
}

public void draw() {
  // MAE à ajouter
  switch (mae) {
    case INITIAL:
      background(255);
      fill(0);
      text("Press (R) for gesture recognition\n\n(L) to learn gesture\n(E) to export templates\n(I) to import templates\n(T) to display Templates name\n(H) for help",50,50);  
      break;
      
    case RECOGNITION:
      background(255);
      textAlign(LEFT);
      fill(0);
      text(s_result, 10, 10);
            
      recorder.update();
      recorder.draw();
      
      if (recorder.hasPoints) {
        Point[] points = recorder.points;
        result = recognizer.Recognize(points);
        recorder.hasPoints = false;
      }

      if( result != null) {
        s_result = "Template: "+ result.Name + "\nScore: " + String.format("%.2f",result.Score);
        
        try {
          bus.sendMsg("OneDolarIvy Template=" + result.Name + " Confidence=" + String.format("%.2f",result.Score));
        }
        catch (IvyException ie) {}
        result=null;
       }
      break;
      
    case LEARNING: // have to register points first
      background(ORANGE);
      recorder.update();
      recorder.draw();
      
      if (recorder.hasPoints) {
        Point[] points = recorder.points;
        String template = JOptionPane.showInputDialog("Give a name to your template: ");
        recognizer.AddTemplate(template, points);
        recorder.hasPoints = false;
        mae = FSM.RECOGNITION;
      }
      break;
      
    case IMPORT:
      recognizer.Import();
      mae = FSM.RECOGNITION;
      break;
      
    case EXPORT: 
      recognizer.Export();
      mae = FSM.RECOGNITION;
      break;
      
    case TEMPLATES:
      String[] tn = recognizer.getTemplatesName();
      background(255);
      fill(0);
      text("REGISTERED TEMPLATES", 20,10);
      for (int i=0;i<tn.length;i++)
        text(tn[i], 50, i*20+30);
      break;
      
    case HELP:
      background(255);
      fill(0);
      text("Press (R) for gesture recognition\n\n(L) to learn gesture\n(E) to export templates\n(I) to import templates\n(T) to display Templates name\n(H) for help",50,50);  
      break;
      
    default:
      break;
  }
}

public void keyPressed() {
  switch (key) {
    case 'r': // RECOGNITION -  default state
    case 'R':
      mae = FSM.RECOGNITION;
      break;
      
    case 'l': // LEARNING new template
    case 'L':
      mae = FSM.LEARNING;
      break;
      
    case 'e': // EXPORT templates
    case 'E':
      mae = FSM.EXPORT;
      break;
      
      case 'h': // HELP
      case 'H':
      mae = FSM.HELP;
      break;
           
    case 'i': // IMPORT templates
    case 'I':    
      mae = FSM.IMPORT;
      break; 
      
    case 't': // display TEMPLATES
    case 'T':    
      mae = FSM.TEMPLATES;
      break;  
    
    case ' ': // SPACEBAR
      switch (mae) {
        case TEMPLATES:
          mae = FSM.RECOGNITION;
          break;

        case HELP:
          mae = FSM.RECOGNITION;
          break;  
          
        default:
          break;
      }
      break;
  }
}
/*
 * Enumération de a Machine à Etats (Finite State Machine)
 *
 */
 
public enum FSM {
  INITIAL, /* Etat Initial */ 
  RECOGNITION, /* mode de reconnaissance */ 
  LEARNING, /* mode apprentissage */
  EXPORT, /* export des templates */
  IMPORT, /* import des templates */ 
  TEMPLATES, /* affichage des templates */ 
  HELP
}
/*
 * Définition d'un Point - et méthodes associées
 */ 
 
class Point {
  float X;
  float Y;
  
  Point(float x, float y) {
    X = x;
    Y = y;
  }

  public float distance(Point other) {
    return dist(X, Y, other.X, other.Y);
  }
}
/*
 * Recognizer - définition des templates
 */

 
 
  


class Recognizer {
  float Infinity = 1e9f; 
  Template [] Templates = {};
  
   Recognizer() {
     // nothing to do
   }

  /* ===================================================== */
  public Result Recognize(Point [] points) {
    points = Resample(points, NumPoints);
    points = RotateToZero(points);
    points = ScaleToSquare(points, SquareSize);
    points = TranslateToOrigin(points);
    float best = Infinity;
    float sndBest = Infinity;
    int t = -1;
    for( int i = 0; i < Templates.length; i++) {
      float d = DistanceAtBestAngle(points, Templates[i], -AngleRange, AngleRange, AnglePrecision);
      if( d < best ) {
        sndBest = best;
        best = d;
        t = i;
      }
      else if( d < sndBest) {
        sndBest = d;
      }
    }
    float score = 1.0f - (best / HalfDiagonal);
    float otherScore = 1.0f - (sndBest / HalfDiagonal);
    float ratio = otherScore / score;
    // The threshold of 0.7 is arbitrary, and not part of the original code.
    if( t > -1 && score > 0.7f) {
      return new Result( Templates[t].Name, score, ratio );
    }
    else {
      return new Result( "NONE", 0.0f, 1.0f);
    }
  }

  public int AddTemplate( String name, Point [] points) {
    Templates = (Template []) append( Templates, new Template(name, points));
    int num = 0;
    for( int i = 0; i < Templates.length; i++) {
      if( Templates[ i ].Name == name) {
        num++;
      }
    }
    return num;
  }

  public void DeleteUserTemplates( ) {
    Templates = (Template [])subset(Templates, 0, NumTemplates);
  }

  /* ---------------------------------- */
  public Point [] Resample( Point [] points, int n) {
    float I = PathLength( points ) / ( (float)n -1.0f );
    float D = 0.0f;
    Point [] newpoints = {};
    Stack stack = new Stack();
    for( int i = 0; i < points.length; i++) {
      stack.push( points[ points.length -1 - i]);
    }
    
   while( !stack.empty()) {
     Point pt1 = (Point) stack.pop();
     if( stack.empty()) {
       newpoints = (Point [])append( newpoints, pt1);
       continue;
     }
     Point pt2 = (Point) stack.peek();
     float d = pt1.distance( pt2);
     if( (D + d) >= I) {
       float qx = pt1.X + (( I - D ) / d ) * (pt2.X - pt1.X);
       float qy = pt1.Y + (( I - D ) / d ) * (pt2.Y - pt1.Y);
       Point q = new Point( qx, qy);
       newpoints = (Point [])append( newpoints, q);
       stack.push( q );
       D = 0.0f;
     } 
     else {
       D += d;
     }
   }
   if (newpoints.length == (n -1)) {
     newpoints = (Point [])append(newpoints, points[points.length -1]);
   }
   return newpoints;
  }
 
 
  public Point [] RotateToZero(Point [] points) {
    Point c = Centroid(points);
    float theta = atan2( c.Y - points[0].Y, c.X - points[0].X);
    return RotateBy( points, -theta);
  }

  public Point [] ScaleToSquare( Point [] points, float sz) {
    Rectangle B = BoundingBox( points );
    Point [] newpoints = {};
    for( int i = 0; i < points.length; i++) {
      float qx = points[i].X * (sz / B.Width);
      float qy = points[i].Y * (sz / B.Height);
      newpoints = (Point [])append( newpoints,  new Point(qx, qy));
    }
    return newpoints;
  }

 public Point [] TranslateToOrigin(Point [] points) {
   Point c = Centroid(points);
   Point [] newpoints = {};
   for( int i = -0; i < points.length; i++) {
     float qx = points[i].X - c.X;
     float qy = points[i].Y - c.Y;
     newpoints = (Point [])append(newpoints,  new Point(qx, qy));
   }
   return newpoints;
  }
  
  public Point [] RotateBy( Point [] points, float theta) {
    Point c = Centroid( points );
    float Cos = cos( theta );
    float Sin = sin( theta );
    Point [] newpoints = {};
    for( int i = 0; i < points.length; i++) {
      float qx = (points[i].X - c.X) * Cos - (points[i].Y - c.Y) * Sin + c.X;
      float qy = (points[i].X - c.X) * Sin + (points[i].Y - c.Y) * Cos + c.Y;
      newpoints = (Point[]) append(newpoints, new Point( qx, qy ));
   }
   return newpoints;
  }

  public float DistanceAtAngle( Point [] points, Template T, float theta) {
    Point [] newpoints = RotateBy( points, theta);
    return PathDistance( newpoints, T.Points);
  }  
  
  public float DistanceAtBestAngle( Point [] points, Template T, float a, float b, float threshold) {
   float x1 = Phi * a + (1.0f - Phi) * b;
   float f1 = DistanceAtAngle(points, T, x1);
   float x2 = (1.0f - Phi) * a + Phi * b;
   float f2 = DistanceAtAngle(points, T, x2);
   while( abs( b - a ) > threshold) {
     if( f1 < f2 ) {
       b = x2;
       x2 = x1;
       f2 = f1;
       x1 = Phi * a + (1.0f - Phi) * b;
       f1 = DistanceAtAngle(points, T, x1);
     }
     else {
       a = x1;
       x1 = x2;
       f1 = f2;
       x2 = (1.0f - Phi) * a + Phi * b;
       f2 = DistanceAtAngle(points, T, x2);
     }
   }
   return min(f1, f2);
  }
  
   
  public float PathLength( Point [] points) {
    float d = 0.0f;
    for( int i = 1; i < points.length; i++) {
      d += points[i-1].distance( points[i]);
    }
    return d;
  }


  public float PathDistance( Point [] pts1, Point [] pts2) {
    if( pts1.length != pts2.length) {
      // println( "Lengths differ. " + pts1.length + " != " + pts2.length);
      return Infinity;
    }
    float d = 0.0f;
    for( int i = 0; i < pts1.length; i++) {
      d += pts1[i].distance( pts2[i]);
    }
    return d / (float)pts1.length;
  }


  public Rectangle BoundingBox( Point [] points) {
    float minX = Infinity;
    float maxX = -Infinity;
    float minY = Infinity;
    float maxY = -Infinity;

    for( int i = 1; i < points.length; i++) {
      minX = min( points[i].X, minX);
      maxX = max( points[i].X, maxX);
      minY = min( points[i].Y, minY);
      maxY = max( points[i].Y, maxY);
    }
    return new Rectangle( minX, minY, maxX - minX, maxY - minY);
  }


  public Point Centroid( Point [] points) {
    Point centriod = new Point(0.0f, 0.0f);
    for( int i = 1; i < points.length; i++) {
      centriod.X += points[i].X;
      centriod.Y += points[i].Y;
    }
    centriod.X /= points.length;
    centriod.Y /= points.length;
    return centriod;
  }  
  
  public void Export() { // Export templates
    FileOutputStream fos;    
    // Pour chaque template, sauver le nom et l'ensemble des points dans un fichier .dat
    // détruire les fichiers auparavant
    for (int i=0;i < Templates.length; i++) {
      try {
         fos = new FileOutputStream(dataPath("") + "/templates/" + Templates[i].Name + ".dat");
         DataOutputStream dos = new DataOutputStream(fos);
         for (int j=0;j<Templates[i].Points.length;j++) {
           dos.writeFloat(Templates[i].Points[j].X);
           dos.writeFloat(Templates[i].Points[j].Y);
         }
         fos.close();
      }
      catch (Exception e) {}
    }
  }
  
  public void Import() { // import templates
    // importer les templates
    // détruire ceux préalblement existants
    Templates = new Template[0];
    
    FileInputStream fis;
    // lister les .template dans le répertoire /templates 
    // charger les fichiers et les sauver comme template   
    try {
      File dir=new File(dataPath("") + "/templates");
      File[] liste=dir.listFiles();
      for (File item:liste) {
        if (item.isFile()) {
          StringTokenizer st = new StringTokenizer(item.getName(),".");
          String filename = st.nextToken();
          // println(">> Nom : " + filename);          
          fis = new FileInputStream(dataPath("") + "/templates/" + item.getName()); 
          DataInputStream dis = new DataInputStream(fis);
          Point[] points = new Point[0];
          // .dat -> 64 Points
          float x,y;
          for (int i=0;i<64;i++) {
            x = dis.readFloat();
            y = dis.readFloat();
            points = (Point[])append(points, new Point(x, y));
          }
          fis.close();
          this.AddTemplate(filename, points);
        }   
      } 
    }
    catch (Exception e) {
      // println(">> ERROR <<");  
    }
  }
  
  public String[] getTemplatesName() {
    String[] names = {}; 
    for (int i=0;i<Templates.length;i++)
       names = (String[])(append(names, Templates[i].Name)); 
    return (names);
  }
}
/*
 * Simple class to record Points
 *
 */
 


class Recorder implements Serializable {
  Point [] points;
  boolean recording;
  boolean hasPoints;

  Recorder() {
     points = new Point[0];
     recording = false;
  }

  public void update() {
    if (recording) {
      if (mousePressed) {
        points = (Point[])append(points, new Point(mouseX, mouseY));
      }
      else {
        recording = false;
        if( points.length > 5) {
          hasPoints = true;
        }
      }
    }
    else {
      if(mousePressed) {
        points = new Point[0];
        recording = true;
        hasPoints = false;
      }
    }
  }

  public void draw( ) {
     int c = color(0,0,0); // Dark
     if(recording) {
       c = color(7, 128, 237); // Blue
     }
     if(points.length > 1) {
       for( int i = 1; i < points.length; i++) {
         stroke(c);
         line( points[i-1].X, points[i-1].Y,
               points[i].X, points[i  ].Y);
       }
     }
  }
}
/*
 * Rectangle to store the bounding box
 *
 */
class Rectangle {
  float X;
  float Y;
  float Width;
  float Height;
  
  Rectangle(float x, float y, float width, float height) {
    X = x;
    Y = y;
    Width = width;
    Height = height;
  }
}
/*
 * Simple class to store results
 *
 */
class Result {
  String Name;
  float Score;
  float Ratio;
  
  Result( String name, float score, float ratio) {
    Name = name;
    Score = score;
    Ratio = ratio;
  }
}
/* A template holds a name and a set of reduced points that represent
 * a single gesture.
 */
 
 

class Template {
  String Name;
  Point [] Points;
  float Infinity = 1e9f; 
  
  Template( String name, Point [] points) {
    Name = name;
    Points = Resample( points, NumPoints);
    Points = RotateToZero( Points );
    Points = ScaleToSquare( Points, SquareSize);
    Points = TranslateToOrigin( Points );
  }
  
  public Point [] Resample( Point [] points, int n) {
   float I = PathLength( points ) / ( (float)n -1.0f );
   float D = 0.0f;
   Point [] newpoints = {};
   Stack stack = new Stack();
   for( int i = 0; i < points.length; i++) {
     stack.push( points[ points.length -1 - i]);
   }

   while( !stack.empty()) {
       Point pt1 = (Point) stack.pop();

       if( stack.empty()) {
         newpoints = (Point [])append( newpoints, pt1);
         continue;
       }
       Point pt2 = (Point) stack.peek();
       float d = pt1.distance( pt2);
       if( (D + d) >= I) {
          float qx = pt1.X + (( I - D ) / d ) * (pt2.X - pt1.X);
          float qy = pt1.Y + (( I - D ) / d ) * (pt2.Y - pt1.Y);
          Point q = new Point( qx, qy);
          newpoints = (Point [])append( newpoints, q);
          stack.push( q );
          D = 0.0f;
       }
       else {
         D += d;
       }
   }

   if(newpoints.length == (n -1)) {
     newpoints = (Point [])append( newpoints, points[ points.length -1 ]);
   }
   return newpoints;
  }
  
  public float PathLength( Point [] points) {
    float d = 0.0f;
    for( int i = 1; i < points.length; i++) {
      d += points[i-1].distance( points[i]);
    }
    return d;
  }
  
  public Point [] RotateToZero(Point [] points) {
   Point c = Centroid(points);
   float theta = atan2( c.Y - points[0].Y, c.X - points[0].X);
   return RotateBy( points, -theta);
  }
  
  public Point [] RotateBy( Point [] points, float theta) {
   Point c = Centroid( points );
   float Cos = cos( theta );
   float Sin = sin( theta );

   Point [] newpoints = {};
   for( int i = 0; i < points.length; i++) {
     float qx = (points[i].X - c.X) * Cos - (points[i].Y - c.Y) * Sin + c.X;
     float qy = (points[i].X - c.X) * Sin + (points[i].Y - c.Y) * Cos + c.Y;
     newpoints = (Point[]) append(newpoints, new Point( qx, qy ));
   }
   return newpoints;
  }
  
  public Point Centroid( Point [] points) {
    Point centriod = new Point(0.0f, 0.0f);
    for( int i = 1; i < points.length; i++) {
      centriod.X += points[i].X;
      centriod.Y += points[i].Y;
    }
    centriod.X /= points.length;
    centriod.Y /= points.length;
    return centriod;
  }
  
  public Point [] ScaleToSquare( Point [] points, float sz) {
    Rectangle B = BoundingBox( points );
    Point [] newpoints = {};
    for( int i = 0; i < points.length; i++) {
       float qx = points[i].X * (sz / B.Width);
       float qy = points[i].Y * (sz / B.Height);
       newpoints = (Point [])append( newpoints,  new Point(qx, qy));
    }
    return newpoints;
  }
  
  public Rectangle BoundingBox( Point [] points) {
    float minX = Infinity;
    float maxX = -Infinity;
    float minY = Infinity;
    float maxY = -Infinity;

    for( int i = 1; i < points.length; i++) {
      minX = min( points[i].X, minX);
      maxX = max( points[i].X, maxX);
      minY = min( points[i].Y, minY);
      maxY = max( points[i].Y, maxY);
    }
    return new Rectangle( minX, minY, maxX - minX, maxY - minY);
  }
  
  public Point [] TranslateToOrigin( Point [] points) {
    Point c = Centroid( points);
    Point [] newpoints = {};
    for( int i = -0; i < points.length; i++) {
      float qx = points[i].X - c.X;
      float qy = points[i].Y - c.Y;
      newpoints = (Point [])append( newpoints,  new Point(qx, qy));
     }
   return newpoints;
  }  
}
  public void settings() {  size(400, 250);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "OneDollarIvy" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
