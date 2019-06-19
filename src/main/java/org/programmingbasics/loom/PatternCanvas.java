package org.programmingbasics.loom;


import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.Touch;
import elemental.events.TouchEvent;
import elemental.events.TouchList;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

public abstract class PatternCanvas
{
  CanvasElement canvas;
  CanvasRenderingContext2D ctx;
  
  PatternData data;
  
  
  // Margin running around the outside of the canvas
  int margin;


  /** If the drawing is modifiable */
  boolean readOnly = false;
  
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
    margin = (int)(1 + Math.ceil(pixelRatio));
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
//      if (checkForAndHandleColorPress(mouseX, mouseY))
//        return;
      // Otherwise, check if the pattern is being drawn
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (readOnly) return;
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
//      if (checkForAndHandleColorPress(mouseX, mouseY))
//      {
//        evt.preventDefault();
//        evt.stopPropagation();
//        return;
//      }
      // Otherwise, check if the pattern is being drawn
      int row = findPatternRow(mouseX, mouseY);
      int col = findPatternCol(mouseX, mouseY);
      if (readOnly) return;
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
  
//  /** Returns true if mouse action is for a color press */
//  private boolean checkForAndHandleColorPress(int mouseX, int mouseY)
//  {
//    if (mouseX > colorZoneX && mouseX < colorZoneX + stitchXSpacing * colorBoxScale)
//    {
//      int row = findPatternRow(mouseX, mouseY);
//      if (row >= 0 && row < data.height)
//        data.rows[row].color = data.rows[row].color.nextColor();
//      draw();
//      return true;
//    }
//    return false;
//  }
  
  protected abstract int findPatternRow(int mouseX, int mouseY);
  
  protected abstract int findPatternCol(int mouseX, int mouseY);
  
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
  
  public abstract void draw();
  
  
}
