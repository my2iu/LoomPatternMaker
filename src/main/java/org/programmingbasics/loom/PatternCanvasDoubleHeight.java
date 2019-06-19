package org.programmingbasics.loom;

import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.html.CanvasElement;

public class PatternCanvasDoubleHeight extends PatternCanvas
{
  // Width and height of a stitch in pixels
  // (Do not assign values to these variables here because the parent constructor will
  // call adjustResolution() to set them, and those values will be overwritten by the child
  // if you assign values here in the child).
  int stitchWidth;
  int stitchHeight;

  public PatternCanvasDoubleHeight(CanvasElement canvas, PatternData data)
  {
    super(canvas, data);
  }

  @Override public void adjustResolution()
  {
    super.adjustResolution();
    
    // Alter the sizing of everything to fill the canvas
    stitchWidth = (int)((canvas.getWidth() - 2 * margin) / data.width);
    stitchHeight = (int)((canvas.getHeight() - 2 * margin) / data.height);
  }

  @Override
  protected int findPatternRow(int mouseX, int mouseY)
  {
    mouseY -= margin;
    double row = (double)mouseY / stitchHeight;
    if (row > data.height && mouseY - ((data.height - 1) * stitchHeight) < stitchHeight)
      return data.height - 1;
    return (int)row;
  }

  @Override
  protected int findPatternCol(int mouseX, int mouseY)
  {
    mouseX -= margin;
    int col = (int)(mouseX / stitchWidth);
    return col < 0 ? 0 : col;
  }

  @Override
  public void draw()
  {
    ctx.save();
    ctx.setFillStyle(data.bgndColor);
    ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    ctx.restore();
    
    // Start drawing
    ctx.save();
    ctx.setLineWidth((float)mouseToCanvasRescale);
    ctx.setStrokeStyle("black");
    ctx.setFillStyle(data.fgndColor);

    for (int row = 0; row < data.height; row++)
    {
      for (int col = 0; col < data.width; col++)
      {
        PatternRow patternRow = data.rows[row];
        int y = (int)(row * stitchHeight) + margin;
        int x = (int)(col * stitchWidth) + margin;

        if (patternRow.data[col])
          drawStitchRun(col, row, 1);
//        else
//           ctx.setFillStyle(data.bgndColor);
        ctx.fill();
        ctx.stroke();
      }
    }

    
    ctx.restore();
  }
  
  private void drawStitchRun(int col, int row, int len)
  {
    int x1 = (int)(col * stitchWidth) + margin;
    int y1 = (int)(row * stitchHeight) + margin;
    
    int x2 = (int)((col + len) * stitchWidth) + margin;
    int y2 = (int)((row  + 1) * stitchHeight) + margin;
    
    int STITCH_SPACING = 3;
    int halfStitchHeight = (stitchHeight - 2 * STITCH_SPACING) / 2;
    
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y1);
    ctx.lineTo(x2, y1 + halfStitchHeight);
    ctx.lineTo(x1, y1 + halfStitchHeight);
    ctx.closePath();
    
    ctx.moveTo(x1, y1 + halfStitchHeight + STITCH_SPACING);
    ctx.lineTo(x2, y1 + halfStitchHeight + STITCH_SPACING);
    ctx.lineTo(x2, y1 + 2 * halfStitchHeight + STITCH_SPACING);
    ctx.lineTo(x1, y1 + 2 * halfStitchHeight + STITCH_SPACING);
    ctx.closePath();
  }
  
}
