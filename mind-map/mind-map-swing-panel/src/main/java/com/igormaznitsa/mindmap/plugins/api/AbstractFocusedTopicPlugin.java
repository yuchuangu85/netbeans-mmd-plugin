/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.Topic;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * Auxiliary class to create plug-ins working with selected topic.
 *
 * @since 1.2
 */
public abstract class AbstractFocusedTopicPlugin extends AbstractPopupMenuItem {

  @Override
  public JMenuItem makeMenuItem(
      final PluginContext context,
      final Topic activeTopic) {

    final JMenuItem result =
        UI_COMPO_FACTORY.makeMenuItem(getName(context, activeTopic), getIcon(context, activeTopic));

    result.setToolTipText(getReference());

    result.addActionListener(e -> {
      if (AbstractFocusedTopicPlugin.this instanceof ExternallyExecutedPlugin) {
        context.processPluginActivation((ExternallyExecutedPlugin) AbstractFocusedTopicPlugin.this,
            activeTopic);
      } else {
        doActionForTopic(context, activeTopic);
      }
    });
    return result;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return true;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  protected abstract Icon getIcon(PluginContext context, Topic activeTopic);

  protected abstract String getName(PluginContext context, Topic activeTopic);

  protected String getReference() {
    return null;
  }

  @Override
  public boolean isEnabled(final PluginContext context, final Topic activeTopic) {
    return context.getSelectedTopics().length == 1 ||
        (context.getSelectedTopics().length == 0 && activeTopic != null);
  }

  protected void doActionForTopic(PluginContext context, Topic actionTopic) {
    if (this instanceof ExternallyExecutedPlugin) {
      context.processPluginActivation((ExternallyExecutedPlugin) this, actionTopic);
    }
  }

}
