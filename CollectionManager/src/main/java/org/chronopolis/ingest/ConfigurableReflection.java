/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.effects.ReflectionDecorator;

/**
 *
 * @author toaster
 */
public class ConfigurableReflection extends ReflectionDecorator {

    private Component component = null;
    private Graphics2D graphics = null;
    private BufferedImage componentImage = null;
    private Graphics2D componentImageGraphics = null;

    private int yTranslate = 0;
    private int xTranslate = 0;

    public void setXTranslate(int xTranslate) {
        this.xTranslate = xTranslate;
    }

    public void setYTranslate(int yTranslate) {
        this.yTranslate = yTranslate;
    }

    public int getXTranslate() {
        return xTranslate;
    }

    public int getYTranslate() {
        return yTranslate;
    }

    

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.component = component;
        this.graphics = graphics;

        int width = component.getWidth();
        int height = component.getHeight();

        componentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        componentImageGraphics = componentImage.createGraphics();

        // Clear the image background
        componentImageGraphics.setComposite(AlphaComposite.Clear);
        componentImageGraphics.fillRect(0, 0, componentImage.getWidth(), componentImage.getHeight());

        componentImageGraphics.setComposite(AlphaComposite.SrcOver);

        return componentImageGraphics;
    }

    @Override
    public void update() {
        // Draw the component
        graphics.drawImage(componentImage, 0, 0, null);

        // Draw the reflection
        int width = componentImage.getWidth();
        int height = componentImage.getHeight();

        GradientPaint mask = new GradientPaint(0, height / 4f, new Color(1.0f, 1.0f, 1.0f, 0.0f),
                0, height, new Color(1.0f, 1.0f, 1.0f, 0.5f));
        componentImageGraphics.setPaint(mask);

        componentImageGraphics.setComposite(AlphaComposite.DstIn);
        componentImageGraphics.fillRect(0, 0, width, height);

        componentImageGraphics.dispose();
        componentImageGraphics = null;

        componentImage.flush();

        graphics.transform(getTransform(component));

        graphics.drawImage(componentImage, 0, 0, null);

        componentImage = null;
        component = null;
        graphics = null;
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(0, 0, component.getWidth() + xTranslate, component.getHeight() * 2 + yTranslate);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        AffineTransform transform = AffineTransform.getScaleInstance(1.0, -1.0);
        transform.translate(xTranslate, -((component.getHeight() * 2) + yTranslate));

        return transform;
    }
}
