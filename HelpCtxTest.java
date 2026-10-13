package org.openide.util;

import java.net.URL;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HelpCtxTest {

    @Test
    public void testNullHelpId() {
        HelpCtx help = new HelpCtx((String) null);

        assertNull(help.getHelpID());
        assertNull(help.getHelp());
        assertEquals("HelpCtx[null]", help.toString());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testUrlConstructor() throws Exception {
        URL url = new URL("https://example.com/help");
        HelpCtx help = new HelpCtx(url);

        assertNull(help.getHelpID());
        assertEquals(url, help.getHelp());
        assertEquals("HelpCtx[https://example.com/help]", help.toString());
    }

    @Test
    public void testEqualHelpContextsHaveSameHashCode() {
        HelpCtx first = new HelpCtx("test.help");
        HelpCtx second = new HelpCtx("test.help");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void testHelpContextIsNotEqualToDifferentObjectType() {
        HelpCtx help = new HelpCtx("test.help");

        assertFalse(help.equals("test.help"));
    }

    @Test
    public void testSetHelpIdStringToNullRemovesHelpId() {
        JButton button = new JButton();

        HelpCtx.setHelpIDString(button, "button.help");
        assertEquals("button.help", button.getClientProperty("HelpID"));

        HelpCtx.setHelpIDString(button, null);
        assertNull(button.getClientProperty("HelpID"));
    }

    @Test
    public void testFindHelpReturnsClientPropertyHelp() {
        JButton button = new JButton();
        HelpCtx.setHelpIDString(button, "button.help");

        HelpCtx result = HelpCtx.findHelp(button);

        assertEquals("button.help", result.getHelpID());
    }

    @Test
    public void testFindHelpReturnsDefaultHelpWhenNothingFound() {
        JButton button = new JButton();

        HelpCtx result = HelpCtx.findHelp(button);

        assertEquals(HelpCtx.DEFAULT_HELP, result);
    }

    @Test
    public void testFindHelpUsesParentComponent() {
        JPanel parent = new JPanel();
        JButton child = new JButton();

        HelpCtx.setHelpIDString(parent, "parent.help");
        parent.add(child);

        HelpCtx result = HelpCtx.findHelp(child);

        assertEquals("parent.help", result.getHelpID());
    }

    @Test
    public void testFindHelpReturnsDefaultHelpForNullComponent() {
        HelpCtx result = HelpCtx.findHelp((java.awt.Component) null);

        assertEquals(HelpCtx.DEFAULT_HELP, result);
    }

    
    @Test
    public void testChildHelpIdTakesPrecedenceOverParentHelpId() {
        JPanel parent = new JPanel();
        JButton child = new JButton();

        HelpCtx.setHelpIDString(parent, "parent.help");
        HelpCtx.setHelpIDString(child, "child.help");
        parent.add(child);

        HelpCtx result = HelpCtx.findHelp(child);

        assertEquals("child.help", result.getHelpID());
    }

    
    @Test
    public void testFindHelpWithNullObject() {
        assertThrows(NullPointerException.class, () ->
            HelpCtx.findHelp((Object) null)
        );
    }



}
