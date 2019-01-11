package org.programmingbasics.loom;

import elemental.client.Browser;
import elemental.html.CanvasElement;

public class LoomPatternMaker
{

  public void go()
  {
    PatternCanvas canvas = 
        new PatternCanvas((CanvasElement)Browser.getDocument().querySelector("canvas#main"));
    canvas.draw();
  }
  
}
