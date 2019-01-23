package org.programmingbasics.loom;


import org.programmingbasics.loom.PatternData.PatternRow;

import com.user00.domjnate.api.CanvasRenderingContext2D;
import com.user00.domjnate.api.MouseEvent;
import com.user00.domjnate.api.Touch;
import com.user00.domjnate.api.TouchEvent;
import com.user00.domjnate.api.TouchList;
import com.user00.domjnate.api.Window;
import com.user00.domjnate.api.dom.Element;
import com.user00.domjnate.api.html.HTMLCanvasElement;
import com.user00.domjnate.api.html.HTMLElement;
import com.user00.domjnate.util.Js;

public class PatternCanvas
{
  HTMLCanvasElement canvas;
  CanvasRenderingContext2D ctx;
  Window win;
  
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
  double trackingTouchId = -1;
  
  public PatternCanvas(Window win, HTMLCanvasElement canvas, PatternData data)
  {
    this.win = win;
    this.canvas = canvas;
    this.data = data;
    adjustResolution();
    ctx = (com.user00.domjnate.api.CanvasRenderingContext2D)canvas.getContext("2d");

    // Hook events
    hookEvents();
  }

  // Adjusts the resolution of the canvas so that we get 1:1 pixels and matches the size of the canvas
  public void adjustResolution()
  {
    // Set the resolution of the canvas appropriately
    int w = (int)canvas.getClientWidth();
    int h = (int)canvas.getClientHeight();
    double pixelRatio = win.getDevicePixelRatio();
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
    canvas.addEventListener("mousedown", (e) -> {
      MouseEvent evt = Js.cast(e, MouseEvent.class);
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
    canvas.addEventListener("mousemove", (e) -> {
      MouseEvent evt = Js.cast(e, MouseEvent.class);
      evt.preventDefault();
      evt.stopPropagation();
      if (!isTrackingMouseOnPattern) return;
      int mouseX = (int)(evt.getOffsetX() * mouseToCanvasRescale);
      int mouseY = (int)(evt.getOffsetY() * mouseToCanvasRescale);
      handlePointerOnPattern(mouseX, mouseY);
    }, false);
    canvas.addEventListener("mouseup", (e) -> {
      MouseEvent evt = Js.cast(e, MouseEvent.class);
      evt.preventDefault();
      evt.stopPropagation();
      isTrackingMouseOnPattern = false;
    }, false);
    
    // Hook touch events
    canvas.addEventListener("touchstart", (e) -> {
      TouchEvent evt = Js.cast(e, TouchEvent.class);
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
    canvas.addEventListener("touchmove", (e) -> {
      TouchEvent evt = Js.cast(e, TouchEvent.class);
      if (!isTrackingTouchOnPattern) return;
      Touch touch = findTouch(evt.getTouches(), trackingTouchId);
      if (touch == null) return;
      int mouseX = (int)(pageXRelativeToEl(touch.getPageX(), canvas) * mouseToCanvasRescale);
      int mouseY = (int)(pageYRelativeToEl(touch.getPageY(), canvas) * mouseToCanvasRescale);
      handlePointerOnPattern(mouseX, mouseY);
      evt.preventDefault();
      evt.stopPropagation();
    }, false);
    canvas.addEventListener("touchend", (e) -> {
      TouchEvent evt = Js.cast(e, TouchEvent.class);
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
    canvas.addEventListener("touchcancel", (e) -> {
      TouchEvent evt = Js.cast(e, TouchEvent.class);
      evt.preventDefault();
      evt.stopPropagation();
      isTrackingTouchOnPattern = false;
    }, false);
  }
  
  private Touch findTouch(TouchList touches, double identifier)
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
  
  public static double pageXRelativeToEl(double x, HTMLElement element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      pageX -= element.getScrollLeft();
      pageY -= element.getScrollTop();
      element = Js.cast(element.getOffsetParent(), HTMLElement.class);
    }
    x = x - pageX;
    return x;
  }

  public static double pageYRelativeToEl(double y, HTMLElement element)
  {
    // Convert pageX and pageY numbers to be relative to a certain element
    int pageX = 0, pageY = 0;
    while(element.getOffsetParent() != null)
    {
      pageX += element.getOffsetLeft();
      pageY += element.getOffsetTop();
      pageX -= element.getScrollLeft();
      pageY -= element.getScrollTop();
      element = Js.cast(element.getOffsetParent(), HTMLElement.class);
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
