/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.swing.panel.ui.MouseSelectedArea;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementRoot;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelFirst;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelOther;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;

import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang.StringEscapeUtils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.api.VisualAttributePlugin;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.igormaznitsa.mindmap.plugins.api.ModelAwarePlugin;
import com.igormaznitsa.mindmap.plugins.api.PanelAwarePlugin;
import java.util.concurrent.locks.ReentrantLock;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;

public class MindMapPanel extends JPanel {

  public static final long serialVersionUID = 2783412123454232L;

  /**
   * Some Job over mind map model.
   *
   * @since 1.3.1
   * @see MindMapPanel#executeModelJobs(com.igormaznitsa.mindmap.swing.panel.MindMapPanel.ModelJob...)
   */
  public interface ModelJob {

    /**
     * Execute the job.
     *
     * @param model model to be processed
     * @return true if to continue job sequence, false if to interrupt
     */
    boolean doChangeModel(@Nonnull MindMap model);
  }

  public static final String ATTR_SHOW_JUMPS = "showJumps";
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapPanel.class);
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  private final MindMapPanelController controller;

  private static final int ALL_SUPPORTED_MODIFIERS = KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.META_MASK | KeyEvent.CTRL_MASK;

  private final Map<Object, WeakReference<?>> weakTable = new WeakHashMap<Object, WeakReference<?>>();
  private final AtomicBoolean disposed = new AtomicBoolean();
  private final ReentrantLock panelLocker = new ReentrantLock();

  public static class DraggedElement {

    public enum Modifier {

      NONE,
      MAKE_JUMP;
    }

    @Nonnull
    private final AbstractElement element;
    private final Image prerenderedImage;
    private final Point mousePointerOffset;
    private final Point currentPosition;
    private final DraggedElement.Modifier modifier;

    public DraggedElement(@Nonnull final AbstractElement element, @Nonnull final MindMapPanelConfig cfg, @Nonnull final Point mousePointerOffset, @Nonnull final DraggedElement.Modifier modifier) {
      this.element = element;
      this.prerenderedImage = Utils.renderWithTransparency(0.55f, element, cfg);
      this.mousePointerOffset = mousePointerOffset;
      this.currentPosition = new Point();
      this.modifier = modifier;
    }

    @Nonnull
    public DraggedElement.Modifier getModifier() {
      return this.modifier;
    }

    public boolean isPositionInside() {
      return this.element.getBounds().contains(this.currentPosition);
    }

    @Nonnull
    public AbstractElement getElement() {
      return this.element;
    }

    public void updatePosition(@Nonnull final Point point) {
      this.currentPosition.setLocation(point);
    }

    @Nonnull
    public Point getPosition() {
      return this.currentPosition;
    }

    @Nonnull
    public Point getMousePointerOffset() {
      return this.mousePointerOffset;
    }

    public int getDrawPositionX() {
      return this.currentPosition.x - this.mousePointerOffset.x;
    }

    public int getDrawPositionY() {
      return this.currentPosition.y - this.mousePointerOffset.y;
    }

    @Nonnull
    public Image getImage() {
      return this.prerenderedImage;
    }

    public void draw(@Nonnull final Graphics2D gfx) {
      final int x = getDrawPositionX();
      final int y = getDrawPositionY();
      gfx.drawImage(this.prerenderedImage, x, y, null);
    }
  }

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");

  private volatile MindMap model;
  private volatile String errorText;

  private final List<MindMapListener> mindMapListeners = new CopyOnWriteArrayList<MindMapListener>();

  private static final double SCALE_STEP = 0.2d;
  private static final double SCALE_MINIMUM = 0.3d;
  private static final double SCALE_MAXIMUM = 10.0d;

  private static final Color COLOR_MOUSE_DRAG_SELECTION = new Color(0x80000000, true);

  private final JTextArea textEditor = UI_COMPO_FACTORY.makeTextArea();
  private final JPanel textEditorPanel = UI_COMPO_FACTORY.makePanel();
  private transient AbstractElement elementUnderEdit = null;
  private transient int[] pathToPrevTopicBeforeEdit = null;

  private final List<Topic> selectedTopics = new ArrayList<Topic>();

  private transient MouseSelectedArea mouseDragSelection = null;
  private transient DraggedElement draggedElement = null;
  private transient AbstractElement destinationElement = null;

  private volatile boolean popupMenuActive = false;

  private final MindMapPanelConfig config;

  public MindMapPanel(@Nonnull final MindMapPanelController controller) {
    super(null);
    final MindMapPanelConfig panelConfig = controller.provideConfigForMindMapPanel(this);

    this.textEditorPanel.setLayout(new BorderLayout(0, 0));
    this.controller = controller;

    this.config = new MindMapPanelConfig(panelConfig, false);

    this.textEditor.setMargin(new Insets(5, 5, 5, 5));
    this.textEditor.setBorder(BorderFactory.createEtchedBorder());
    this.textEditor.setTabSize(4);
    this.textEditor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            switch (e.getKeyCode()) {
              case KeyEvent.VK_ENTER: {
                e.consume();
              }
              break;
              case KeyEvent.VK_TAB: {
                if ((e.getModifiers() & ALL_SUPPORTED_MODIFIERS) == 0) {
                  e.consume();
                  final Topic edited = elementUnderEdit.getModel();
                  final int[] topicPosition = edited.getPositionPath();
                  endEdit(true);
                  SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      final Topic theTopic = model.findForPositionPath(topicPosition);
                      if (theTopic != null) {
                        makeNewChildAndStartEdit(theTopic, null);
                      }
                    }
                  });
                }
              }
              break;
              default:
                break;
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyTyped(@Nonnull final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_TEXT_NEXT_LINE, e)) {
              e.consume();
              textEditor.insert("\n", textEditor.getCaretPosition()); //NOI18N
            } else if (e.getKeyChar() == KeyEvent.VK_ENTER && (e.getModifiers() & ALL_SUPPORTED_MODIFIERS) == 0) {
              e.consume();
              endEdit(true);
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyReleased(@Nonnull final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_CANCEL_EDIT, e)) {
              e.consume();
              final Topic edited = elementUnderEdit == null ? null : elementUnderEdit.getModel();
              endEdit(false);
              if (edited != null && edited.canBeLost()) {
                deleteTopics(false, edited);
                if (pathToPrevTopicBeforeEdit != null) {
                  final int[] path = pathToPrevTopicBeforeEdit;
                  pathToPrevTopicBeforeEdit = null;
                  SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      final Topic topic = model.findForPositionPath(path);
                      if (topic != null) {
                        select(topic, false);
                      }
                    }
                  });
                }
              }
            }
          } finally {
            unlock();
          }
        }
      }
    });

    this.textEditor.getDocument().addDocumentListener(new DocumentListener() {

      private void updateEditorPanelSize(@Nonnull final Dimension newSize) {
        if (lockIfNotDisposed()) {
          try {
            final Dimension editorPanelMinSize = textEditorPanel.getMinimumSize();
            final Dimension newDimension = new Dimension(Math.max(editorPanelMinSize.width, newSize.width), Math.max(editorPanelMinSize.height, newSize.height));
            textEditorPanel.setSize(newDimension);
            textEditorPanel.repaint();
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }
    });
    this.textEditorPanel.add(this.textEditor, BorderLayout.CENTER);

    super.setOpaque(true);

    final KeyAdapter keyAdapter = new KeyAdapter() {
      @Override
      public void keyTyped(@Nonnull final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_ADD_CHILD_AND_START_EDIT, e)) {
              e.consume();
              if (!selectedTopics.isEmpty()) {
                makeNewChildAndStartEdit(selectedTopics.get(0), null);
              }
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ADD_SIBLING_AND_START_EDIT, e)) {
              e.consume();
              if (!hasActiveEditor() && hasOnlyTopicSelected()) {
                final Topic baseTopic = selectedTopics.get(0);
                makeNewChildAndStartEdit(baseTopic.getParent() == null ? baseTopic : baseTopic.getParent(), baseTopic);
              }
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_FOCUS_ROOT_OR_START_EDIT, e)) {
              e.consume();
              if (!hasSelectedTopics()) {
                select(getModel().getRoot(), false);
              } else if (hasOnlyTopicSelected()) {
                startEdit((AbstractElement) selectedTopics.get(0).getPayload());
              }
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyReleased(@Nonnull final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_SHOW_POPUP, e)) {
              e.consume();
              processPopUpForShortcut();
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_DELETE_TOPIC, e)) {
              e.consume();
              focusTo(deleteSelectedTopics(false));
            } else if (config.isKeyEventDetected(e,
                MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT,
                MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT,
                MindMapPanelConfig.KEY_FOCUS_MOVE_UP,
                MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN,
                MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
                MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
                MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
                MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED)) {
              e.consume();
              processMoveFocusByKey(e);
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_IN, e)) {
              e.consume();
              setScale(Math.max(SCALE_MINIMUM, Math.min(getScale() + SCALE_STEP, SCALE_MAXIMUM)));
              updateView(false);
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_OUT, e)) {
              e.consume();
              setScale(Math.max(SCALE_MINIMUM, Math.min(getScale() - SCALE_STEP, SCALE_MAXIMUM)));
              updateView(false);
            } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_RESET, e)) {
              e.consume();
              setScale(1.0);
              updateView(false);
            }
          } finally {
            unlock();
          }
        }
      }
    };

    this.setFocusTraversalKeysEnabled(false);

    final MindMapPanel theInstance = this;

    final MouseAdapter adapter = new MouseAdapter() {

      @Override
      public void mouseEntered(@Nonnull final MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }

      @Override
      public void mouseMoved(@Nonnull final MouseEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
              return;
            }
            final AbstractElement element = findTopicUnderPoint(e.getPoint());
            if (element == null) {
              setCursor(Cursor.getDefaultCursor());
              setToolTipText(null);
            } else {
              final ElementPart part = element.findPartForPoint(e.getPoint());
              switch (part) {
                case ICONS: {
                  final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                  if (extra != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setToolTipText(makeHtmlTooltipForExtra(extra));
                  } else {
                    setCursor(null);
                    setToolTipText(null);
                  }
                }
                break;
                case VISUAL_ATTRIBUTES: {
                  final VisualAttributePlugin plugin = element.getVisualAttributeImageBlock().findPluginForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                  if (plugin != null) {
                    final Topic theTopic = element.getModel();
                    if (plugin.isClickable(theInstance, theTopic)) {
                      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                      setCursor(null);
                    }
                    setToolTipText(plugin.getToolTip(theInstance, theTopic));
                  } else {
                    setCursor(null);
                    setToolTipText(null);
                  }
                }
                break;
                case COLLAPSATOR: {
                  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  setToolTipText(null);
                }
                break;
                default: {
                  setCursor(Cursor.getDefaultCursor());
                  setToolTipText(null);
                }
                break;
              }
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mousePressed(@Nonnull final MouseEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!controller.isMouseClickProcessingAllowed(theInstance)) {
              return;
            }
            try {
              if (e.isPopupTrigger()) {
                mouseDragSelection = null;
                MindMap theMap = model;
                AbstractElement element = null;
                if (theMap != null) {
                  element = findTopicUnderPoint(e.getPoint());
                }
                processPopUp(e.getPoint(), element);
                e.consume();
              } else {
                endEdit(elementUnderEdit != null);
                mouseDragSelection = null;
              }
            } catch (Exception ex) {
              LOGGER.error("Error during mousePressed()", ex);
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseReleased(@Nonnull final MouseEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!controller.isMouseClickProcessingAllowed(theInstance)) {
              return;
            }
            try {
              if (draggedElement != null) {
                draggedElement.updatePosition(e.getPoint());
                if (endDragOfElement(draggedElement, destinationElement)) {
                  updateView(true);
                }
              } else if (mouseDragSelection != null) {
                final List<Topic> covered = mouseDragSelection.getAllSelectedElements(model);
                if (e.isShiftDown()) {
                  for (final Topic m : covered) {
                    select(m, false);
                  }
                } else if (e.isControlDown()) {
                  for (final Topic m : covered) {
                    select(m, true);
                  }
                } else {
                  removeAllSelection();
                  for (final Topic m : covered) {
                    select(m, false);
                  }
                }
              } else if (e.isPopupTrigger()) {
                mouseDragSelection = null;
                MindMap theMap = model;
                AbstractElement element = null;
                if (theMap != null) {
                  element = findTopicUnderPoint(e.getPoint());
                }
                processPopUp(e.getPoint(), element);
                e.consume();
              }
            } catch (Exception ex) {
              LOGGER.error("Error during mouseReleased()", ex);
            } finally {
              mouseDragSelection = null;
              draggedElement = null;
              destinationElement = null;
              repaint();
            }
          } finally {
            unlock();
          }
        }
      }

      private boolean isNonOverCollapsator(@Nonnull final MouseEvent e, @Nonnull final AbstractElement element) {
        final ElementPart part = element.findPartForPoint(e.getPoint());
        return part != ElementPart.COLLAPSATOR;
      }

      @Override
      public void mouseDragged(@Nonnull final MouseEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
              return;
            }
            scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));

            if (!popupMenuActive) {
              if (draggedElement == null && mouseDragSelection == null) {
                final AbstractElement elementUnderMouse = findTopicUnderPoint(e.getPoint());
                if (elementUnderMouse == null) {
                  MindMap theMap = model;
                  if (theMap != null) {
                    final AbstractElement element = findTopicUnderPoint(e.getPoint());
                    if (controller.isSelectionAllowed(theInstance) && element == null) {
                      mouseDragSelection = new MouseSelectedArea(e.getPoint());
                    }
                  }
                } else if (controller.isElementDragAllowed(theInstance)) {
                  if (elementUnderMouse.isMoveable() && isNonOverCollapsator(e, elementUnderMouse)) {
                    selectedTopics.clear();

                    final Point mouseOffset = new Point((int) Math.round(e.getPoint().getX() - elementUnderMouse.getBounds().getX()), (int) Math.round(e.getPoint().getY() - elementUnderMouse.getBounds().getY()));
                    draggedElement = new DraggedElement(elementUnderMouse, config, mouseOffset, e.isControlDown() || e.isMetaDown() ? DraggedElement.Modifier.MAKE_JUMP : DraggedElement.Modifier.NONE);
                    draggedElement.updatePosition(e.getPoint());
                    findDestinationElementForDragged();
                  } else {
                    draggedElement = null;
                  }
                  repaint();
                }
              } else if (mouseDragSelection != null) {
                if (controller.isSelectionAllowed(theInstance)) {
                  mouseDragSelection.update(e);
                } else {
                  mouseDragSelection = null;
                }
                repaint();
              } else if (draggedElement != null) {
                if (controller.isElementDragAllowed(theInstance)) {
                  draggedElement.updatePosition(e.getPoint());
                  findDestinationElementForDragged();
                } else {
                  draggedElement = null;
                }
                repaint();
              }
            } else {
              mouseDragSelection = null;
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseWheelMoved(@Nonnull final MouseWheelEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (controller.isMouseWheelProcessingAllowed(theInstance)) {
              mouseDragSelection = null;
              draggedElement = null;

              final MindMapPanelConfig theConfig = config;

              if (!e.isConsumed() && (theConfig != null && ((e.getModifiers() & theConfig.getScaleModifiers()) == theConfig.getScaleModifiers()))) {
                endEdit(elementUnderEdit != null);

                setScale(Math.max(SCALE_MINIMUM, Math.min(getScale() + (SCALE_STEP * -e.getWheelRotation()), SCALE_MAXIMUM)));

                updateView(false);
                e.consume();
              } else {
                sendToParent(e);
              }
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!controller.isMouseClickProcessingAllowed(theInstance)) {
              return;
            }
            mouseDragSelection = null;
            draggedElement = null;

            MindMap theMap = model;
            AbstractElement element = null;
            if (theMap != null) {
              element = findTopicUnderPoint(e.getPoint());
            }

            final boolean isCtrlDown = e.isControlDown();

            if (element != null) {
              final ElementPart part = element.findPartForPoint(e.getPoint());
              if (part == ElementPart.COLLAPSATOR) {
                removeAllSelection();

                if (element.isCollapsed()) {
                  ((AbstractCollapsableElement) element).setCollapse(false);
                  if (isCtrlDown) {
                    ((AbstractCollapsableElement) element).collapseAllFirstLevelChildren();
                  }
                } else {
                  ((AbstractCollapsableElement) element).setCollapse(true);
                }
                notifyModelChanged();
                repaint();
              } else if (!isCtrlDown) {
                switch (part) {
                  case VISUAL_ATTRIBUTES:
                    final VisualAttributePlugin plugin = element.getVisualAttributeImageBlock().findPluginForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                    boolean processedByPlugin = false;
                    if (plugin != null) {
                      if (plugin.isClickable(theInstance, element.getModel())) {
                        processedByPlugin = true;
                        try {
                          if (plugin.onClick(theInstance, element.getModel(), e.getClickCount())) {
                            notifyModelChanged();
                            repaint();
                          }
                        } catch (Exception ex) {
                          LOGGER.error("Error during visual attribute processing", ex);
                          controller.getDialogProvider(theInstance).msgError("Detectd critical error! See log!");
                        }
                      }
                    }
                    if (!processedByPlugin) {
                      removeAllSelection();
                      select(element.getModel(), false);
                    }
                    break;
                  case ICONS:
                    final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                    if (extra != null) {
                      fireNotificationClickOnExtra(element.getModel(), e.getClickCount(), extra);
                    }
                    break;
                  default:
                    // only
                    removeAllSelection();
                    select(element.getModel(), false);
                    if (e.getClickCount() > 1) {
                      startEdit(element);
                    }
                    break;
                }
              } else // group
              {
                if (selectedTopics.isEmpty()) {
                  select(element.getModel(), false);
                } else {
                  select(element.getModel(), true);
                }
              }
            }
          } finally {
            unlock();
          }
        }
      }
    };

    addMouseWheelListener(adapter);
    addMouseListener(adapter);
    addMouseMotionListener(adapter);
    addKeyListener(keyAdapter);

    this.textEditorPanel.setVisible(false);
    this.add(this.textEditorPanel);

    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(@Nonnull final ComponentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateView(false);
            updateEditorAfterResizing();
          }
        });
      }
    });

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (lockIfNotDisposed()) {
          try {
            for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(PanelAwarePlugin.class)) {
              p.onPanelCreate(theInstance);
            }
          } finally {
            unlock();
          }
        }
      }
    });
  }

  @Nullable
  public Object findTmpObject(@Nonnull final Object key) {
    this.lock();
    try {
      final WeakReference ref = this.weakTable.get(key);
      return ref == null ? null : ref.get();
    } finally {
      this.unlock();
    }
  }

  public void putTmpObject(@Nonnull final Object key, @Nullable final Object value) {
    this.lock();
    try {
      if (value == null) {
        this.weakTable.remove(key);
      } else {
        this.weakTable.put(key, new WeakReference<Object>(value));
      }
    } finally {
      this.unlock();
    }
  }

  @Nonnull
  private String makeHtmlTooltipForExtra(@Nonnull final Extra<?> extra) {
    final StringBuilder builder = new StringBuilder();

    builder.append("<html>"); //NOI18N

    switch (extra.getType()) {
      case FILE: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenFile")).append(StringEscapeUtils.escapeHtml(((ExtraFile) extra).getAsString()));
      }
      break;
      case TOPIC: {
        final Topic topic = this.getModel().findTopicForLink((ExtraTopic) extra);
        builder.append(BUNDLE.getString("MindMapPanel.tooltipJumpToTopic")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(topic == null ? "----" : topic.getText(), 32)));
      }
      break;
      case LINK: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenLink")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(((ExtraLink) extra).getAsString(), 48)));
      }
      break;
      case NOTE: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenText")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(((ExtraNote) extra).getAsString(), 64)));
      }
      break;
      default: {
        builder.append("<b>Unknown</b>"); //NOI18N
      }
      break;
    }

    builder.append("</html>"); //NOI18N

    return builder.toString();
  }

  public void refreshConfiguration() {
    if (this.lockIfNotDisposed()) {
      try {
        final MindMapPanel theInstance = this;
        final double scale = this.config.getScale();
        this.config.makeAtomicChange(new Runnable() {
          @Override
          public void run() {
            config.makeFullCopyOf(controller.provideConfigForMindMapPanel(theInstance), false, false);
            config.setScale(scale);
          }
        });
        invalidate();
        repaint();
      } finally {
        this.unlock();
      }
    }
  }

  private static final int DRAG_POSITION_UNKNOWN = -1;
  private static final int DRAG_POSITION_LEFT = 1;
  private static final int DRAG_POSITION_TOP = 2;
  private static final int DRAG_POSITION_BOTTOM = 3;
  private static final int DRAG_POSITION_RIGHT = 4;

  private int calcDropPosition(@Nonnull final AbstractElement destination, @Nonnull final Point dropPoint) {
    final int result;
    if (destination.getClass() == ElementRoot.class) {
      result = dropPoint.getX() < destination.getBounds().getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_RIGHT;
    } else {
      final boolean destinationIsLeft = destination.isLeftDirection();
      final Rectangle2D bounds = destination.getBounds();

      final double edgeOffset = bounds.getWidth() * 0.2d;
      if (dropPoint.getX() >= (bounds.getX() + edgeOffset) && dropPoint.getX() <= (bounds.getMaxX() - edgeOffset)) {
        result = dropPoint.getY() < bounds.getCenterY() ? DRAG_POSITION_TOP : DRAG_POSITION_BOTTOM;
      } else if (destinationIsLeft) {
        result = dropPoint.getX() < bounds.getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_UNKNOWN;
      } else {
        result = dropPoint.getX() > bounds.getCenterX() ? DRAG_POSITION_RIGHT : DRAG_POSITION_UNKNOWN;
      }
    }
    return result;
  }

  private boolean endDragOfElement(@Nonnull final DraggedElement draggedElement, @Nonnull final AbstractElement destination) {
    final AbstractElement dragged = draggedElement.getElement();
    final Point dropPoint = draggedElement.getPosition();

    final boolean ignore = dragged.getModel() == destination.getModel() || dragged.getBounds().contains(dropPoint) || destination.getModel().hasAncestor(dragged.getModel());
    if (ignore) {
      return false;
    }

    boolean changed = true;

    if (draggedElement.getModifier() == DraggedElement.Modifier.MAKE_JUMP) {
      // make link
      return this.controller.processDropTopicToAnotherTopic(this, dropPoint, dragged.getModel(), destination.getModel());
    }

    final int pos = calcDropPosition(destination, dropPoint);
    switch (pos) {
      case DRAG_POSITION_TOP:
      case DRAG_POSITION_BOTTOM: {
        dragged.getModel().moveToNewParent(assertNotNull(destination.getParent()).getModel());
        if (pos == DRAG_POSITION_TOP) {
          dragged.getModel().moveBefore(destination.getModel());
        } else {
          dragged.getModel().moveAfter(destination.getModel());
        }

        if (destination.getClass() == ElementLevelFirst.class) {
          AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), destination.isLeftDirection());
        } else {
          AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), false);
        }
      }
      break;
      case DRAG_POSITION_RIGHT:
      case DRAG_POSITION_LEFT: {
        if (dragged.getParent() == destination) {
          // the same parent
          if (destination.getClass() == ElementRoot.class) {
            // process only for the root, just update direction
            if (dragged instanceof AbstractCollapsableElement) {
              ((AbstractCollapsableElement) dragged).setLeftDirection(pos == DRAG_POSITION_LEFT);
            }
          }
        } else {
          dragged.getModel().moveToNewParent(destination.getModel());
          if (destination instanceof AbstractCollapsableElement && destination.isCollapsed() && (controller == null ? true : controller.isUnfoldCollapsedTopicDropTarget(this))) { //NOI18N
            ((AbstractCollapsableElement) destination).setCollapse(false);
          }
          if (dropPoint.getY() < destination.getBounds().getY()) {
            dragged.getModel().makeFirst();
          } else {
            dragged.getModel().makeLast();
          }
          if (destination.getClass() == ElementRoot.class) {
            AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), pos == DRAG_POSITION_LEFT);
          } else {
            AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), false);
          }
        }
      }
      break;
      default:
        break;
    }
    dragged.getModel().setPayload(null);

    return changed;
  }

  private void sendToParent(@Nonnull final AWTEvent evt) {
    final Container parent = this.getParent();
    if (parent != null) {
      parent.dispatchEvent(evt);
    }
  }

  private void processMoveFocusByKey(@Nonnull final KeyEvent key) {
    final AbstractElement lastSelectedTopic = this.selectedTopics.isEmpty() ? null : (AbstractElement) this.selectedTopics.get(this.selectedTopics.size() - 1).getPayload();
    if (lastSelectedTopic == null) {
      return;
    }

    AbstractElement nextFocused = null;

    boolean modelChanged = false;

    if (lastSelectedTopic.isMoveable()) {
      boolean processFirstChild = false;
      if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED)) {
        if (lastSelectedTopic.isLeftDirection()) {
          processFirstChild = true;
        } else {
          nextFocused = (AbstractElement) assertNotNull(lastSelectedTopic.getModel().getParent()).getPayload();
        }
      } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED)) {
        if (lastSelectedTopic.isLeftDirection()) {
          nextFocused = (AbstractElement) assertNotNull(lastSelectedTopic.getModel().getParent()).getPayload();
        } else {
          processFirstChild = true;
        }
      } else {
        final boolean pressedButtonMoveUp = config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_UP, MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED);
        final boolean firstLevel = lastSelectedTopic.getClass() == ElementLevelFirst.class;
        final boolean currentLeft = AbstractCollapsableElement.isLeftSidedTopic(lastSelectedTopic.getModel());

        final TopicChecker checker = new TopicChecker() {
          @Override
          public boolean check(@Nonnull final Topic topic) {
            if (!firstLevel) {
              return true;
            } else if (currentLeft) {
              return AbstractCollapsableElement.isLeftSidedTopic(topic);
            } else {
              return !AbstractCollapsableElement.isLeftSidedTopic(topic);
            }
          }
        };
        final Topic topic = pressedButtonMoveUp ? lastSelectedTopic.getModel().findPrev(checker) : lastSelectedTopic.getModel().findNext(checker);
        nextFocused = topic == null ? null : (AbstractElement) topic.getPayload();
      }

      if (processFirstChild) {
        if (lastSelectedTopic.hasChildren()) {
          if (lastSelectedTopic.isCollapsed()) {
            ((AbstractCollapsableElement) lastSelectedTopic).setCollapse(false);
            modelChanged = true;
          }

          nextFocused = (AbstractElement) (lastSelectedTopic.getModel().getChildren().get(0)).getPayload();
        }
      }
    } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED)) {
      for (final Topic t : lastSelectedTopic.getModel().getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null && e.isLeftDirection()) {
          nextFocused = e;
          break;
        }
      }
    } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED)) {
      for (final Topic t : lastSelectedTopic.getModel().getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null && !e.isLeftDirection()) {
          nextFocused = e;
          break;
        }
      }
    }

    if (nextFocused != null) {
      final boolean addFocused = config.isKeyEventDetected(key,
          MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
          MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
          MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
          MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED);
      if (!addFocused || this.selectedTopics.contains(nextFocused.getModel())) {
        removeAllSelection();
      }
      select(nextFocused.getModel(), false);
    }

    if (modelChanged) {
      notifyModelChanged();
    }
  }

  /**
   * Safe Swing thread execution sequence of some jobs over model with model changed notification in the end
   *
   * @param jobs sequence of jobs to be executed
   *
   * @since 1.3.1
   */
  public void executeModelJobs(@Nonnull @MustNotContainNull final ModelJob... jobs) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        for (final ModelJob j : jobs) {
          try {
            if (!j.doChangeModel(model)) {
              break;
            }
          } catch (Exception ex) {
            LOGGER.error("Errot during job execution", ex);
          }
        }
        notifyModelChanged();
      }
    });
  }

  /**
   * Send signal that the model has been changed.
   *
   * @since 1.2
   */
  public void notifyModelChanged() {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        if (lockIfNotDisposed()) {
          try {
            invalidate();
            fireNotificationMindMapChanged();
          } finally {
            unlock();
          }
        }
      }
    });
  }

  private void ensureVisibility(@Nonnull final AbstractElement e) {
    fireNotificationEnsureTopicVisibility(e.getModel());
  }

  private boolean hasActiveEditor() {
    if (lockIfNotDisposed()) {
      try {
        return this.elementUnderEdit != null;
      } finally {
        unlock();
      }
    }
    return false;
  }

  public boolean isShowJumps() {
    return Boolean.parseBoolean(this.model.getAttribute(ATTR_SHOW_JUMPS));
  }

  public void setShowJumps(final boolean flag) {
    if (lockIfNotDisposed()) {
      try {
        this.model.setAttribute(ATTR_SHOW_JUMPS, flag ? "true" : null);
        repaint();
        fireNotificationMindMapChanged();
      } finally {
        this.unlock();
      }
    }
  }

  @Nonnull
  private Topic makeNewTopic(@Nonnull final Topic parent, @Nullable final Topic afterTopic, @Nonnull final String text) {
    final Topic result = parent.makeChild(text, afterTopic);
    for (final ModelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(ModelAwarePlugin.class)) {
      p.onCreateTopic(this, parent, result);
    }
    return result;
  }

  public void makeNewChildAndStartEdit(@Nullable final Topic parent, @Nullable final Topic baseTopic) {
    if (this.lockIfNotDisposed()) {
      try {
        if (parent != null) {
          final Topic currentSelected = getFirstSelected();
          this.pathToPrevTopicBeforeEdit = currentSelected == null ? null : currentSelected.getPositionPath();

          removeAllSelection();

          final Topic newTopic = makeNewTopic(parent, baseTopic, ""); //NOI18N

          if (this.controller.isCopyColorInfoFromParentToNewChildAllowed(this) && !parent.isRoot()) {
            MindMapUtils.copyColorAttributes(parent, newTopic);
          }

          final AbstractElement parentElement = (AbstractElement) parent.getPayload();

          if (parent.getChildren().size() != 1 && parent.getParent() == null && baseTopic == null) {
            int numLeft = 0;
            int numRight = 0;
            for (final Topic t : parent.getChildren()) {
              if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
                numLeft++;
              } else {
                numRight++;
              }
            }

            AbstractCollapsableElement.makeTopicLeftSided(newTopic, numLeft < numRight);
          } else if (baseTopic != null && baseTopic.getPayload() != null) {
            final AbstractElement element = assertNotNull((AbstractElement) baseTopic.getPayload());
            AbstractCollapsableElement.makeTopicLeftSided(newTopic, element.isLeftDirection());
          }

          if (parentElement instanceof AbstractCollapsableElement && parentElement.isCollapsed()) {
            ((AbstractCollapsableElement) parentElement).setCollapse(false);
          }

          select(newTopic, false);
          updateView(false);
          startEdit((AbstractElement) newTopic.getPayload());
        }
      } finally {
        this.unlock();
      }
    }
  }

  protected void fireNotificationSelectionChanged() {
    final Topic[] selected = this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]);
    for (final MindMapListener l : this.mindMapListeners) {
      l.onChangedSelection(this, selected);
    }
  }

  protected void fireNotificationMindMapChanged() {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onMindMapModelChanged(this);
    }
  }

  protected void fireNotificationClickOnExtra(@Nonnull final Topic topic, final int clicks, @Nonnull final Extra<?> extra) {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onClickOnExtra(this, clicks, topic, extra);
    }
  }

  protected void fireNotificationEnsureTopicVisibility(@Nonnull final Topic topic) {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onEnsureVisibilityOfTopic(this, topic);
    }
  }

  public void deleteTopics(final boolean force, @Nonnull @MustNotContainNull final Topic... topics) {
    if (lockIfNotDisposed()) {
      try {
        endEdit(false);

        final List<ModelAwarePlugin> plugins = MindMapPluginRegistry.getInstance().findFor(ModelAwarePlugin.class);

        boolean allowed = true;
        if (!force) {
          for (final MindMapListener l : this.mindMapListeners) {
            allowed &= l.allowedRemovingOfTopics(this, topics);
          }
        }

        if (allowed) {
          removeAllSelection();
          for (final Topic t : topics) {
            for (final ModelAwarePlugin p : plugins) {
              p.onDeleteTopic(this, t);
            }
            this.model.removeTopic(t);
          }
          updateView(true);
        }
      } finally {
        unlock();
      }
    }
  }

  public void collapseOrExpandAll(final boolean collapse) {
    if (this.lockIfNotDisposed()) {
      try {
        endEdit(false);
        removeAllSelection();

        if (this.model.getRoot() != null) {
          final AbstractElement root = (AbstractElement) assertNotNull(this.model.getRoot()).getPayload();
          if (root != null && root.collapseOrExpandAllChildren(collapse)) {
            updateView(true);
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  @Nullable
  public Topic deleteSelectedTopics(final boolean force) {
    Topic nextToFocus = null;
    if (this.lockIfNotDisposed()) {
      try {
        if (!this.selectedTopics.isEmpty()) {
          if (this.selectedTopics.size() == 1) {
            nextToFocus = this.selectedTopics.get(0).getParent();
          }

          deleteTopics(force, this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]));
        }
      } finally {
        this.unlock();
      }
    }
    return nextToFocus;
  }

  public boolean hasSelectedTopics() {
    if (this.lockIfNotDisposed()) {
      try {
        return !this.selectedTopics.isEmpty();
      } finally {
        this.unlock();
      }
    } else {
      return false;
    }
  }

  public boolean hasOnlyTopicSelected() {
    if (this.lockIfNotDisposed()) {
      try {
        return this.selectedTopics.size() == 1;
      } finally {
        this.unlock();
      }
    } else {
      return false;
    }
  }

  public void removeFromSelection(@Nonnull final Topic t) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.selectedTopics.contains(t)) {
          if (this.selectedTopics.remove(t)) {
            fireNotificationSelectionChanged();
          }
          repaint();
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void select(@Nullable final Topic t, final boolean removeIfPresented) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.controller.isSelectionAllowed(this) && t != null) {
          if (!this.selectedTopics.contains(t)) {
            if (this.selectedTopics.add(t)) {
              fireNotificationSelectionChanged();
            }
            fireNotificationEnsureTopicVisibility(t);
            repaint();
          } else if (removeIfPresented) {
            removeFromSelection(t);
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  @Nonnull
  @MustNotContainNull
  public Topic[] getSelectedTopics() {
    this.lock();
    try {
      return this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]);
    } finally {
      this.unlock();
    }
  }

  public void updateEditorAfterResizing() {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.elementUnderEdit != null) {
          final AbstractElement element = this.elementUnderEdit;
          final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
          this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
          this.textEditor.setMinimumSize(textBlockSize);
          this.textEditorPanel.setVisible(true);
          this.textEditor.requestFocus();
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void hideEditor() {
    if (this.lockIfNotDisposed()) {
      try {
        this.textEditorPanel.setVisible(false);
        this.elementUnderEdit = null;
      } finally {
        this.unlock();
      }
    }
  }

  public void endEdit(final boolean commit) {
    if (this.lockIfNotDisposed()) {
      try {
        if (commit && this.elementUnderEdit != null) {
          this.pathToPrevTopicBeforeEdit = null;
          final AbstractElement editedElement = this.elementUnderEdit;
          final Topic editedTopic = this.elementUnderEdit.getModel();

          final String oldText = editedElement.getText();
          final String newText = this.textEditor.getText();
          if (!oldText.equals(newText)) {
            editedElement.setText(newText);
          }
          this.textEditorPanel.setVisible(false);
          updateView(true);
          fireNotificationEnsureTopicVisibility(editedTopic);
        }
      } finally {
        try {
          this.elementUnderEdit = null;
          this.textEditorPanel.setVisible(false);
          this.requestFocus();
        } finally {
          this.unlock();
        }
      }
    }
  }

  public void startEdit(@Nullable final AbstractElement element) {
    if (this.lockIfNotDisposed()) {
      try {
        if (element == null) {
          this.elementUnderEdit = null;
          this.textEditorPanel.setVisible(false);
        } else {
          this.elementUnderEdit = element;
          element.fillByTextAndFont(this.textEditor);
          final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
          this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
          this.textEditor.setMinimumSize(textBlockSize);

          ensureVisibility(this.elementUnderEdit);

          this.textEditorPanel.setVisible(true);
          this.textEditor.requestFocus();
        }
      } finally {
        this.unlock();
      }
    }
  }

  private void findDestinationElementForDragged() {
    final Topic theroot = this.model.getRoot();
    if (this.draggedElement != null && theroot != null) {
      final AbstractElement root = (AbstractElement) assertNotNull(theroot.getPayload());
      this.destinationElement = root.findNearestOpenedTopicToPoint(this.draggedElement.getElement(), this.draggedElement.getPosition());
    } else {
      this.destinationElement = null;
    }
  }

  protected void processPopUpForShortcut() {
    if (this.lockIfNotDisposed()) {
      try {
        final Topic topic = this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);

        if (topic != null) {
          fireNotificationEnsureTopicVisibility(topic);
        }

        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (topic == null) {
              select(getModel().getRoot(), false);
            } else {
              final AbstractElement element = (AbstractElement) topic.getPayload();
              if (element != null) {
                final Rectangle2D bounds = element.getBounds();
                processPopUp(new Point((int) Math.round(bounds.getCenterX()), (int) Math.round(bounds.getCenterY())), element);
              }
            }
          }
        });
      } finally {
        unlock();
      }
    }
  }

  protected void processPopUp(@Nonnull final Point point, @Nullable final AbstractElement elementUnderMouse) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.controller != null) {
          final ElementPart partUnderMouse = elementUnderMouse == null ? null : elementUnderMouse.findPartForPoint(point);

          if (elementUnderMouse != null && !this.selectedTopics.contains(elementUnderMouse.getModel())) {
            this.selectedTopics.clear();
            this.select(elementUnderMouse.getModel(), false);
          }

          final JPopupMenu menu = this.controller.makePopUpForMindMapPanel(this, point, elementUnderMouse, partUnderMouse);
          if (menu != null) {

            final MindMapPanel theInstance = this;

            menu.addPopupMenuListener(new PopupMenuListener() {
              @Override
              public void popupMenuWillBecomeVisible(@Nonnull final PopupMenuEvent e) {
                theInstance.mouseDragSelection = null;
                theInstance.popupMenuActive = true;
              }

              @Override
              public void popupMenuWillBecomeInvisible(@Nonnull final PopupMenuEvent e) {
                theInstance.mouseDragSelection = null;
                theInstance.popupMenuActive = false;
              }

              @Override
              public void popupMenuCanceled(@Nonnull final PopupMenuEvent e) {
                theInstance.mouseDragSelection = null;
                theInstance.popupMenuActive = false;
              }
            });

            menu.show(this, point.x, point.y);
          }
        }
      } finally {
        unlock();
      }
    }
  }

  public void addMindMapListener(@Nonnull final MindMapListener l) {
    if (this.lockIfNotDisposed()) {
      try {
        this.mindMapListeners.add(Assertions.assertNotNull(l));
      } finally {
        this.unlock();
      }
    }
  }

  public void removeMindMapListener(@Nonnull final MindMapListener l) {
    if (this.lockIfNotDisposed()) {
      try {
        this.mindMapListeners.remove(Assertions.assertNotNull(l));
      } finally {
        this.unlock();
      }
    }
  }

  public void setModel(@Nonnull final MindMap model) {
    this.setModel(model, false);
  }

  /**
   * Set model for the panel, allows to notify listeners optionally.
   *
   * @param model model to be set
   * @param notifyModelChangeListeners true if to notify model change listeners, false otherwise
   * @since 1.3.0
   */
  public void setModel(@Nonnull final MindMap model, final boolean notifyModelChangeListeners) {
    this.lock();
    try {
      if (this.elementUnderEdit != null) {
        Utils.safeSwingBlockingCall(new Runnable() {
          @Override
          public void run() {
            endEdit(false);
          }
        });
      }

      final List<int[]> selectedPaths = new ArrayList<int[]>();
      for (final Topic t : this.selectedTopics) {
        selectedPaths.add(t.getPositionPath());
      }

      this.selectedTopics.clear();

      final MindMap oldModel = this.model;
      this.model = assertNotNull("Model must not be null", model);

      for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(PanelAwarePlugin.class)) {
        p.onPanelModelChange(this, oldModel, this.model);
      }

      updateView(false);

      boolean selectionChanged = false;
      for (final int[] posPath : selectedPaths) {
        final Topic topic = this.model.findForPositionPath(posPath);
        if (topic == null) {
          selectionChanged = true;
        } else if (!MindMapUtils.isHidden(topic)) {
          this.selectedTopics.add(topic);
        }
      }
      if (selectionChanged) {
        fireNotificationSelectionChanged();
      }
      repaint();
    } finally {
      this.unlock();
      if (notifyModelChangeListeners) {
        notifyModelChanged();
      }
    }
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  @Nonnull
  public MindMap getModel() {
    this.lock();
    try {
      return this.model;
    } finally {
      this.unlock();
    }
  }

  public void setScale(final double zoom) {
    if (this.lockIfNotDisposed()) {
      try {
        this.config.setScale(zoom);
      } finally {
        this.unlock();
      }
    }
  }

  public double getScale() {
    this.lock();
    try {
      return this.config.getScale();
    } finally {
      this.unlock();
    }
  }

  private static void drawBackground(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg) {
    final Rectangle clipBounds = g.getClipBounds();

    if (cfg.isDrawBackground()) {
      if (clipBounds == null) {
        LOGGER.warn("Can't draw background because clip bounds is not provided!");
      } else {
        g.drawRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, null, cfg.getPaperColor());

        if (cfg.isShowGrid()) {
          final double scaledGridStep = cfg.getGridStep() * cfg.getScale();

          final float minX = clipBounds.x;
          final float minY = clipBounds.y;
          final float maxX = clipBounds.x + clipBounds.width;
          final float maxY = clipBounds.y + clipBounds.height;

          final Color gridColor = cfg.getGridColor();

          for (float x = 0.0f; x < maxX; x += scaledGridStep) {
            if (x < minX) {
              continue;
            }
            final int intx = Math.round(x);
            g.drawLine(intx, (int) minY, intx, (int) maxY, gridColor);
          }

          for (float y = 0.0f; y < maxY; y += scaledGridStep) {
            if (y < minY) {
              continue;
            }
            final int inty = Math.round(y);
            g.drawLine((int) minX, inty, (int) maxX, inty, gridColor);
          }
        }
      }
    }
  }

  private static boolean isModelValid(@Nullable final MindMap map) {
    boolean result = true;
    if (map != null) {
      final Topic root = map.getRoot();
      if (root != null) {
        result = root.getPayload() != null;
      }
    }
    return result;
  }

  public static void drawOnGraphicsForConfiguration(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig config, @Nonnull final MindMap map, final boolean drawSelection, @Nullable @MustNotContainNull final List<Topic> selectedTopics) {
    drawBackground(g, config);
    drawTopics(g, config, map);
    if (drawSelection && selectedTopics != null && !selectedTopics.isEmpty()) {
      drawSelection(g, config, selectedTopics);
    }
  }

  private void drawDestinationElement(@Nonnull final Graphics2D g, @Nonnull final MindMapPanelConfig cfg) {
    if (this.destinationElement != null && this.draggedElement != null) {
      g.setColor(new Color((cfg.getSelectLineColor().getRGB() & 0xFFFFFF) | 0x80000000, true));
      g.setStroke(new BasicStroke(this.config.safeScaleFloatValue(3.0f, 0.1f)));

      final Rectangle2D rectToDraw = new Rectangle2D.Double();
      rectToDraw.setRect(this.destinationElement.getBounds());
      final double selectLineGap = cfg.getSelectLineGap() * 3.0d * cfg.getScale();
      rectToDraw.setRect(rectToDraw.getX() - selectLineGap, rectToDraw.getY() - selectLineGap, rectToDraw.getWidth() + selectLineGap * 2, rectToDraw.getHeight() + selectLineGap * 2);

      final int position = calcDropPosition(this.destinationElement, this.draggedElement.getPosition());

      boolean draw = !this.draggedElement.isPositionInside() && !this.destinationElement.getModel().hasAncestor(this.draggedElement.getElement().getModel());

      switch (this.draggedElement.getModifier()) {
        case NONE: {
          switch (position) {
            case DRAG_POSITION_TOP: {
              rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY(), rectToDraw.getWidth(), rectToDraw.getHeight() / 2);
            }
            break;
            case DRAG_POSITION_BOTTOM: {
              rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY() + rectToDraw.getHeight() / 2, rectToDraw.getWidth(), rectToDraw.getHeight() / 2);
            }
            break;
            case DRAG_POSITION_LEFT: {
              rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY(), rectToDraw.getWidth() / 2, rectToDraw.getHeight());
            }
            break;
            case DRAG_POSITION_RIGHT: {
              rectToDraw.setRect(rectToDraw.getX() + rectToDraw.getWidth() / 2, rectToDraw.getY(), rectToDraw.getWidth() / 2, rectToDraw.getHeight());
            }
            break;
            default:
              draw = false;
              break;
          }
        }
        break;
        case MAKE_JUMP: {
        }
        break;
        default:
          throw new Error("Unexpected state " + this.draggedElement.getModifier());
      }

      if (draw) {
        g.fill(rectToDraw);
      }
    }
  }

  private static void drawSelection(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, @Nullable @MustNotContainNull final List<Topic> selectedTopics) {
    if (selectedTopics != null && !selectedTopics.isEmpty()) {
      final Color selectLineColor = cfg.getSelectLineColor();
      g.setStroke(cfg.safeScaleFloatValue(cfg.getSelectLineWidth(), 0.1f), StrokeType.DASHES);
      final double selectLineGap = (double) cfg.safeScaleFloatValue(cfg.getSelectLineGap(), 0.05f);
      final double selectLineGapX2 = selectLineGap + selectLineGap;

      for (final Topic s : selectedTopics) {
        final AbstractElement e = (AbstractElement) s.getPayload();
        if (e != null) {
          final int x = (int) Math.round(e.getBounds().getX() - selectLineGap);
          final int y = (int) Math.round(e.getBounds().getY() - selectLineGap);
          final int w = (int) Math.round(e.getBounds().getWidth() + selectLineGapX2);
          final int h = (int) Math.round(e.getBounds().getHeight() + selectLineGapX2);
          g.drawRect(x, y, w, h, selectLineColor, null);
        }
      }
    }
  }

  private static void drawTopics(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, @Nullable final MindMap map) {
    if (map != null) {
      if (Boolean.parseBoolean(map.getAttribute(ATTR_SHOW_JUMPS))) {
        drawJumps(g, map, cfg);
      }

      final Topic root = map.getRoot();
      if (root != null) {
        drawTopicTree(g, root, cfg);
      }
    }
  }

  private static double findLineAngle(final double sx, final double sy, final double ex, final double ey) {
    final double deltax = ex - sx;
    if (deltax == 0.0d) {
      return Math.PI / 2;
    }
    return Math.atan((ey - sy) / deltax) + (ex < sx ? Math.PI : 0);
  }

  private static void drawJumps(@Nonnull final MMGraphics gfx, @Nonnull final MindMap map, @Nonnull final MindMapPanelConfig cfg) {
    final List<Topic> allTopicsWithJumps = map.findAllTopicsForExtraType(Extra.ExtraType.TOPIC);

    final float scaledSize = cfg.safeScaleFloatValue(cfg.getJumpLinkWidth(), 0.1f);

    final float lineWidth = scaledSize;
    final float arrowWidth = cfg.safeScaleFloatValue(cfg.getJumpLinkWidth() * 1.0f, 0.3f);

    final Color jumpLinkColor = cfg.getJumpLinkColor();

    final float arrowSize = cfg.safeScaleFloatValue(10.0f * cfg.getJumpLinkWidth(), 0.2f);

    for (Topic src : allTopicsWithJumps) {
      final ExtraTopic extra = (ExtraTopic) assertNotNull(assertNotNull(src).getExtras()).get(Extra.ExtraType.TOPIC);

      src = MindMapUtils.isHidden(src) ? MindMapUtils.findFirstVisibleAncestor(src) : src;

      if (extra != null) {
        Topic dst = map.findTopicForLink(extra);
        if (dst != null) {
          if (MindMapUtils.isHidden(dst)) {
            dst = MindMapUtils.findFirstVisibleAncestor(dst);
            if (dst == src) {
              dst = null;
            }
          }

          if (dst != null) {
            final AbstractElement dstElement = (AbstractElement) dst.getPayload();
            if (!MindMapUtils.isHidden(dst) && dstElement != null) {
              final AbstractElement srcElement = assertNotNull((AbstractElement) assertNotNull(src).getPayload());
              final Rectangle2D srcRect = srcElement.getBounds();
              final Rectangle2D dstRect = dstElement.getBounds();
              drawArrowToDestination(gfx, srcRect, dstRect, lineWidth, arrowWidth, arrowSize, jumpLinkColor);
            }
          }
        }
      }
    }
  }

  private static void drawArrowToDestination(@Nonnull final MMGraphics gfx, @Nonnull final Rectangle2D start, @Nonnull final Rectangle2D destination, @Nonnull final float lineWidth, @Nonnull final float arrowWidth, final float arrowSize, @Nonnull final Color color) {

    final double startx = start.getCenterX();
    final double starty = start.getCenterY();

    final Point2D arrowPoint = Utils.findRectEdgeIntersection(destination, startx, starty);

    if (arrowPoint != null) {
      gfx.setStroke(lineWidth, StrokeType.SOLID);

      double angle = findLineAngle(arrowPoint.getX(), arrowPoint.getY(), startx, starty);

      final double arrowAngle = Math.PI / 12.0d;

      final double x1 = arrowSize * Math.cos(angle - arrowAngle);
      final double y1 = arrowSize * Math.sin(angle - arrowAngle);
      final double x2 = arrowSize * Math.cos(angle + arrowAngle);
      final double y2 = arrowSize * Math.sin(angle + arrowAngle);

      final double cx = (arrowSize / 2.0f) * Math.cos(angle);
      final double cy = (arrowSize / 2.0f) * Math.sin(angle);

      final GeneralPath polygon = new GeneralPath();
      polygon.moveTo(arrowPoint.getX(), arrowPoint.getY());
      polygon.lineTo(arrowPoint.getX() + x1, arrowPoint.getY() + y1);
      polygon.lineTo(arrowPoint.getX() + x2, arrowPoint.getY() + y2);
      polygon.closePath();
      gfx.draw(polygon, null, color);

      gfx.setStroke(lineWidth, StrokeType.DOTS);
      gfx.drawLine((int) startx, (int) starty, (int) (arrowPoint.getX() + cx), (int) (arrowPoint.getY() + cy), color);
    }
  }

  private static void drawTopicTree(@Nonnull final MMGraphics gfx, @Nonnull final Topic topic, @Nonnull final MindMapPanelConfig cfg) {
    paintTopic(gfx, topic, cfg);
    final AbstractElement w = assertNotNull((AbstractElement) topic.getPayload());
    if (w.isCollapsed()) {
      return;
    }
    for (final Topic t : topic.getChildren()) {
      drawTopicTree(gfx, t, cfg);
    }
  }

  private static void paintTopic(@Nonnull final MMGraphics gfx, @Nonnull final Topic topic, @Nonnull final MindMapPanelConfig cfg) {
    final AbstractElement element = (AbstractElement) topic.getPayload();
    if (element != null) {
      element.doPaint(gfx, cfg, true);
    }
  }

  private static void setElementSizesForElementAndChildren(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig cfg, @Nonnull final Topic topic, final int level) {
    AbstractElement widget = (AbstractElement) topic.getPayload();
    if (widget == null) {
      switch (level) {
        case 0:
          widget = new ElementRoot(topic);
          break;
        case 1:
          widget = new ElementLevelFirst(topic);
          break;
        default:
          widget = new ElementLevelOther(topic);
          break;
      }
      topic.setPayload(widget);
    }

    widget.updateElementBounds(gfx, cfg);
    for (final Topic t : topic.getChildren()) {
      setElementSizesForElementAndChildren(gfx, cfg, t, level + 1);
    }
    widget.updateBlockSize(cfg);
  }

  public static boolean calculateElementSizes(@Nonnull final MMGraphics gfx, @Nullable final MindMap model, @Nonnull final MindMapPanelConfig cfg) {
    boolean result = false;

    final Topic root = model == null ? null : model.getRoot();
    if (root != null && model != null) {
      model.resetPayload();
      setElementSizesForElementAndChildren(gfx, cfg, root, 0);
      result = true;
    }
    return result;
  }

  @Nullable
  public static Dimension2D layoutModelElements(@Nullable final MindMap model, @Nonnull final MindMapPanelConfig cfg) {
    Dimension2D result = null;
    if (model != null) {
      final Topic rootTopic = model.getRoot();
      if (rootTopic != null) {
        final AbstractElement root = (AbstractElement) rootTopic.getPayload();
        if (root != null) {
          root.alignElementAndChildren(cfg, true, 0, 0);
          result = root.getBlockSize();
        }
      }
    }
    return result;
  }

  protected static void moveDiagram(@Nullable final MindMap model, final double deltaX, final double deltaY) {
    if (model != null) {
      final Topic root = model.getRoot();
      if (root != null) {
        final AbstractElement element = (AbstractElement) root.getPayload();
        if (element != null) {
          element.moveWholeTreeBranchCoordinates(deltaX, deltaY);
        }
      }
    }
  }

  private void changeSizeOfComponentWithNotification(@Nullable final Dimension size) {
    if (size != null) {
      setMinimumSize(size);
      setPreferredSize(size);
      for (final MindMapListener l : this.mindMapListeners) {
        l.onMindMapModelRealigned(this, size);
      }
    }
  }

  @Nullable
  public static Dimension layoutFullDiagramWithCenteringToPaper(@Nonnull final MMGraphics gfx, @Nonnull final MindMap map, @Nonnull final MindMapPanelConfig cfg, @Nonnull final Dimension2D paperSize) {
    Dimension resultSize = null;
    if (calculateElementSizes(gfx, map, cfg)) {
      Dimension2D rootBlockSize = layoutModelElements(map, cfg);
      final double paperMargin = cfg.getPaperMargins() * cfg.getScale();

      if (rootBlockSize != null) {
        final ElementRoot rootElement = assertNotNull((ElementRoot) assertNotNull(map.getRoot()).getPayload());

        double rootOffsetXInBlock = rootElement.getLeftBlockSize().getWidth();
        double rootOffsetYInBlock = (rootBlockSize.getHeight() - rootElement.getBounds().getHeight()) / 2;

        rootOffsetXInBlock += paperSize.getWidth() - rootBlockSize.getWidth() <= paperMargin ? paperMargin : (paperSize.getWidth() - rootBlockSize.getWidth()) / 2;
        rootOffsetYInBlock += paperSize.getHeight() - rootBlockSize.getHeight() <= paperMargin ? paperMargin : (paperSize.getHeight() - rootBlockSize.getHeight()) / 2;

        moveDiagram(map, rootOffsetXInBlock, rootOffsetYInBlock);
        resultSize = new Dimension((int) Math.round(rootBlockSize.getWidth() + paperMargin * 2), (int) Math.round(rootBlockSize.getHeight() + paperMargin * 2));
      }
    }

    return resultSize;
  }

  public void updateView(final boolean structureWasChanged) {
    if (this.lockIfNotDisposed()) {
      try {
        invalidate();
        revalidate();
        if (structureWasChanged) {
          fireNotificationMindMapChanged();
        }
        repaint();
      } finally {
        this.unlock();
      }
    }
  }

  @Override
  public void revalidate() {
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (!isValid()) {
          if (lockIfNotDisposed()) {
            try {
              final Graphics2D graph = (Graphics2D) getGraphics();
              if (graph != null) {
                final MMGraphics gfx = new MMGraphics2DWrapper(graph);
                if (calculateElementSizes(gfx, model, config)) {
                  changeSizeOfComponentWithNotification(layoutFullDiagramWithCenteringToPaper(gfx, model, config, getSize()));
                }
              }
            } finally {
              unlock();
            }
          }
        }
      }
    };
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public void setErrorText(@Nullable final String text) {
    if (this.lockIfNotDisposed()) {
      try {
        this.errorText = text;
        repaint();
      } finally {
        this.unlock();
      }
    }
  }

  @Nullable
  public String getErrorText() {
    return this.errorText;
  }

  @Override
  public boolean isValid() {
    if (this.lockIfNotDisposed()) {
      try {
        return isModelValid(this.model);
      } finally {
        this.unlock();
      }
    }
    return false;
  }

  @Override
  public boolean isValidateRoot() {
    return true;
  }

  @Override
  public void invalidate() {
    if (lockIfNotDisposed()) {
      try {
        super.invalidate();
        if (this.model != null && this.model.getRoot() != null) {
          this.model.resetPayload();
        }
      } finally {
        this.unlock();
      }
    }
  }

  private static void drawErrorText(@Nonnull final Graphics2D gfx, @Nonnull final Dimension fullSize, @Nonnull final String error) {
    final Font font = new Font(Font.DIALOG, Font.BOLD, 24);
    final FontMetrics metrics = gfx.getFontMetrics(font);
    final Rectangle2D textBounds = metrics.getStringBounds(error, gfx);
    gfx.setFont(font);
    gfx.setColor(Color.DARK_GRAY);
    gfx.fillRect(0, 0, fullSize.width, fullSize.height);
    final int x = (int) (fullSize.width - textBounds.getWidth()) / 2;
    final int y = (int) (fullSize.height - textBounds.getHeight()) / 2;
    gfx.setColor(Color.BLACK);
    gfx.drawString(error, x + 5, y + 5);
    gfx.setColor(Color.RED.brighter());
    gfx.drawString(error, x, y);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void paintComponent(@Nonnull final Graphics g) {
    if (this.lockIfNotDisposed()) {
      try {
        final Graphics2D gfx = (Graphics2D) g.create();
        try {
          final String error = this.errorText;

          Utils.prepareGraphicsForQuality(gfx);
          if (error != null) {
            drawErrorText(gfx, this.getSize(), error);
          } else {
            revalidate();
            drawOnGraphicsForConfiguration(new MMGraphics2DWrapper(gfx), this.config, this.model, true, this.selectedTopics);
            drawDestinationElement(gfx, this.config);
          }

          paintChildren(g);

          if (this.draggedElement != null) {
            this.draggedElement.draw(gfx);
          } else if (this.mouseDragSelection != null) {
            gfx.setColor(COLOR_MOUSE_DRAG_SELECTION);
            gfx.fill(this.mouseDragSelection.asRectangle());
          }
        } finally {
          gfx.dispose();
        }
      } finally {
        this.unlock();
      }
    }
  }

  @Nullable
  public AbstractElement findTopicUnderPoint(@Nonnull final Point point) {
    if (this.lockIfNotDisposed()) {
      try {
        AbstractElement result = null;
        if (this.model != null) {
          final Topic root = this.model.getRoot();
          if (root != null) {
            final AbstractElement rootWidget = (AbstractElement) root.getPayload();
            if (rootWidget != null) {
              result = rootWidget.findForPoint(point);
            }
          }
        }

        return result;
      } finally {
        this.unlock();
      }
    }
    return null;
  }

  public void removeAllSelection() {
    if (this.lockIfNotDisposed()) {
      try {
        if (!this.selectedTopics.isEmpty()) {
          try {
            this.selectedTopics.clear();
            fireNotificationSelectionChanged();
          } finally {
            repaint();
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void focusTo(@Nullable final Topic theTopic) {
    if (this.lockIfNotDisposed()) {
      try {
        if (theTopic != null) {
          final AbstractElement element = (AbstractElement) theTopic.getPayload();
          if (element != null && element instanceof AbstractCollapsableElement) {
            final AbstractCollapsableElement cel = (AbstractCollapsableElement) element;
            if (MindMapUtils.ensureVisibility(cel.getModel())) {
              updateView(true);
            }
          }

          removeAllSelection();

          final int[] path = theTopic.getPositionPath();
          this.select(this.model.findForPositionPath(path), false);
        }
      } finally {
        this.unlock();
      }
    }
  }

  public boolean cloneTopic(@Nullable final Topic topic) {
    return this.cloneTopic(topic, true);
  }

  public boolean cloneTopic(@Nullable final Topic topic, final boolean cloneSubtree) {
    this.lock();
    try {
      if (topic == null || topic.getTopicLevel() == 0) {
        return false;
      }

      final Topic cloned = this.model.cloneTopic(topic, cloneSubtree);

      if (cloned != null) {
        cloned.moveAfter(topic);
        updateView(true);
      }

      return true;
    } finally {
      this.unlock();
    }
  }

  @Nonnull
  public MindMapPanelConfig getConfiguration() {
    this.lock();
    try {
      return this.config;
    } finally {
      this.unlock();
    }
  }

  @Nonnull
  public MindMapPanelController getController() {
    this.lock();
    try {
      return this.controller;
    } finally {
      this.unlock();
    }
  }

  @Nullable
  public Topic getFirstSelected() {
    if (this.lockIfNotDisposed()) {
      try {
        return this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);
      } finally {
        this.unlock();
      }
    } else {
      return null;
    }
  }

  @Nullable
  public static Dimension2D calculateSizeOfMapInPixels(@Nonnull final MindMap model, @Nonnull final MindMapPanelConfig cfg, final boolean expandAll) {
    final MindMap workMap = new MindMap(model, null);
    workMap.resetPayload();

    BufferedImage img = new BufferedImage(32, 32, cfg.isDrawBackground() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
    Dimension2D blockSize = null;
    final Graphics2D g = img.createGraphics();
    final MMGraphics gfx = new MMGraphics2DWrapper(g);
    try {
      Utils.prepareGraphicsForQuality(g);
      if (calculateElementSizes(gfx, workMap, cfg)) {
        if (expandAll) {
          final AbstractElement root = assertNotNull((AbstractElement) assertNotNull(workMap.getRoot()).getPayload());
          root.collapseOrExpandAllChildren(false);
          calculateElementSizes(gfx, workMap, cfg);
        }
        blockSize = assertNotNull(layoutModelElements(workMap, cfg));
        final double paperMargin = cfg.getPaperMargins() * cfg.getScale();
        blockSize.setSize(blockSize.getWidth() + paperMargin * 2, blockSize.getHeight() + paperMargin * 2);
      }
    } finally {
      gfx.dispose();
    }
    return blockSize;
  }

  @Nullable
  public static BufferedImage renderMindMapAsImage(@Nonnull final MindMap model, @Nonnull final MindMapPanelConfig cfg, final boolean expandAll) {
    final MindMap workMap = new MindMap(model, null);
    workMap.resetPayload();

    if (expandAll) {
      MindMapUtils.removeCollapseAttr(workMap);
    }

    final Dimension2D blockSize = calculateSizeOfMapInPixels(workMap, cfg, expandAll);
    if (blockSize == null) {
      return null;
    }

    final BufferedImage img = new BufferedImage((int) blockSize.getWidth(), (int) blockSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = img.createGraphics();
    final MMGraphics gfx = new MMGraphics2DWrapper(g);
    try {
      Utils.prepareGraphicsForQuality(g);
      gfx.setClip(0, 0, img.getWidth(), img.getHeight());
      layoutFullDiagramWithCenteringToPaper(gfx, workMap, cfg, blockSize);
      drawOnGraphicsForConfiguration(gfx, cfg, workMap, false, null);
    } finally {
      gfx.dispose();
    }
    return img;
  }

  public boolean isLocked() {
    return this.panelLocker == null ? false : this.panelLocker.isLocked();
  }

  /**
   * Try lock the panel if it is not disposed.
   *
   * @return true if the panel is locked successfully, false if the panel has been disposed.
   */
  public boolean lockIfNotDisposed() {
    boolean result = false;
    if (this.panelLocker != null) {
      this.panelLocker.lock();
      if (this.disposed.get()) {
        this.panelLocker.unlock();
      } else {
        result = true;
      }
    }
    return result;
  }

  /**
   * Lock the panel.
   *
   * @return the panel
   * @throws IllegalStateException it will be thrown if the panel is disposed
   */
  @Nonnull
  public MindMapPanel lock() {
    if (this.panelLocker != null) {
      this.panelLocker.lock();
      if (this.isDisposed()) {
        this.panelLocker.unlock();
        throw new IllegalStateException("Mind map has been already disposed!");
      }
    }
    return this;
  }

  /**
   * Unlock the panel. it will not throw any exception if the panel is disposed.
   *
   * @throws AssertionError if the panel is locked by another thread
   */
  public void unlock() {
    if (this.panelLocker != null) {
      Assertions.assertTrue("Panel must be held by the current thread", this.panelLocker.isHeldByCurrentThread());
      this.panelLocker.unlock();
    }
  }

  public boolean isDisposed() {
    return this.disposed.get();
  }

  public void dispose() {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.disposed.compareAndSet(false, true)) {
          this.weakTable.clear();
          this.selectedTopics.clear();
          this.mindMapListeners.clear();

          for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(PanelAwarePlugin.class)) {
            p.onPanelDispose(this);
          }
        }
      } finally {
        this.unlock();
      }
    }
  }
}
