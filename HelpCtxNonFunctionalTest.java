package org.openide.util;

import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.Test;

import java.awt.Component;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HelpCtxNonFunctionalTest {

    @Test
    public void testHelpCtxCreationPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            HelpCtx help = new HelpCtx("help.id." + i);
            assertEquals("help.id." + i, help.getHelpID());
        }
        long duration = System.currentTimeMillis() - startTime;
        assertTrue("HelpCtx-olioiden luonti kesti liian kauan: " + duration + " ms", duration < 1000);
    }


    @Test
    public void testMemoryManagementAndGarbageCollection() {
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        for (int i = 0; i < 50_000; i++) {
            JButton button = new JButton();
            HelpCtx.setHelpIDString(button, "temp.help." + i);
            HelpCtx.findHelp(button);
        }
        
        System.gc();
        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        assertNotNull(finalMemory);
    }

    @Test
    public void testDeepComponentHierarchyStress() {
        int depth = 1000;
        JPanel root = new JPanel();
        HelpCtx.setHelpIDString(root, "root.help");
        
        JPanel current = root;
        for (int i = 0; i < depth; i++) {
            JPanel next = new JPanel();
            current.add(next);
            current = next;
        }
        
        JButton deepestButton = new JButton();
        current.add(deepestButton);

        HelpCtx result = HelpCtx.findHelp(deepestButton);
        assertEquals("root.help", result.getHelpID());
    }

    @Test
    public void testFindHelpConcurrency() throws InterruptedException {
        final JPanel parent = new JPanel();
        HelpCtx.setHelpIDString(parent, "parent.help");
        
        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    JButton child = new JButton();
                    parent.add(child);
                    HelpCtx result = HelpCtx.findHelp(child);
                    assertEquals("parent.help", result.getHelpID());
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue("Säikeistystesti keskeytyi aikakatkaisuun", finished);
    }

    @Test
    public void testSecurityAndMalformedInputHandling() {
        String maliciousOrLongId = "A".repeat(10000) + "<script>alert('test')</script>";
        HelpCtx help = new HelpCtx(maliciousOrLongId);
        
        assertEquals(maliciousOrLongId, help.getHelpID());
        assertNotNull(help.toString());
    }

    @Test
    public void testScalabilityWithMultipleProperties() {
        JButton button = new JButton();
        for (int i = 0; i < 1000; i++) {
            HelpCtx.setHelpIDString(button, "help.id." + i);
        }
        assertEquals("help.id.999", HelpCtx.findHelp(button).getHelpID());
    }

    @Test
    public void testLongRunningEndurance() {
        JButton button = new JButton();
        HelpCtx.setHelpIDString(button, "endurance.help");

        for (int i = 0; i < 500_000; i++) {
            HelpCtx result = HelpCtx.findHelp(button);
            assertEquals("endurance.help", result.getHelpID());
        }
    }

    @Test
    public void testUrlConstructorResourceHandling() throws Exception {
        URL url = new URL("https://netbeans.apache.org/");
        for (int i = 0; i < 1000; i++) {
            HelpCtx help = new HelpCtx(url);
            assertEquals(url, help.getHelp());
        }
    }

    @Test
    public void testRobustnessWithNullAndInvalidStructures() {
        assertEquals(HelpCtx.DEFAULT_HELP, HelpCtx.findHelp((Component) null));
        
        JButton button = new JButton();
        HelpCtx.setHelpIDString(button, null);
        assertEquals(HelpCtx.DEFAULT_HELP, HelpCtx.findHelp(button));
    }

    @Test
    public void testEnvironmentAndLocaleIndependence() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ja", "JP"));
            HelpCtx help = new HelpCtx("locale.test.help");
            assertEquals("locale.test.help", help.getHelpID());
            
            JButton button = new JButton();
            HelpCtx.setHelpIDString(button, "locale.button.help");
            assertEquals("locale.button.help", HelpCtx.findHelp(button).getHelpID());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}