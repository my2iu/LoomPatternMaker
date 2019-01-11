package org.programmingbasics.loom;

import com.google.gwt.core.client.EntryPoint;

import elemental.client.Browser;
import elemental.html.CanvasElement;

public class LoomPatternMakerEntry implements EntryPoint
{
  public void onModuleLoad()
  {
    new LoomPatternMaker().go();
  }
}
