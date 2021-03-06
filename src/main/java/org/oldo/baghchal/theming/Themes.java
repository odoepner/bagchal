package org.oldo.baghchal.theming;

import org.guppy4j.io.DirectoryLister;
import org.oldo.baghchal.model.Piece;
import org.oldo.baghchal.resources.ImageLoader;
import org.oldo.baghchal.view.Colors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_MITER;
import static org.oldo.baghchal.model.Piece.PREDATOR;
import static org.oldo.baghchal.model.Piece.PREY;

/**
 * Switchable themes
 */
public final class Themes implements Theme, ThemeSelector {

    private final String resourcePattern;
    private final Iterable<String> themeNames;

    private final ImageLoader imageLoader = new ImageLoader();
    private final ConcurrentMap<String, Properties> propertiesMap = new ConcurrentHashMap<>();

    private String themeName;
    private Colors colors;

    public Themes(DirectoryLister directoryLister, String resourceBasePath, String resourcePattern) {
        this.resourcePattern = resourceBasePath + '/' + resourcePattern;
        final URL resource = getClass().getResource(resourceBasePath);
        if (resource == null) {
            throw new IllegalStateException("Themes location not found in classpath: " + resourceBasePath);
        }
        themeNames = directoryLister.getSubDirectories(resource);
        final Iterator<String> themesIter = themeNames.iterator();
        if (themesIter.hasNext()) {
            selectTheme(themesIter.next());
        } else {
            throw new IllegalStateException("No themes found in classpath under " + resourceBasePath);
        }
    }

    @Override
    public Iterable<String> getAvailableThemeNames() {
        return themeNames;
    }

    @Override
    public void selectTheme(String themeName) {
        if (contains(themeNames, themeName)) {
            this.themeName = themeName;
            colors = new Colors(getProperties(themeName));
        } else {
            selectTheme(themeNames.iterator().next());
        }
    }

    // TODO: move to org.oldo.Iterables in guppy4j libs
    private <T> boolean contains(Iterable<T> iterable, T item) {
        for (T t : iterable) {
            if (Objects.equals(t, item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BufferedImage getImage(Piece piece) {
        return getImage(piece.name().toLowerCase(), "png");
    }

    @Override
    public BufferedImage getImage(ImageId imageId) {
        switch (imageId) {
            case CONGRATS:
                return getImage("congrats", "gif");
            case BACKGROUND:
                return getImage("background", "jpg");
        }
        throw new IllegalArgumentException("Unknown image id : " + imageId);
    }

    @Override
    public Color getColor(ColorId colorId) {
        return colors.getColor(colorId);
    }

    private int preyIndex = 1;

    @Override
    public URL getSoundResource(SoundResourceId resourceId) {
        switch (resourceId) {
            case WELCOME:
                return getResource("welcome", "wav");
            case PREDATOR_KILLS:
                return getResource("predator-kills", "wav");
            case PREDATOR_MOVES:
                return getResource("predator-step", "wav");
            case PREY_MOVES:
                final URL resource = getResource("prey" + preyIndex, "wav");
                preyIndex = (preyIndex % 3) + 1;
                return resource;
            case CONGRATS:
                return getResource("congrats", "wav");
        }
        throw new IllegalArgumentException("Unknown sound : " + resourceId);
    }

    @Override
    public int getPieceWidth() {
        return Math.max(getImage(PREDATOR).getWidth(), getImage(PREY).getWidth());
    }

    @Override
    public int getPieceHeight() {
        return Math.max(getImage(PREDATOR).getHeight(), getImage(PREY).getHeight());
    }

    @Override
    public Paint getBoardPaint() {
        final BufferedImage bgImage = getImage(ImageId.BACKGROUND);
        return new TexturePaint(bgImage, new Rectangle(0, 0, bgImage.getWidth(), bgImage.getHeight()));
    }

    @Override
    public Stroke getDragBoxStroke() {
        return new BasicStroke(2f, CAP_BUTT, JOIN_MITER, 2, new float[]{2}, 0);
    }

    private BufferedImage getImage(String pieceName, String extension) {
        return imageLoader.getImage(getResource(pieceName, extension));
    }

    private URL getResource(String filename, String extension) {
        final String location = String.format(resourcePattern, themeName, filename, extension);
        return getClass().getResource(location);
    }

    private Properties getProperties(String themeName) {
        final Properties p = propertiesMap.get(themeName);
        if (p != null) {
            return p;
        }
        final Properties p1 = new Properties();
        try (final InputStream stream = getResource("theme", "properties").openStream()) {
            p1.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        propertiesMap.put(themeName, p1);
        return p1;
    }

    @Override
    public String getSelectedThemeName() {
        return themeName;
    }
}
