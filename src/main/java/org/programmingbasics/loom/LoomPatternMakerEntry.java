package org.programmingbasics.loom;

import com.google.gwt.core.client.EntryPoint;

public class LoomPatternMakerEntry implements EntryPoint
{
  public void onModuleLoad()
  {
    notifyGwtLoaded();
  }
  
  // Tell the JavaScript code that GWT code has loaded and been initialized
  private static native void notifyGwtLoaded() /*-{
    if ($wnd.onGwtLoaded)
      $wnd.onGwtLoaded();
  }-*/;

}
