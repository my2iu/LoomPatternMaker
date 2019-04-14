package org.programmingbasics.loom;


import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.Touch;
import elemental.events.TouchEvent;
import elemental.events.TouchList;
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

  int pillHeight = stitchHeight / 3;
  int pillStraightWidth = stitchWidth - 2 * pillHeight;
  int colorZoneX;
  float colorBoxScale = 0.8f;

  /** For remapping mouse coordinates to canvas coordinates */
  double mouseToCanvasRescale = 1.0;
  
  /** Whether the mouse button was depressed on the pattern portion of the canvas */
  boolean isTrackingMouseOnPattern = false;
  
  /** Whether the mouse/touch is turning on pattern locations or turning them off */
  boolean isMouseTurnOn = false;
  
  /** Whether a touch has been initiated on the pattern portion of the canvas, plus its id */
  boolean isTrackingTouchOnPattern = false;
  int trackingTouchId = -1;
  
  public PatternCanvas(CanvasElement canvas, PatternData data)
  {
    this.canvas = canvas;
    this.data = data;
    adjustResolution();
    ctx = (CanvasRenderingContext2D)canvas.getContext("2d");

    // Hook events
    hookEvents();
  }

  // Adjusts the resolution of the canvas so that we get 1:1 pixels and matches the size of the canvas
  public void adjustResolution()
  {
    // Set the resolution of the canvas appropriately
    int w = canvas.getClientWidth();
    int h = canvas.getClientHeight();
    double pixelRatio = Browser.getWindow().getDevicePixelRatio();
    mouseToCanvasRescale = pixelRatio;
    canvas.setWidth((int)(w * pixelRatio));
    canvas.setHeight((int)(h * pixelRatio));
    
    // Alter the sizing of everything to fill the canvas
    stitchWidth = (int)(canvas.getWidth() / (data.width + 3));
    stitchHeight = (int)(canvas.getHeight() / data.height);
    stitchWidth = Math.min(stitchWidth, stitchHeight);
    stitchHeight = stitchWidth;
    pillHeight = stitchHeight / 3;
    pillStraightWidth = stitchWidth - 2 * pillHeight;
    colorZoneX = (data.width + 1) * stitchWidth;
    
  }
  
  void hookEvents()
  {
    // Hook mouse events
    canvas.addEventListener(Event.MOUSEDOWN, (e) -> {
      MouseEvent evt = (MouseEvent)e;
      evt.preventDefault();
      evt.stopPropagation();
      int mouseX = (int)(evt.getOffsetX() * mouseToCanvasRescale);
      int mouseY = (int)(evt.getOffsetY() * mouseToCanvasRescale);
      // Check if clicking in a color area
      if (checkForAndHandleColorPress(mouseX, mouseY))
        return;
      // Otherwise, check if the pattern is being drawn
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (row >= 0 && row < data.height && col >= 0 && col < data.width)
      {
        isMouseTurnOn = !data.rows[row].data[col]; 
        data.rows[row].data[col] = isMouseTurnOn;
        isTrackingMouseOnPattern = true;
        draw();
      }
    }, false);
    canvas.addEventListener(Event.MOUSEMOVE, (e) -> {
      MouseEvent evt = (MouseEvent)e;
      evt.preventDefault();
      evt.stopPropagation();
      if (!isTrackingMouseOnPattern) return;
      int mouseX = (int)(evt.getOffsetX() * mouseToCanvasRescale);
      int mouseY = (int)(evt.getOffsetY() * mouseToCanvasRescale);
      handlePointerOnPattern(mouseX, mouseY);
    }, false);
    canvas.addEventListener(Event.MOUSEUP, (e) -> {
      MouseEvent evt = (MouseEvent)e;
      evt.preventDefault();
      evt.stopPropagation();
      isTrackingMouseOnPattern = false;
    }, false);
    
    // Hook touch events
    canvas.addEventListener(Event.TOUCHSTART, (e) -> {
      TouchEvent evt = (TouchEvent)e;
      if (evt.getChangedTouches().getLength() > 1)
        return;
      Touch touch = evt.getChangedTouches().item(0);
      int mouseX = (int)(pageXRelativeToEl(touch.getPageX(), canvas) * mouseToCanvasRescale);
      int mouseY = (int)(pageYRelativeToEl(touch.getPageY(), canvas) * mouseToCanvasRescale);
      if (checkForAndHandleColorPress(mouseX, mouseY))
      {
        evt.preventDefault();
        evt.stopPropagation();
        return;
      }
      // Otherwise, check if the pattern is being drawn
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (row >= 0 && row < data.height && col >= 0 && col < data.width)
      {
        isMouseTurnOn = !data.rows[row].data[col]; 
        data.rows[row].data[col] = isMouseTurnOn;
        // If there are multiple touches, just reset to follow the latest one
        isTrackingTouchOnPattern = true;
        trackingTouchId = touch.getIdentifier();
        draw();
        evt.preventDefault();
        evt.stopPropagation();
      }
    }, false);
    canvas.addEventListener(Event.TOUCHMOVE, (e) -> {
      TouchEvent evt = (TouchEvent)e;
      if (!isTrackingTouchOnPattern) return;
      Touch touch = findTouch(evt.getTouches(), trackingTouchId);
      if (touch == null) return;
      int mouseX = (int)(pageXRelativeToEl(touch.getPageX(), canvas) * mouseToCanvasRescale);
      int mouseY = (int)(pageYRelativeToEl(touch.getPageY(), canvas) * mouseToCanvasRescale);
      handlePointerOnPattern(mouseX, mouseY);
      evt.preventDefault();
      evt.stopPropagation();
    }, false);
    canvas.addEventListener(Event.TOUCHEND, (e) -> {
      TouchEvent evt = (TouchEvent)e;
      if (!isTrackingTouchOnPattern) return;
      Touch touch = findTouch(evt.getTouches(), trackingTouchId);
      if (touch == null) return;
      int mouseX = (int)(pageXRelativeToEl(touch.getPageX(), canvas) * mouseToCanvasRescale);
      int mouseY = (int)(pageYRelativeToEl(touch.getPageY(), canvas) * mouseToCanvasRescale);
      handlePointerOnPattern(mouseX, mouseY);
      isTrackingTouchOnPattern = false;
      evt.preventDefault();
      evt.stopPropagation();
    }, false);
    canvas.addEventListener(Event.TOUCHCANCEL, (e) -> {
      TouchEvent evt = (TouchEvent)e;
      evt.preventDefault();
      evt.stopPropagation();
      isTrackingTouchOnPattern = false;
    }, false);
  }
  
  private Touch findTouch(TouchList touches, int identifier)
  {
    for (int n = 0; n < touches.getLength(); n++)
    {
      if (touches.item(n).getIdentifier() == identifier)
        return touches.item(n);
    }
    return null;
  }

  private void handlePointerOnPattern(int mouseX, int mouseY)
  {
    int row = findPatternRow(mouseX, mouseY);
    int col = findPatternCol(mouseX, mouseY);
    if (row >= 0 && row < data.height && col >= 0 && col < data.width)
    {
      data.rows[row].data[col] = isMouseTurnOn;
      draw();
    }
  }
  
  /** Returns true if mouse action is for a color press */
  private boolean checkForAndHandleColorPress(int mouseX, int mouseY)
  {
    if (mouseX > colorZoneX && mouseX < colorZoneX + stitchWidth * colorBoxScale)
    {
      int row = findPatternRow(mouseX, mouseY);
      if (row >= 0 && row < data.height)
        data.rows[row].color = data.rows[row].color.nextColor();
      draw();
      return true;
    }
    return false;
  }
  
  private int findPatternRow(int mouseX, int mouseY)
  {
    return (int)(mouseY / stitchHeight);
  }
  
  private int findPatternCol(int mouseX, int mouseY)
  {
    int row = findPatternRow(mouseX, mouseY);
    if ((row % 2) != 0) mouseX -= stitchWidth / 2;
    return (int)(mouseX / stitchWidth);
  }
  
  public static int pageXRelativeToEl(int x, Element element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      pageX -= element.getScrollLeft();
      pageY -= element.getScrollTop();
      element = element.getOffsetParent();
    }
    x = x - pageX;
    return x;
  }

  public static int pageYRelativeToEl(int y, Element element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      pageX -= element.getScrollLeft();
      pageY -= element.getScrollTop();
      element = element.getOffsetParent();
    }
    y = y - pageY;
    return y;
  }
  
  public void draw()
  {
    ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    
    // Start drawing
    ctx.save();
    ctx.setStrokeStyle("1px black");
    DrawOrder order = new DrawOrder(data.height, data.width);
    while (order.hasNext())
    {
      order.next();
      int row = order.row;
      int col = order.col;
      PatternRow patternRow = data.rows[row];
      int centerY = row * stitchHeight + stitchHeight / 2;
      int centerX = col * stitchWidth + stitchWidth / 2;
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

      drawStitch(centerX, centerY);
      if (patternRow.data[col])
        ctx.setFillStyle(patternRow.color.cssColor);
      else
        ctx.setFillStyle("#fff");
      ctx.fill();
      ctx.stroke();
    }
    for (int row = 0; row < data.height; row++)
    {
      int centerY = row * stitchHeight + stitchHeight / 2;
      PatternRow patternRow = data.rows[row];
      // Draw a color box on the end
      ctx.beginPath();
      ctx.rect(colorZoneX, (int)(centerY - stitchHeight / 2 * colorBoxScale), (int)(stitchWidth * colorBoxScale), (int)(stitchHeight * colorBoxScale));
      ctx.setFillStyle(patternRow.color.cssColor);
      ctx.fill();
      ctx.stroke();
    }
    ctx.restore();
  }
  
  private void drawStitch(float centerX, float centerY)
  {
     ctx.save();
     try {
        ctx.translate(centerX, centerY);
        ctx.scale((float)(2 * stitchWidth / 500.0), (float)(2 * stitchWidth / 500.0));
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
