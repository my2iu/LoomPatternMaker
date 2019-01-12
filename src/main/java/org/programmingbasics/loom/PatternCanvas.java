package org.programmingbasics.loom;


import org.programmingbasics.loom.PatternData.PatternRow;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.Touch;
import elemental.events.TouchEvent;
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
    ctx = (CanvasRenderingContext2D)canvas.getContext("2d");

    // Figure out sizing of stuff
    colorZoneX = (data.width + 1) * stitchWidth;

    // Hook mouse events
    canvas.addEventListener(Event.MOUSEDOWN, (e) -> {
      MouseEvent evt = (MouseEvent)e;
      evt.preventDefault();
      evt.stopPropagation();
      // Check if clicking in a color area
      if (checkForAndHandleColorPress(evt.getOffsetX(), evt.getOffsetY()))
        return;
      // Otherwise, check if the pattern is being drawn
      int row = findPatternRow(evt.getOffsetX(), evt.getOffsetY());
      int col = findPatternCol(evt.getOffsetX(), evt.getOffsetY());
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
      int row = findPatternRow(evt.getOffsetX(), evt.getOffsetY());
      int col = findPatternCol(evt.getOffsetX(), evt.getOffsetY());
      if (row >= 0 && row < data.height && col >= 0 && col < data.width)
      {
        data.rows[row].data[col] = isMouseTurnOn;
        draw();
      }
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
      int mouseX = pageXRelativeToEl(touch.getPageX(), canvas);
      int mouseY = pageYRelativeToEl(touch.getPageY(), canvas);
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
      Touch touch = null;
      for (int n = 0; n < evt.getTouches().getLength(); n++)
      {
        if (evt.getTouches().item(n).getIdentifier() == trackingTouchId)
          touch = evt.getTouches().item(n);
      }
      if (touch == null) return;
      int mouseX = pageXRelativeToEl(touch.getPageX(), canvas);
      int mouseY = pageYRelativeToEl(touch.getPageY(), canvas);
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (row >= 0 && row < data.height && col >= 0 && col < data.width)
      {
        data.rows[row].data[col] = isMouseTurnOn;
        draw();
      }
      evt.preventDefault();
      evt.stopPropagation();
    }, false);
    canvas.addEventListener(Event.TOUCHEND, (e) -> {
      TouchEvent evt = (TouchEvent)e;
      if (!isTrackingTouchOnPattern) return;
      Touch touch = null;
      for (int n = 0; n < evt.getTouches().getLength(); n++)
      {
        if (evt.getTouches().item(n).getIdentifier() == trackingTouchId)
          touch = evt.getTouches().item(n);
      }
      if (touch == null) return;
      int mouseX = pageXRelativeToEl(touch.getPageX(), canvas);
      int mouseY = pageYRelativeToEl(touch.getPageY(), canvas);
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (row >= 0 && row < data.height && col >= 0 && col < data.width)
      {
        data.rows[row].data[col] = isMouseTurnOn;
        draw();
      }
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
    for (int row = 0; row < data.height; row++)
    {
      PatternRow patternRow = data.rows[row];
      ctx.setFillStyle(patternRow.color.cssColor);
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
      ctx.rect(colorZoneX, (int)(centerY - stitchHeight / 2 * colorBoxScale), (int)(stitchWidth * colorBoxScale), (int)(stitchHeight * colorBoxScale));
      ctx.fill();
      ctx.stroke();
    }
    ctx.restore();
  }
}
