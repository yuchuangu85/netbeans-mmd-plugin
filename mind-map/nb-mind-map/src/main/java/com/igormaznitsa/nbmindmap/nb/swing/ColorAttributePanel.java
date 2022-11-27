/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.nbmindmap.nb.swing;

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

    colorChooserBorder = new com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton();
    colorChooserFill = new com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton();
    colorChooserText = new com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton();
    buttonResetBorder = new javax.swing.JButton();
    buttonResetFill = new javax.swing.JButton();
    buttonResetText = new javax.swing.JButton();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(colorChooserBorder, bundle.getString("ColorAttributePanel.colorChooserBorder.text")); // NOI18N
    colorChooserBorder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    org.openide.awt.Mnemonics.setLocalizedText(colorChooserFill, bundle.getString("ColorAttributePanel.colorChooserFill.text")); // NOI18N
    colorChooserFill.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    org.openide.awt.Mnemonics.setLocalizedText(colorChooserText, bundle.getString("ColorAttributePanel.colorChooserText.text")); // NOI18N
    colorChooserText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    buttonResetBorder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/cross16.png"))); // NOI18N
    buttonResetBorder.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetBorderActionPerformed(evt);
      }
    });

    buttonResetFill.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/cross16.png"))); // NOI18N
    buttonResetFill.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetFillActionPerformed(evt);
      }
    });

    buttonResetText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/cross16.png"))); // NOI18N
    buttonResetText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetTextActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(colorChooserText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(colorChooserFill, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(colorChooserBorder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonResetBorder, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(buttonResetFill, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(buttonResetText, javax.swing.GroupLayout.Alignment.TRAILING))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonResetBorder)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(colorChooserBorder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(colorChooserFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(buttonResetFill))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonResetText)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(colorChooserText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
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
  private com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton colorChooserBorder;
  private com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton colorChooserFill;
  private com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton colorChooserText;
  // End of variables declaration//GEN-END:variables
}
