/*
 * Palette Graphique - prélude au projet multimodal 3A SRI
 * 4 objets gérés : cercle, rectangle(carré), losange et triangle
 * (c) 05/11/2019
 * Dernière révision : 28/04/2020
 */

import java.awt.Point;
import fr.dgac.ivy.*;

ArrayList<Forme> formes; // liste de formes stockées
FSM mae; // Finite Sate Machine
int indice_forme;
PImage sketch_icon;

Data data_en_cours = new Data();

Ivy bus;

void setup() {
  int WIDTH = 800;
  int HEIGHT = 600;
  size(800, 600);
  surface.setResizable(true);
  surface.setTitle("Palette multimodale");
  surface.setLocation(displayWidth/2 - WIDTH/2, displayHeight/2- HEIGHT/2);
  sketch_icon = loadImage("Palette.jpg");
  surface.setIcon(sketch_icon);

  formes= new ArrayList(); // nous créons une liste vide
  noStroke();
  mae = FSM.INITIAL;
  indice_forme = -1;

  try
  {
    bus = new Ivy("Palette", " Palette is ready", null);
    bus.start("127.255.255.255:2010");

    bus.bindMsg("^OneDolarIvy Template=(.*) Confidence=(.*)", new IvyMessageListener()
    {
      public void receive(IvyClient client, String[] args)
      {
        System.out.println("Received forme"+ args[0]);
        if (args[0] != "NONE") {
          //System.out.println("Received forme");

          String shape_string = args[0];
          data_en_cours.setShape(shape_string);

          System.out.println(data_en_cours);
          if (data_en_cours.isDataCompleted()) {
            mae=FSM.REALISATION_ACTION;
          } else {
            mae=FSM.EN_ATTENTE_INFORMATIONS;
          }
        } else {
          System.out.println("Forme inconnue");
        }
      }
    }
    );

    bus.bindMsg("^sra5 Parsed=action=(.*) where=(.*) form=(.*) color=(.*) localisation=(.*) Confidence=(.*) NP=(.*) Num_A=(.*)", new IvyMessageListener()
    {
      public void receive(IvyClient client, String[] args)
      {
        String act = args[0];
        String shape_string = args[2];
        String color_string = args[3];
        System.out.println("Received parole");

        data_en_cours.setAction(act);
        data_en_cours.setColor(color_string); // if empty will be set to null
        data_en_cours.setShape(shape_string); // if empty will be set to null

        System.out.println(data_en_cours);        
        if (data_en_cours.action.equals("DELETE")) {
          mae = FSM.REALISATION_ACTION;
        } else if (data_en_cours.isDataCompleted()) {
          mae = FSM.REALISATION_ACTION;
        } else {
          mae = FSM.EN_ATTENTE_INFORMATIONS;
        }
      }
    }
    );

    // reset state
    //bus.bindMsg("^sra5 Event=Speech_Rejected", new IvyMessageListener()
    //{
    //  public void receive(IvyClient client, String[] args)
    //  {
    //    System.out.println("Received reset");
    //    data_en_cours.reset();
    //    System.out.println(data_en_cours);
    //    mae=FSM.IDLE;
    //  }
    //}
    //);
  }
  catch (IvyException ie) {
  }
}

void draw() {
  background(0);
  //println("MAE : " + mae );
  switch (mae) {
  case INITIAL:  // Etat INITIAL
    background(255);
    fill(0);
    text("Projet fusion multimodale", 50, 50);
    text("Julian TRANI & Pauline JOBERT 3A", 50, 80);
    text("r : reset la trame de données", 50, 110);
    break;

  case IDLE:
  case EN_ATTENTE_INFORMATIONS:
    affiche();
    break;

  case REALISATION_ACTION:
    println("REALISATION_ACTION");
    switch(data_en_cours.action) {
    case "CREATE":
      formes.add(data_en_cours.getForme());
      data_en_cours.reset();
      break;

    case "MOVE":
      for (int i=formes.size()-1; i>=0; i--) {
        if (formes.get(i).c_color == data_en_cours.color_i && formes.get(i).shape == data_en_cours.shape) {
          formes.get(i).setLocation(data_en_cours.location);
          break;
        }
      }
      data_en_cours.reset();
      break;

    case "DELETE":
      for (int i=formes.size()-1; i>=0; i--) {
        if (formes.get(i).c_color == data_en_cours.color_i && formes.get(i).shape == data_en_cours.shape) {
          formes.remove(i);
          break;
        }
      }
      data_en_cours.reset();
      break;
    default:
      break;
    }
    mae = FSM.IDLE;
    break;

  default:
    break;
  }
}

// fonction d'affichage des formes m
void affiche() {
  background(255);
  /* afficher tous les objets */
  for (int i=0; i<formes.size(); i++) // on affiche les objets de la liste
    (formes.get(i)).update();
}

void mousePressed() { // sur l'événement clic
  Point p = new Point(mouseX, mouseY);

  data_en_cours.setLocationPoint(p);
  System.out.println(data_en_cours);
  if (data_en_cours.isDataCompleted()) {
    mae=FSM.REALISATION_ACTION;
  } else {
    mae=FSM.EN_ATTENTE_INFORMATIONS;
  }
}


void keyPressed() {
  switch(key) {
  case 'r':
    data_en_cours.reset();
    System.out.println("Reset data");
    System.out.println(data_en_cours);
    break;
  }
}
