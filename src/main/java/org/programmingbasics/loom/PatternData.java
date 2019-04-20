package org.programmingbasics.loom;

public class PatternData
{
  // Width and height in stitches
  public int width;  
  public int height;

  String fgndColor = "#000";
  String bgndColor = "#fff";
  public PatternRow[] rows;
  
  public PatternData()
  {
     this(15, 15);
  }

  public PatternData(int w, int h)
  {
     width = w;
     height = h;
     rows = new PatternRow[height];
     for (int n = 0; n < height; n++)
        rows[n] = new PatternRow(width);
  }

  public static class PatternRow
  {
    public boolean [] data;
    
    public PatternRow(int width)
    {
      data = new boolean[width];
    }
  }
  
  
}
