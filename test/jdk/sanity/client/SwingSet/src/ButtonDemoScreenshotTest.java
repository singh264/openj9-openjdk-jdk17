/*
 * Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import com.sun.swingset3.demos.button.ButtonDemo;
import org.jtregext.GuiTestListener;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.image.StrictImageComparator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.awt.Component;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import static com.sun.swingset3.demos.button.ButtonDemo.DEMO_TITLE;
import static org.jemmy2ext.JemmyExt.*;

/*
 * @test
 * @key headful screenshots intermittent
 * @summary Verifies buttons on SwingSet3 ButtonDemo page by clicking each
 *          button, taking its screenshots and checking that pressed button
 *          image is different from initial button image.
 *
 * @library /sanity/client/lib/jemmy/src
 * @library /sanity/client/lib/Extensions/src
 * @library /sanity/client/lib/SwingSet3/src
 * @modules java.desktop
 *          java.logging
 * @build org.jemmy2ext.JemmyExt
 * @build com.sun.swingset3.demos.button.ButtonDemo
 * @run testng ButtonDemoScreenshotTest
 */
@Listeners(GuiTestListener.class)
public class ButtonDemoScreenshotTest {

    private static final int[] BUTTONS = {0, 1, 2, 3, 4, 5}; // "open browser" buttons (6, 7) open a browser, so ignore
    private static StrictImageComparator sComparator = null;

    @BeforeClass
    public void init() {
        sComparator = new StrictImageComparator();
    }

    @Test
    public void test() throws Exception {
        Robot rob = new Robot();

        new ClassReference(ButtonDemo.class.getCanonicalName()).startApplication();

        JFrameOperator mainFrame = new JFrameOperator(DEMO_TITLE);
        waitImageIsStill(rob, mainFrame);

        // Check all the buttons
        for (int i : BUTTONS) {
            checkButton(mainFrame, i, rob);
        }
    }

    private void checkButton(JFrameOperator jfo, int i, Robot rob) {
        JButtonOperator button = new JButtonOperator(jfo, i);
        button.moveMouse(button.getCenterX(), button.getCenterY());

        BufferedImage initialButtonImage = capture(rob, button);
        assertNotBlack(initialButtonImage);
        save(initialButtonImage, "button" + i + ".png");

        BufferedImage[] pressedImage = new BufferedImage[1];

        button.pressMouse();
        try {
            waitPressed(button);
            button.waitState(new ComponentChooser() {
                public boolean checkComponent(Component c) {
                    pressedImage[0] = capture(rob, button);
                    assertNotBlack(pressedImage[0]);
                    return !sComparator.compare(initialButtonImage, pressedImage[0]);
                }
                public String getDescription() {
                    return "Button with new image";
                }
            });
        } finally {
            if(pressedImage[0] != null) save(pressedImage[0], "button" + i + "_pressed.png");
            button.releaseMouse();
        }
    }
}
