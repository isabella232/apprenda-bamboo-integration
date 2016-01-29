package ut.apprenda.clientservices;

import org.junit.Test;
import apprenda.clientservices.api.MyPluginComponent;
import apprenda.clientservices.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}