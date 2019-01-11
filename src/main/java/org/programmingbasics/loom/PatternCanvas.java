package org.programmingbasics.loom;


import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

public class PatternCanvas
{
  CanvasElement canvas;
  CanvasRenderingContext2D ctx;
  
  // Width and height in stitches
  int patternWidth = 15;  
  int patternHeight = 15;
  
  // Width and height of a stitch in pixels
  int stitchWidth = 20;
  int stitchHeight = 20;
  
  public PatternCanvas(CanvasElement canvas)
  {
    this.canvas = canvas;
    ctx = (CanvasRenderingContext2D)canvas.getContext("2d");
  }
  
  public void draw()
  {
    ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    int pillHeight = stitchHeight / 3;
    int pillStraightWidth = stitchWidth - 2 * pillHeight;
    ctx.save();
    ctx.setStrokeStyle("1px black");
    ctx.setFillStyle("#ccc");
    for (int row = 0; row < patternHeight; row++)
    {
      for (int col = 0; col < patternWidth; col++)
      {
        ctx.beginPath();
        int centerX = col * stitchWidth + stitchWidth / 2;
        int centerY = row * stitchHeight + stitchHeight / 2;
        ctx.moveTo(centerX - pillStraightWidth / 2, centerY - pillHeight / 2);
        ctx.lineTo(centerX + pillStraightWidth / 2, centerY - pillHeight / 2);
        ctx.arc(centerX + pillStraightWidth / 2, centerY, pillHeight / 2, (float)(- Math.PI / 2), (float)(Math.PI / 2), false);
        ctx.lineTo(centerX - pillStraightWidth / 2, centerY + pillHeight / 2);
        ctx.arc(centerX - pillStraightWidth / 2, centerY, pillHeight / 2, (float)(Math.PI / 2), (float)(-Math.PI / 2), false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
      }
    }
    ctx.restore();
  }
}
