package org.programmingbasics.loom;

import org.junit.Assert;
import org.junit.Test;
import org.programmingbasics.loom.PatternCanvas.DrawOrder;

public class PatternCanvasTest
{
  @Test
  public void testDrawOrderWide()
  {
    DrawOrder order = new DrawOrder(2, 3);
    Assert.assertTrue(order.hasNext());
    order.next();
    Assert.assertEquals(2, order.col);
    Assert.assertEquals(0, order.row);
    Assert.assertTrue(order.hasNext());
    
    order.next();
    Assert.assertEquals(2, order.col);
    Assert.assertEquals(1, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(1, order.col);
    Assert.assertEquals(0, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(1, order.col);
    Assert.assertEquals(1, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(0, order.col);
    Assert.assertEquals(0, order.row);
    Assert.assertTrue(order.hasNext());
    
    order.next();
    Assert.assertEquals(0, order.col);
    Assert.assertEquals(1, order.row);
    Assert.assertFalse(order.hasNext());
  }
  
  @Test
  public void testDrawOrderTall()
  {
    DrawOrder order = new DrawOrder(3, 2);
    Assert.assertTrue(order.hasNext());
    order.next();
    Assert.assertEquals(1, order.col);
    Assert.assertEquals(0, order.row);
    Assert.assertTrue(order.hasNext());
    
    order.next();
    Assert.assertEquals(1, order.col);
    Assert.assertEquals(1, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(0, order.col);
    Assert.assertEquals(0, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(1, order.col);
    Assert.assertEquals(2, order.row);
    Assert.assertTrue(order.hasNext());

    order.next();
    Assert.assertEquals(0, order.col);
    Assert.assertEquals(1, order.row);
    Assert.assertTrue(order.hasNext());
    
    order.next();
    Assert.assertEquals(0, order.col);
    Assert.assertEquals(2, order.row);
    Assert.assertFalse(order.hasNext());
  }

}
