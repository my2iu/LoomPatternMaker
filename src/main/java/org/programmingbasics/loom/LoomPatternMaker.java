package org.programmingbasics.loom;

import com.user00.domjnate.api.Window;
import com.user00.domjnate.api.html.HTMLCanvasElement;
import com.user00.domjnate.util.Js;

public class LoomPatternMaker
{
  PatternData data;
  PatternCanvas canvas;
  Window win;
  
  public LoomPatternMaker(Window win)
  {
     this.win = win;
  }
  
  public void go()
  {
    data = new PatternData();
    canvas = new PatternCanvas(win, Js.cast(win.getDocument().querySelector("canvas#main"), HTMLCanvasElement.class), data);
    canvas.draw();
    
    // Hook for resizing
    win.addEventListener("resize", (e) -> {
      canvas.adjustResolution();
      canvas.draw();
    }, false);
  }
}
