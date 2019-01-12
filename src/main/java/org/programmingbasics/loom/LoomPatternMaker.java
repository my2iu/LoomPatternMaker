package org.programmingbasics.loom;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.html.CanvasElement;

public class LoomPatternMaker
{
  PatternData data;
  PatternCanvas canvas;
  
  public void go()
  {
    data = new PatternData();
    canvas = new PatternCanvas((CanvasElement)Browser.getDocument().querySelector("canvas#main"), data);
    canvas.draw();
    
    // Hook for resizing
    Browser.getWindow().addEventListener(Event.RESIZE, (e) -> {
      canvas.adjustResolution();
      canvas.draw();
    }, false);
  }
}
