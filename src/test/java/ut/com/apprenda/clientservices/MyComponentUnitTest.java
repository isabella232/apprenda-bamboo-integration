package ut.com.apprenda.clientservices;

import org.junit.Test;
import com.apprenda.clientservices.api.MyPluginComponent;
import com.apprenda.clientservices.impl.MyPluginComponentImpl;

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