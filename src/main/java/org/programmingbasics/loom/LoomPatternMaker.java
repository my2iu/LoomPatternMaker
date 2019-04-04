package org.programmingbasics.loom;

import java.util.ArrayList;
import java.util.List;

import org.programmingbasics.loom.PatternData.PatternRow;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GwtIncompatible;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.html.CanvasElement;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import jsinterop.annotations.JsType;

@JsType
public class LoomPatternMaker
{
  PatternData data;
  PatternCanvas canvas;

  @GwtIncompatible
  public void go()
  {
    data = new PatternData();
    canvas = new PatternCanvas((CanvasElement)Browser.getDocument().querySelector("canvas#main"), data);
    canvas.draw();
    
    // Hook for resizing
    Browser.getWindow().addEventListener(Event.RESIZE, (e) -> {
      handleResize();
    }, false);
  }
  
  public void handleResize()
  {
    canvas.adjustResolution();
    canvas.draw();
  }
  
  // Returns data from the loom pattern
  public JavaScriptObject getDataJson()
  {
    JsonArray arr = Json.createArray();
    for (PatternRow row: data.rows)
    {
      JsonObject rowJson = Json.createObject();
      arr.set(arr.length(), rowJson);
      rowJson.put("color", row.color.name());
      String rowData = "";
      for (boolean bit: row.data)
        rowData += (bit ? "1" : "0");
      rowJson.put("data", rowData);
    }
    return (JavaScriptObject)arr;
  }
  
  // Sets data for the loom pattern
  public void setDataJson(JsonArray arr)
  {
    int height = arr.length();
    data.height = height;
    List<PatternRow> rows = new ArrayList<>();
    for (int n = 0; n < arr.length(); n++)
    {
      JsonObject rowJson = arr.getObject(n);
      String rowData = rowJson.getString("data");
      PatternRow row = new PatternRow(rowData.length());
      row.color = RowColor.valueOf(rowJson.getString("color"));
      for (int i = 0; i < rowData.length(); i++)
        row.data[i] = (rowData.charAt(i) == '1');
      rows.add(row);
    }
    data.rows = rows.toArray(new PatternRow[0]);
    data.width = data.rows[0].data.length;

    // Redraw the canvas
    handleResize();
  }
  
  public static LoomPatternMaker createUi(CanvasElement canvasEl)
  {
    LoomPatternMaker loom = new LoomPatternMaker();
    loom.data = new PatternData();
    loom.canvas = new PatternCanvas(canvasEl, loom.data);
    loom.canvas.draw();
    return loom;
  }
}
