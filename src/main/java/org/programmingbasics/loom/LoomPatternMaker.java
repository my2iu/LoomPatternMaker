package org.programmingbasics.loom;

import java.util.ArrayList;
import java.util.List;

import org.programmingbasics.loom.PatternData.PatternRow;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GwtIncompatible;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.html.CanvasElement;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.xpath.DOMParser;
import elemental.xpath.XMLSerializer;
import elemental.xpath.XPathNSResolver;
import elemental.xpath.XPathResult;
import jsinterop.annotations.JsFunction;
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
    return getDataJson(data);
  }
  
  // Sets data for the loom pattern
  public void setDataJson(JsonObject json)
  {
    setDataJson(json, data);

    // Redraw the canvas
    handleResize();
  }
  
  public void setReadOnly(boolean readOnly)
  {
     canvas.readOnly = readOnly;
  }
  
  public void setForegroundColor(String color)
  {
     data.fgndColor = color;
     canvas.draw();
  }

  public void setBackgroundColor(String color)
  {
     data.bgndColor = color;
     canvas.draw();
  }

  public static LoomPatternMaker createUi(CanvasElement canvasEl, int width, int height)
  {
    LoomPatternMaker loom = new LoomPatternMaker();
    loom.data = new PatternData(width, height);
    loom.canvas = new PatternCanvas(canvasEl, loom.data);
    loom.canvas.draw();
    return loom;
  }
  
  private static native XPathNSResolver getSvgNSResolver() /*-{
    return function(prefix) {
      if ('svg' == prefix) return 'http://www.w3.org/2000/svg';
      return null;
    }
  }-*/;

  public static String createSvgLaserCutterFile(String baseSvg, JsonObject json)
  {
    // Read in the pattern data
    PatternData data = new PatternData();
    setDataJson(json, data);
    
    // Parse in the SVG
    DOMParser parser = Browser.getWindow().newDOMParser();
    Document doc = parser.parseFromString(baseSvg, "text/xml");
    
    // Modify SVG to fit the data
    // Find all the paths that meet that refer to stitches
    XPathResult result = doc.evaluate("//svg:g[@id='Holes_Pixels']/svg:path", doc, getSvgNSResolver(), XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
    List<Node> stitchNodes = new ArrayList<>();
    for (Node resultNode = result.iterateNext(); resultNode != null; resultNode = result.iterateNext())
      stitchNodes.add(resultNode);
    // Remove stitches where noted
    for (int row = 0; row < data.height; row++)
    {
      for (int col = 0; col < data.width; col++)
      {
        if (!data.rows[row].data[col])
        {
          int idx = (data.height - 1 - row) * data.width + (data.width - 1 - col);
          if (idx < stitchNodes.size())
          {
            Node node = stitchNodes.get(idx);
            node.getParentNode().removeChild(node);
          }
        }
      }
    }
    
    // Return the svg as a string
    XMLSerializer serializer = Browser.getWindow().newXMLSerializer();
    return serializer.serializeToString(doc);
  }
  
  private static JavaScriptObject getDataJson(PatternData data)
  {
    JsonObject json = Json.createObject();
    json.put("foreground", data.fgndColor);
    json.put("background", data.bgndColor);
    JsonArray arr = Json.createArray();
    json.put("rows", arr);
    for (PatternRow row: data.rows)
    {
      JsonObject rowJson = Json.createObject();
      arr.set(arr.length(), rowJson);
//      rowJson.put("color", row.color.name());
      String rowData = "";
      for (boolean bit: row.data)
        rowData += (bit ? "1" : "0");
      rowJson.put("data", rowData);
    }
    return (JavaScriptObject)json;
  }

  private static void setDataJson(JsonObject json, PatternData data)
  {
    JsonArray arr = json.getArray("rows");
    int height = arr.length();
    data.height = height;
    data.fgndColor = json.getString("foreground");
    data.bgndColor = json.getString("background");

    List<PatternRow> rows = new ArrayList<>();
    for (int n = 0; n < arr.length(); n++)
    {
      JsonObject rowJson = arr.getObject(n);
      String rowData = rowJson.getString("data");
      PatternRow row = new PatternRow(rowData.length());
//      row.color = RowColor.valueOf(rowJson.getString("color"));
      for (int i = 0; i < rowData.length(); i++)
        row.data[i] = (rowData.charAt(i) == '1');
      rows.add(row);
    }
    data.rows = rows.toArray(new PatternRow[0]);
    data.width = data.rows[0].data.length;
  }

}
