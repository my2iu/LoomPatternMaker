package org.programmingbasics.loom;

import com.google.gwt.core.client.EntryPoint;
import com.user00.domjnate.api.Window;

public class LoomPatternMakerEntry implements EntryPoint
{
  public void onModuleLoad()
  {
    new LoomPatternMaker(win()).go();
  }
  
  public static native Window win() /*-{
     return $wnd;
   }-*/;
}
