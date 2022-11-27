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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import java.awt.Component;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public final class SafeDeletePanel extends javax.swing.JPanel implements CustomRefactoringPanel {
  public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  private static final long serialVersionUID = 8032492831487495590L;

  private final AtomicBoolean initialized = new AtomicBoolean();
  
  private final Lookup lookup;
  private final FileObject [] files;
  
  public SafeDeletePanel(final Lookup lookup, final FileObject [] files) {
    initComponents();
    this.files = files;
    this.lookup = lookup;
  }

  @Override
  public void initialize() {
    if (this.initialized.compareAndSet(false, true)){
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (files.length>1){
            labelText.setText(String.format(BUNDLE.getString("SafeDeletePanel.multiFile"), Integer.toString(files.length)));
          }else{
            labelText.setText(String.format(BUNDLE.getString("SafeDeletePanel.onlyFile"), files[0].getNameExt()));
          }
          
          if (!panelScope.initialize(lookup, new AtomicBoolean())) {
            labelScope.setVisible(false);
            panelScope.setVisible(false);
          }
          else {
            labelScope.setVisible(true);
            panelScope.setVisible(true);
          }
        }
      });
    }
  }

  @Override
  public Component getComponent() {
    return  this;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    labelText = new javax.swing.JLabel();
    panelScope = new org.netbeans.modules.refactoring.spi.ui.ScopePanel(SafeDeletePanel.class.getCanonicalName().replace('.', '-'),NbPreferences.forModule(SafeDeletePanel.class),"safeDelete.scope");
    labelScope = new javax.swing.JLabel();

    labelText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/logo/logo16.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelText, bundle.getString("SafeDeletePanel.labelText.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(labelScope, bundle.getString("SafeDeletePanel.labelScope.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(labelText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(labelScope)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(labelText)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(labelScope)
          .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(19, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel labelScope;
  private javax.swing.JLabel labelText;
  private org.netbeans.modules.refactoring.spi.ui.ScopePanel panelScope;
  // End of variables declaration//GEN-END:variables
}
