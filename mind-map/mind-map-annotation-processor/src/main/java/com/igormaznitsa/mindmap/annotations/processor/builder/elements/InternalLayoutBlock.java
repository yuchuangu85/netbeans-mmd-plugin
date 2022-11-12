package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findAmongEnclosingAndAncestors;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.COLOR_BORDER;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.COLOR_FILL;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.COLOR_TEXT;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.EMOTICON;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.LEFT_SIDE;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.TOPIC_LINK_UID;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.fillAttributesWithoutFileAndTopicLinks;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.setTopicDirection;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.Direction;
import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

final class InternalLayoutBlock {
  private final TopicItem baseItem;
  private final boolean autoCreated;
  private final String forceTitle;
  private InternalLayoutBlock parent;
  private Topic topic;

  InternalLayoutBlock(final TopicItem baseItem) {
    this(baseItem, null, false);
  }

  InternalLayoutBlock(
      final TopicItem baseItem,
      final String forceTitle,
      final boolean autoCreated) {
    this.baseItem = baseItem;
    this.forceTitle = forceTitle;
    this.autoCreated = autoCreated;
  }

  public boolean isAutocreated() {
    return this.autoCreated;
  }

  boolean isLinked() {
    return this.parent != null;
  }

  public String findAnyPossibleUid() {
    if (this.autoCreated) {
      return this.forceTitle;
    } else {
      if (StringUtils.isBlank(this.baseItem.asMmdTopicAnnotation().uid())) {
        final String uid = this.baseItem.asMmdTopicAnnotation().uid();
        final String title = this.baseItem.asMmdTopicAnnotation().title();
        final String elementName =
            this.baseItem.getElement().getSimpleName().toString();

        if (StringUtils.isNotBlank(uid)) {
          return uid;
        }
        if (StringUtils.isNotBlank(title)) {
          return uid;
        }
        return elementName;
      } else {
        return this.baseItem.asMmdTopicAnnotation().uid();
      }
    }
  }

  public InternalLayoutBlock getParent() {
    return this.parent;
  }

  public void setParent(final InternalLayoutBlock parent) {
    this.parent = parent;
  }

  private String findTitle() {
    if (this.forceTitle != null) {
      return this.forceTitle;
    }
    if (this.getAnnotation().title().length() == 0) {
      return this.baseItem.getElement().getSimpleName().toString();
    } else {
      return this.getAnnotation().title();
    }
  }

  public Topic findOrCreateTopic(final MindMap mindMap) {
    if (this.topic == null) {
      Topic parentTopic = null;
      InternalLayoutBlock current = this.parent;
      while (current != null) {
        final Topic found = current.findOrCreateTopic(mindMap);
        if (parentTopic == null) {
          parentTopic = found;
        }
        current = current.parent;
      }
      this.topic =
          new Topic(
              mindMap, parentTopic == null ? mindMap.getRoot() : parentTopic, this.findTitle());
      if (this.autoCreated) {
        this.topic.setText(this.forceTitle);
      }
    }
    return this.topic;
  }

  public MmdTopic getAnnotation() {
    return this.baseItem.asAnnotation();
  }

  public TopicItem getAnnotationItem() {
    return this.baseItem;
  }

  public InternalLayoutBlock[] findPath() {
    if (!(this.parent == null)) {
      return ArrayUtils.add(this.parent.findPath(), this);
    } else {
      return new InternalLayoutBlock[] {this};
    }
  }

  @Override
  public String toString() {
    return "LayoutBlock"
        + "autoCreated="
        + this.autoCreated
        + ", title='"
        + this.findTitle()
        + '\''
        + '}';
  }

  @Override
  public int hashCode() {
    return this.baseItem.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof InternalLayoutBlock) {
      return Objects.equals(this.baseItem, ((InternalLayoutBlock) that).baseItem);
    }
    return false;
  }

  public Optional<InternalLayoutBlock> findParentAmong(Types types,
                                                       List<InternalLayoutBlock> children) {
    final List<Pair<MmdTopic, Element>> found =
        findAmongEnclosingAndAncestors(this.baseItem.getElement(), MmdTopic.class, types);
    return found.stream()
        .flatMap(x ->
            children.stream()
                .filter(y ->
                    !this.baseItem.getElement().equals(y.baseItem.getElement())
                        && y.baseItem.getElement().equals(x.getValue())))
        .findFirst();
  }

  public void processTopicAttributes() throws URISyntaxException {
    if (this.baseItem.asMmdTopicAnnotation().direction()
        == Direction.LEFT) {
      InternalLayoutBlock root = this;
      while (root.getParent() != null) {
        root = root.getParent();
      }
      setTopicDirection(root.topic, Direction.LEFT);
    }

    this.fillAttributesWithParentAwareness(this.topic);
  }

  private void fillAttributesWithParentAwareness(final Topic topic) throws URISyntaxException {
    fillAttributesWithoutFileAndTopicLinks(topic,
        this.baseItem.getElement(),
        this.baseItem.asMmdTopicAnnotation());

    if (this.autoCreated) {
      topic.setText(this.forceTitle);
      topic.removeAttributes(false, TOPIC_LINK_UID.getId(), EMOTICON.getId(), LEFT_SIDE.getId());
    }

    final InternalLayoutBlock[] topicPath = this.findPath();

    MmdColor colorText =
        this.baseItem.asMmdTopicAnnotation().colorText();
    MmdColor colorBorder =
        this.baseItem.asMmdTopicAnnotation().colorBorder();
    MmdColor colorFill =
        this.baseItem.asMmdTopicAnnotation().colorFill();

    for (int i = topicPath.length - 1; i >= 0; i--) {
      final InternalLayoutBlock next = topicPath[i];
      if (colorText == MmdColor.DEFAULT) {
        colorText = next.getAnnotationItem().asMmdTopicAnnotation().colorText();
      }
      if (colorFill == MmdColor.DEFAULT) {
        colorFill = next.getAnnotationItem().asMmdTopicAnnotation().colorFill();
      }
      if (colorBorder == MmdColor.DEFAULT) {
        colorBorder = next.getAnnotationItem().asMmdTopicAnnotation().colorBorder();
      }
    }

    if (colorText != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_TEXT.getId(), colorText.getHtmlColor());
    }
    if (colorFill != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_FILL.getId(), colorFill.getHtmlColor());
    }
    if (colorBorder != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_BORDER.getId(), colorBorder.getHtmlColor());
    }
  }
}
