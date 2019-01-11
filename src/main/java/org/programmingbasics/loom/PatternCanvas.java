package org.programmingbasics.loom;


import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

public class PatternCanvas
{
  CanvasElement canvas;
  CanvasRenderingContext2D ctx;
  
  PatternData data;
  
  // Width and height of a stitch in pixels
  int stitchWidth = 22;
  int stitchHeight = 22;
  
  public PatternCanvas(CanvasElement canvas, PatternData data)
  {
    this.canvas = canvas;
    this.data = data;
    ctx = (CanvasRenderingContext2D)canvas.getContext("2d");
  }
  
  public void draw()
  {
    ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    // Figure out sizing of stuff
    int pillHeight = stitchHeight / 3;
    int pillStraightWidth = stitchWidth - 2 * pillHeight;
    int colorZoneX = (data.width + 1) * stitchWidth;
    float colorBoxScale = 0.8f;
    
    // Start drawing
    ctx.save();
    ctx.setStrokeStyle("1px black");
    ctx.setFillStyle("#ccc");
    for (int row = 0; row < data.height; row++)
    {
      PatternRow patternRow = data.rows[row];
      int centerY = row * stitchHeight + stitchHeight / 2;
      
      // Draw a row of the pattern 
      for (int col = 0; col < data.width; col++)
      {
        int centerX = col * stitchWidth + stitchWidth / 2;
        if ((row % 2) != 0)
          centerX += stitchWidth / 2;

        ctx.beginPath();
        ctx.moveTo(centerX - pillStraightWidth / 2, centerY - pillHeight / 2);
        ctx.lineTo(centerX + pillStraightWidth / 2, centerY - pillHeight / 2);
        ctx.arc(centerX + pillStraightWidth / 2, centerY, pillHeight / 2, (float)(- Math.PI / 2), (float)(Math.PI / 2), false);
        ctx.lineTo(centerX - pillStraightWidth / 2, centerY + pillHeight / 2);
        ctx.arc(centerX - pillStraightWidth / 2, centerY, pillHeight / 2, (float)(Math.PI / 2), (float)(-Math.PI / 2), false);
        ctx.closePath();
        if (patternRow.data[col])
          ctx.fill();
        ctx.stroke();
      }
      
      // Draw a color box on the end
      ctx.beginPath();
      ctx.rect(colorZoneX, centerY - stitchHeight / 2 * colorBoxScale, stitchWidth * colorBoxScale, stitchHeight * colorBoxScale);
      ctx.stroke();
    }
    ctx.restore();
  }
}
