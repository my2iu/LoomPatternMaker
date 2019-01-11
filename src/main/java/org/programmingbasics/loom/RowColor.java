package org.programmingbasics.loom;

public enum RowColor {
  BLACK("Black", "#000"), RED("Red", "#f00"), BLUE("Blue", "#00f"), YELLOW("Yellow", "#ff0");
  RowColor(String colorName, String cssColor)
  {
    this.colorName = colorName;
    this.cssColor = cssColor;
  }
  public String colorName;
  public String cssColor;
  public RowColor nextColor()
  {
    return RowColor.values()[(ordinal() + 1) % RowColor.values().length];
  }
}
