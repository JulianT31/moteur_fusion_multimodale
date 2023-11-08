
public class Data {
  String action;
  Couleur color_i;
  Shape shape;
  Point location;
  Forme forme;

  Data() {
    this.action="";
    this.color_i = null;
    this.shape = null;
    this.location = null;
  }

  void setAction(String act) {
    this.action = act;
  }

  void setColor(String c) {
    switch(c) {
    case "RED" :
      this.color_i=Couleur.ROUGE;
      break;
    case "BLUE" :
      this.color_i=Couleur.BLEU;
      break;
    case "YELLOW" :
      this.color_i=Couleur.JAUNE;
      break;
    case "GREEN" :
      this.color_i=Couleur.VERT;
      break;
    default:
      this.color_i=null;
      break;
    }
  }

  void setShape(String shape) {
    switch(shape) {
    case "CIRCLE" :
      this.shape = Shape.CIRCLE;
      break;
    case "RECTANGLE" :
      this.shape = Shape.RECTANGLE;
      break;
    case "TRIANGLE" :
      this.shape = Shape.TRIANGLE;
      break;
    case "DIAMOND" :
      this.shape = Shape.DIAMOND;
      break;
    default:
      this.shape = null;
      break;
    }
  }

  void setLocationPoint(Point pt) {
    this.location = pt;
  }

  void reset() {
    this.action="";
    this.color_i = null;
    this.shape = null;
    this.location = null;
  }

  boolean isDataCompleted() {
    return this.action != "" && this.color_i != null && this.shape != null && this.location != null;
  }

  Forme getForme() {
    if (this.isDataCompleted()) {
      switch(this.shape) {
      case CIRCLE :
        this.forme = new Cercle(this.location);
        break;
      case RECTANGLE :
        this.forme = new Rectangle(this.location);
        break;
      case TRIANGLE :
        this.forme = new Triangle(this.location);
        break;
      case DIAMOND :
        this.forme = new Losange(this.location);
        break;
      default:
        this.forme = null;
        break;
      }

      this.forme.setColor(this.color_i);
      this.forme.setLocation(this.location);
      return this.forme;
    }
    return null;
  }

  public String toString() {
    return "Forme: act=" + this.action + " color=" + this.color_i + " shape=" + this.shape +" location="+  this.location ;
  }
}
