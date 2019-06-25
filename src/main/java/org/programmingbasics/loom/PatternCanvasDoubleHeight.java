package org.programmingbasics.loom;

import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.client.Browser;
import elemental.html.CanvasElement;

public class PatternCanvasDoubleHeight extends PatternCanvas
{
  // Width and height of a stitch in pixels
  // (Do not assign values to these variables here because the parent constructor will
  // call adjustResolution() to set them, and those values will be overwritten by the child
  // if you assign values here in the child).
  int stitchWidth;
  int stitchHeight;
  int halfStitchHeight;
  int xOffset;
  int yOffset;
  int cornerRadius;
  double stitchSpacing;
  
  public PatternCanvasDoubleHeight(CanvasElement canvas, PatternData data)
  {
    super(canvas, data);
  }

  @Override public void adjustResolution()
  {
    super.adjustResolution();

    // Alter the sizing of everything to fill the canvas
    stitchSpacing = 2 * mouseToCanvasRescale;
    stitchWidth = (int)((canvas.getWidth() - 2 * margin) / data.width);
    stitchHeight = (int)((canvas.getHeight() - 2 * margin) / data.height);
    halfStitchHeight = (int)(stitchHeight - 2 * stitchSpacing) / 2;
    stitchHeight = (int)(halfStitchHeight * 2 + 2 * stitchSpacing);
    xOffset = (canvas.getWidth() - data.width * stitchWidth) / 2;
    yOffset = (canvas.getHeight() - data.height * stitchHeight) / 2;
    cornerRadius = Math.min(halfStitchHeight / 2, stitchWidth / 2);
    
  }

  @Override
  protected int findPatternRow(int mouseX, int mouseY)
  {
    mouseY -= yOffset;
    double row = (double)mouseY / stitchHeight;
    if (row > data.height && mouseY - ((data.height - 1) * stitchHeight) < stitchHeight)
      return data.height - 1;
    return (int)row;
  }

  @Override
  protected int findPatternCol(int mouseX, int mouseY)
  {
    mouseX -= xOffset;
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
    // Shift everything by 0.5 so that lines are aligned to the center of pixels
    ctx.translate(0.5f, 0.5f);

    for (int row = 0; row < data.height; row++)
    {
      int startRun = -1;
      int runLength = 0;
      
      for (int col = 0; col < data.width; col++)
      {
        PatternRow patternRow = data.rows[row];
//        int y = (int)(row * stitchHeight) + margin;
//        int x = (int)(col * stitchWidth) + margin;

        if (patternRow.data[col])
        {
          if (startRun < 0)
          {
            startRun = col;
            runLength = 1;
          }
          else
            runLength++;
        }
        else if (startRun >= 0)
        {
          drawStitchRun(startRun, row, runLength);
          ctx.fill();
          ctx.stroke();
          startRun = -1;
        }
      }
      if (startRun >= 0)
      {
        drawStitchRun(startRun, row, runLength);
        ctx.fill();
        ctx.stroke();
      }
    }

    // Draw a grid over top the lines
    ctx.setGlobalAlpha(0.5f);
    ctx.setStrokeStyle("cyan");
    ctx.beginPath();
    for (int row = 1; row < data.height; row++)
    {
      ctx.moveTo(xOffset, (float)(row * stitchHeight + yOffset - mouseToCanvasRescale));
      ctx.lineTo(xOffset + data.width * stitchWidth, (float)(row * stitchHeight + yOffset - mouseToCanvasRescale));
    }
    for (int col = 1; col < data.width; col++)
    {
      ctx.moveTo(xOffset + col * stitchWidth, yOffset);
      ctx.lineTo(xOffset + col * stitchWidth, yOffset + data.height * stitchHeight);
    }
    ctx.stroke();
    
    
    ctx.restore();
  }
  
  private void drawStitchRun(int col, int row, int len)
  {
    int x1 = (int)(col * stitchWidth) + xOffset;
    int y1 = (int)(row * stitchHeight) + yOffset;
    
    int x2 = (int)((col + len) * stitchWidth) + xOffset;
    float y2 = (float)(y1 + halfStitchHeight + stitchSpacing);
    
//    int halfStitchHeight = (stitchHeight - 2 * STITCH_SPACING) / 2;
    
    ctx.beginPath();
    drawHalfStitch(x1, y1, x2);
    ctx.closePath();
    
    drawHalfStitch(x1, y2, x2);
    ctx.closePath();
  }

  private void drawHalfStitch(float x1, float y1, float x2)
  {
    ctx.moveTo(x1 + cornerRadius, y1);
    ctx.lineTo(x2 - cornerRadius, y1);
    ctx.arcTo(x2, y1, x2, y1 + cornerRadius, cornerRadius);
    ctx.lineTo(x2, y1 + halfStitchHeight - cornerRadius);
    ctx.arcTo(x2, y1 + halfStitchHeight, x2 - cornerRadius, y1 + halfStitchHeight, cornerRadius);
    ctx.lineTo(x1 + cornerRadius, y1 + halfStitchHeight);
    ctx.arcTo(x1, y1 + halfStitchHeight, x1, y1 + halfStitchHeight - cornerRadius, cornerRadius);
    ctx.lineTo(x1, y1 + cornerRadius);
    ctx.arcTo(x1, y1, x1 + cornerRadius, y1, cornerRadius);
  }
  
}
