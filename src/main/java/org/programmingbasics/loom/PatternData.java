package org.programmingbasics.loom;

public class PatternData
{
  // Width and height in stitches
  public int width = 15;  
  public int height = 15;
  
  public PatternRow[] rows;
  
  public PatternData()
  {
    rows = new PatternRow[height];
    for (int n = 0; n < height; n++)
      rows[n] = new PatternRow(width);
  }
  
  public static class PatternRow
  {
    public boolean [] data;
    public RowColor color = RowColor.BLACK;
    
    public PatternRow(int width)
    {
      data = new boolean[width];
    }
  }
  
  
}
