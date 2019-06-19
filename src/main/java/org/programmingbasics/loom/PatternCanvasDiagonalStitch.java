package org.programmingbasics.loom;

import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.client.Browser;
import elemental.html.CanvasElement;

public class PatternCanvasDiagonalStitch extends PatternCanvas
{
  // Width and height of a stitch in pixels
  // (Do not assign values to these variables here because the parent constructor will
  // call adjustResolution() to set them, and those values will be overwritten by the child
  // if you assign values here in the child).
  int stitchXSpacing;
  int stitchYSpacing;
  int stitchWidth;
  int stitchHeight;

  static final double STITCH_X_OVERLAP = 0.25;
  static final double STITCH_Y_OVERLAP = 0.2;
  static final double STITCH_SIZE_RATIO = 500.0 / 270.0; 
//  int pillHeight = stitchHeight / 3;
//  int pillStraightWidth = stitchWidth - 2 * pillHeight;
  int colorZoneX;
//  float colorBoxScale = 0.8f;

  public PatternCanvasDiagonalStitch(CanvasElement canvas, PatternData data)
  {
    super(canvas, data);
  }

  // Adjusts the resolution of the canvas so that we get 1:1 pixels and matches the size of the canvas
  @Override public void adjustResolution()
  {
    super.adjustResolution();
    
    // Alter the sizing of everything to fill the canvas
    stitchXSpacing = (int)((canvas.getWidth() - 2 * margin) / (data.width + STITCH_X_OVERLAP));
    stitchYSpacing = (int)((canvas.getHeight() - 2 * margin) / data.height + STITCH_Y_OVERLAP);
    stitchWidth = (int)(stitchXSpacing / (1 - STITCH_X_OVERLAP));
    stitchHeight = (int)(stitchYSpacing / (1 - STITCH_Y_OVERLAP));
    stitchXSpacing = (int)Math.min(stitchXSpacing, stitchHeight * STITCH_SIZE_RATIO * (1 - STITCH_X_OVERLAP));
    stitchWidth = (int)(stitchXSpacing / (1 - STITCH_X_OVERLAP));
    stitchHeight = (int)(stitchWidth / STITCH_SIZE_RATIO);
    stitchYSpacing = (int)(stitchHeight * (1 - STITCH_Y_OVERLAP));
//    pillHeight = stitchHeight / 3;
//    pillStraightWidth = stitchWidth - 2 * pillHeight;
    colorZoneX = (data.width + 1) * stitchXSpacing;
    
  }

  
  protected int findPatternRow(int mouseX, int mouseY)
  {
    mouseY -= margin;
    double row = (double)mouseY / stitchYSpacing;
    if (row > data.height && mouseY - ((data.height - 1) * stitchYSpacing) < stitchHeight)
      return data.height - 1;
    return (int)row;
  }
  
  protected int findPatternCol(int mouseX, int mouseY)
  {
    mouseX -= margin;
//    int row = findPatternRow(mouseX, mouseY);
//    if ((row % 2) != 0) mouseX -= stitchXSpacing / 2;
    int col = (int)((mouseX - (double)stitchWidth * STITCH_X_OVERLAP) / stitchXSpacing);
    return col < 0 ? 0 : col;
  }

  
  public void draw()
  {
    ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    
    // Start drawing
    ctx.save();
    ctx.setLineWidth((float)mouseToCanvasRescale);
    ctx.setStrokeStyle("black");
    DrawOrder order = new DrawOrder(data.height, data.width);
    while (order.hasNext())
    {
      order.next();
      int row = order.row;
      int col = order.col;
      PatternRow patternRow = data.rows[row];
      int y = (int)(row * stitchYSpacing) + margin;
      int x = (int)(col * stitchXSpacing) + margin;
//        if ((row % 2) != 0)
//          centerX += stitchWidth / 2;

//        ctx.beginPath();
//        ctx.moveTo(centerX - pillStraightWidth / 2, centerY - pillHeight / 2);
//        ctx.lineTo(centerX + pillStraightWidth / 2, centerY - pillHeight / 2);
//        ctx.arc(centerX + pillStraightWidth / 2, centerY, pillHeight / 2, (float)(- Math.PI / 2), (float)(Math.PI / 2), false);
//        ctx.lineTo(centerX - pillStraightWidth / 2, centerY + pillHeight / 2);
//        ctx.arc(centerX - pillStraightWidth / 2, centerY, pillHeight / 2, (float)(Math.PI / 2), (float)(-Math.PI / 2), false);
//        ctx.closePath();
//        if (patternRow.data[col])
//          ctx.fill();
//        ctx.stroke();

      drawStitch(x, y);
      if (patternRow.data[col])
        ctx.setFillStyle(data.fgndColor);
      else
         ctx.setFillStyle(data.bgndColor);
      ctx.fill();
      ctx.stroke();
    }
//    for (int row = 0; row < data.height; row++)
//    {
//      int centerY = row * stitchYSpacing + stitchYSpacing / 2;
//      PatternRow patternRow = data.rows[row];
//      // Draw a color box on the end
//      ctx.beginPath();
//      ctx.rect(colorZoneX, (int)(centerY - stitchYSpacing / 2 * colorBoxScale), (int)(stitchXSpacing * colorBoxScale), (int)(stitchYSpacing * colorBoxScale));
//      ctx.setFillStyle(patternRow.color.cssColor);
//      ctx.fill();
//      ctx.stroke();
//    }
    ctx.restore();
  }

  private void drawStitch(float x, float y)
  {
     ctx.save();
     try {
        ctx.translate(x, y);
        ctx.scale((float)(stitchWidth / 500.0), (float)(stitchHeight / 270.0));
        ctx.beginPath();
        ctx.moveTo(0, 50);
        ctx.arc(50, 50, 50, (float)(- Math.PI), (float)(- Math.PI / 2), false);
        ctx.lineTo(330-50, 0);
        ctx.arc(330-50, 50, 50, (float)(-Math.PI / 2), 0, false);
        ctx.arc(330 + 50, 50, 50, (float)(Math.PI), (float)(Math.PI / 2), true);
        ctx.lineTo(500-50, 100);
        ctx.arc(500-50, 150, 50, (float)(-Math.PI / 2), 0, false);
        ctx.lineTo(500, 270 - 50);
        ctx.arc(500-50, 270 - 50, 50, 0, (float)(Math.PI / 2), false);
        ctx.lineTo(220, 270);
        ctx.arc(220, 270 - 50, 50, (float)(Math.PI / 2), (float)(Math.PI), false);
        ctx.arc(220 - 50 - 50, 270 - 50, 50, 0, (float)(- Math.PI / 2), true);
        ctx.lineTo(50, 270 - 50 - 50);
        ctx.arc(50, 270 - 50 - 50 - 50, 50, (float)(Math.PI / 2), (float)(Math.PI), false);

//        curve is 50?
        
        ctx.closePath();
     } finally {
        ctx.restore();
     }
     
  }

  
  // Draw order of the stitches is a little complicated (they're drawn
  // diagonally starting from the upper-right), so I'm putting that
  // logic in its own class
  public static class DrawOrder
  {
    int rowCount;
    int colCount;
    DrawOrder(int rowCount, int colCount)
    {
      this.rowCount = rowCount;
      this.colCount = colCount;
      reset();
    }
    
    int row;
    int col;
    void reset()
    {
      row = 0;
      col = colCount;
    }
    
    boolean hasNext()
    {
      return !(row == rowCount - 1 && col == 0);
    }
    
    void next()
    {
      row--;
      col--;
      if (row < 0 || col < 0)
      {
        row++;
        while (col < colCount - 1 && row < rowCount - 1)
        {
          row++;
          col++;
        }
      }
    }
  }

}
