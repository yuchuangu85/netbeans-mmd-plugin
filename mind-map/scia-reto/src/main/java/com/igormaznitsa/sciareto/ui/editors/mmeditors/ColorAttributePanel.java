/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import java.awt.Color;

public final class ColorAttributePanel extends javax.swing.JPanel {

  private static final long serialVersionUID = -7468734011400244324L;

  public static class Result {

    private final Color borderColor;
    private final Color textColor;
    private final Color fillColor;

    private Result(final Color border, final Color text, final Color fill) {
      this.borderColor = border;
      this.textColor = text;
      this.fillColor = fill;
    }

    public Color getTextColor() {
      return this.textColor;
    }

    public Color getFillColor() {
      return this.fillColor;
    }

    public Color getBorderColor() {
      return this.borderColor;
    }
  }

  public ColorAttributePanel(final MindMap map, final Color border, final Color fill, final Color text) {
    initComponents();
    this.colorChooserBorder.setValue(border);
    this.colorChooserFill.setValue(fill);
    this.colorChooserText.setValue(text);
    
    this.colorChooserBorder.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.BORDER));
    this.colorChooserFill.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.FILL));
    this.colorChooserText.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.TEXT));
  }

  public Result getResult() {
    return new Result(
        this.colorChooserBorder.getValue(),
        this.colorChooserText.getValue(),
        this.colorChooserFill.getValue());
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        colorChooserBorder = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserFill = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserText = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        buttonResetBorder = new javax.swing.JButton();
        buttonResetFill = new javax.swing.JButton();
        buttonResetText = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
        colorChooserBorder.setText(bundle.getString("panelColorAttribute.buttonBorderColor")); // NOI18N
        colorChooserBorder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 64;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        add(colorChooserBorder, gridBagConstraints);

        colorChooserFill.setText(bundle.getString("panelColorAttribute.buttonFillColor")); // NOI18N
        colorChooserFill.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 64;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 8, 16);
        add(colorChooserFill, gridBagConstraints);

        colorChooserText.setText(bundle.getString("panelColorAttribute.buttonTextColor")); // NOI18N
        colorChooserText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 64;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 8, 16);
        add(colorChooserText, gridBagConstraints);

        buttonResetBorder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross16.png"))); // NOI18N
        buttonResetBorder.setToolTipText(bundle.getString("panelColorAttribute.buttonResetValue")); // NOI18N
        buttonResetBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetBorderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(buttonResetBorder, gridBagConstraints);

        buttonResetFill.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross16.png"))); // NOI18N
        buttonResetFill.setToolTipText(bundle.getString("panelColorAttribute.buttonResetValue")); // NOI18N
        buttonResetFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetFillActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(buttonResetFill, gridBagConstraints);

        buttonResetText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross16.png"))); // NOI18N
        buttonResetText.setToolTipText(bundle.getString("panelColorAttribute.buttonResetValue")); // NOI18N
        buttonResetText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(buttonResetText, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void buttonResetBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetBorderActionPerformed
    this.colorChooserBorder.setValue(null);
  }//GEN-LAST:event_buttonResetBorderActionPerformed

  private void buttonResetFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetFillActionPerformed
    this.colorChooserFill.setValue(null);
  }//GEN-LAST:event_buttonResetFillActionPerformed

  private void buttonResetTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetTextActionPerformed
    this.colorChooserText.setValue(null);
  }//GEN-LAST:event_buttonResetTextActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonResetBorder;
    private javax.swing.JButton buttonResetFill;
    private javax.swing.JButton buttonResetText;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserBorder;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserFill;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserText;
    // End of variables declaration//GEN-END:variables
}
