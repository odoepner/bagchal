package net.doepner.baghchal;

import static net.doepner.baghchal.Piece.PREY;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Manages the prey pieces (e.g. goats)
 */
public class PreyManager extends MouseAdapter {

    private static final int TOTAL = 20;

    private final boolean remaining[] = new boolean[TOTAL];

    private final Images images;
    private final Board board;
    private final Phases phases;

    private int selected;

    private boolean dragging = false;

    private int mouseX;
    private int mouseY;

    private Position draggedPos = null;

    private EventHandler eventHandler;

    public PreyManager(Images images, Board board, Phases phases) {
        this.images = images;
        this.board = board;
        this.phases = phases;
    }

    void reset() {
        draggedPos = null;
        dragging = false;
        for (int i = 0; i < TOTAL; i++) {
            remaining[i] = true;
        }
    }

    public void mousePressed(MouseEvent e) {
        final int width = e.getComponent().getWidth();
        final int height = e.getComponent().getHeight();

        final int x = e.getX();
        final int y = e.getY();

        final int xBoardEnd = width - 50;
        final int yBoardEnd = height - 50;

        if (x >= xBoardEnd + 10) {
            int i = (y - 10) / 40;
            dragAvailablePrey(i);
        } else if (y >= yBoardEnd + 10) {
            int i = (x - 10) / 40 + (TOTAL / 2);
            dragAvailablePrey(i);
        }
        if (phases.isMiddle() && isPositionOnBoard(e)) {
            final Position p = getPosition(e);
            if (board.get(p) == PREY) {
                draggedPos = p;
                board.clear(p);
                dragging = true;
            }
        }
    }

    private boolean isPositionOnBoard(MouseEvent e) {
        final int xBoardStart = 20;
        final int yBoardStart = 20;

        final int xBoardEnd = e.getComponent().getWidth() - 50;
        final int yBoardEnd = e.getComponent().getHeight() - 50;

        int x = e.getX();
        int y = e.getY();

        return x >= xBoardStart && x <= xBoardEnd && y >= yBoardStart && y <= yBoardEnd;
    }

    private void dragAvailablePrey(int i) {
        if (remaining[i]) {
            selected = i;
            remaining[i] = false;
            dragging = true;
            draggingStarted();
        }
    }

    private MouseEvent previousEvent;

    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            mouseX = e.getX() - 16; // hard-coded assumption
            mouseY = e.getY() - 16; // that images are 32x32
            dragged(previousEvent);
            dragged(e);
            previousEvent = e;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!dragging) {
            return;
        }
        dragging = false;
        final Position pos = board.normalize(getPosition(e));
        final Move move = new Move(draggedPos, pos);
        if (board.isEmpty(pos) && (phases.isBeginning() || board.validStep(move))) {
            board.set(move.p2(), PREY);
            if (phases.isBeginning() && noRemaining()) {
                phases.setMiddle();
            }
            if (!pos.equals(draggedPos)) {
                moveDone();
            }
        } else {
            if (phases.isBeginning()) {
                remaining[selected] = true;
            } else {
                board.set(draggedPos, PREY);
            }
        }
        dragged(e);
    }

    private Position getPosition(MouseEvent e) {
        return new Position(getXIndex(e), getYIndex(e));
    }

    private int getXIndex(MouseEvent e) {
        final double xStep = e.getComponent().getWidth() / board.getXSize();
        return (int) (e.getX() / xStep + 0.25D);
    }

    private int getYIndex(MouseEvent e) {
        final double yStep = e.getComponent().getHeight() / board.getYSize();
        return (int) (e.getY() / yStep + 0.25D);
    }

    boolean noRemaining() {
        for (int i = 0; i < TOTAL; i++) {
            if (remaining[i]) {
                return false;
            }
        }
        return true;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private void dragged(MouseEvent e) {
        if (eventHandler != null && e != null) {
            eventHandler.dragged(e);
        }
    }

    private void moveDone() {
        if (eventHandler != null) {
            eventHandler.moveDone();
        }
    }

    private void draggingStarted() {
        if (eventHandler != null) {
            eventHandler.draggingStarted();
        }
    }

    public void drawDraggedPrey(Graphics2D g2) {
        if (dragging) {
            g2.drawImage(images.getImage(PREY), mouseX, mouseY, null);
        }
    }

    void drawRemainingPrey(Graphics2D g2, int width, int height) {
        for (int i = 0; i < TOTAL; i++) {
            if (remaining[i]) {
                final Point p = getPoint(i, width, height);
                g2.drawImage(images.getImage(PREY), p.x, p.y, null);
            }
        }
    }

    private Point getPoint(int i, int width, int height) {
        final int half = TOTAL / 2;
        final int x, y;
        if (i < half) {
            x = (width - 50) + 10;
            y = 10 + i * 40;
        } else {
            x = 10 + (i - half) * 40;
            y = (height - 50) + 10;
        }
        return new Point(x,y);
    }
}