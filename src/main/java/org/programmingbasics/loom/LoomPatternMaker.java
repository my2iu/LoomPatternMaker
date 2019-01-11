package org.programmingbasics.loom;

import elemental.client.Browser;
import elemental.html.CanvasElement;

public class LoomPatternMaker
{

  public void go()
  {
    PatternData data = new PatternData();
    PatternCanvas canvas = 
        new PatternCanvas((CanvasElement)Browser.getDocument().querySelector("canvas#main"), data);
    canvas.draw();
  }
  
}
