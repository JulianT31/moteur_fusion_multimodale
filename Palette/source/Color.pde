
public enum Couleur {
  ROUGE(236, 112, 99),
    VERT(154, 255, 40),
    BLEU(174, 214, 241),
    JAUNE(253, 255, 97),
    GRIS(160, 160, 160),
    NOIR(0, 0, 0);

  private int rouge;
  private int vert;
  private int bleu;

  Couleur(int rouge, int vert, int bleu) {
    this.rouge = rouge;
    this.vert = vert;
    this.bleu = bleu;
  }

  public int[] toRGB() {
    int[] rvb = {this.rouge, this.vert, this.bleu};
    return rvb;
  }
}
