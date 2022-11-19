package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findAllTypeElements;
import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findFirstWithAncestors;
import static java.lang.String.format;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdFiles;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MultipleFileVariantsForTopicException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Class describes one topic defined through MMD topic annotation.
 *
 * @see MmdTopic
 */
public class TopicItem extends AbstractItem {

  public TopicItem(final MmdAnnotationWrapper base) {
    super(base);
    if (!(base.asAnnotation() instanceof MmdTopic)) {
      throw new IllegalArgumentException("Expected annotation " + MmdTopic.class.getName());
    }
  }

  private static Optional<String> findFileUidAmongParentTopics(final Types typeUtils,
                                                               final Element element) {
    if (element == null) {
      return Optional.empty();
    }
    final MmdTopic topicAnnotation = element.getAnnotation(MmdTopic.class);
    if (topicAnnotation != null && StringUtils.isNotBlank(topicAnnotation.fileUid())) {
      return Optional.of(topicAnnotation.fileUid());
    } else {
      final List<Pair<MmdFileLink, Element>> foundMmdFileLinks =
          AnnotationUtils.findFirstWithAncestors(element, MmdFileLink.class, typeUtils, true);
      if (foundMmdFileLinks.isEmpty()) {
        return findFileUidAmongParentTopics(typeUtils, element.getEnclosingElement());
      } else {
        return Optional.of(foundMmdFileLinks.get(0).getKey().uid());
      }
    }
  }

  private static List<Pair<MmdFile, Element>> findTargetFileAnnotations(
      final Types types,
      final Element element)
      throws MmdElementException {
    if (element == null) {
      return List.of();
    }

    for (final Element typeElement : findAllTypeElements(element)) {
      final List<Pair<MmdFile, Element>> fileAnnotation =
          findFirstWithAncestors(typeElement, MmdFile.class, types, true);
      final List<Pair<MmdFiles, Element>> filesAnnotation =
          findFirstWithAncestors(typeElement, MmdFiles.class, types, true);
      final List<Pair<MmdFileLink, Element>> fileLinkAnnotation =
          findFirstWithAncestors(typeElement, MmdFileLink.class, types, true);

      if (!(filesAnnotation.isEmpty()
          && fileAnnotation.isEmpty()
          && fileLinkAnnotation.isEmpty())) {

        final List<Pair<MmdFile, Element>> result = new ArrayList<>(fileAnnotation);

        for (final Pair<MmdFiles, Element> files : filesAnnotation) {
          for (final MmdFile f : files.getLeft().value()) {
            result.add(Pair.of(f, files.getRight()));
          }
        }

        for (final Pair<MmdFileLink, Element> link : fileLinkAnnotation) {
          final String uid = link.getKey().uid();
          if (StringUtils.isBlank(uid)) {
            throw new MmdElementException("Element has blank file link UID", element);
          }
        }
        return result;
      }
    }
    throw new MmdElementException(
        "Can't find any associated MMD target file mark for MmdTopic annotated element",
        element);
  }

  public Optional<FileItem> findTargetFileItem(
      final Types types,
      final Map<String, FileItem> fileMap,
      final BiConsumer<String, Element> logWarning)
      throws MmdElementException, MmdAnnotationProcessorException {
    final Optional<String> fileUid = this.findFileUidAttribute(types);
    if (StringUtils.isBlank(this.asMmdTopicAnnotation().fileUid())) {
      if (fileUid.isPresent()) {
        // find for file uid
        final Optional<FileItem> foundForUid = fileUid.stream()
            .flatMap(
                x ->
                    fileMap.entrySet().stream()
                        .filter(
                            y ->
                                y.getValue().asMmdFileAnnotation().uid().equals(x))
            ).map(Map.Entry::getValue)
            .findFirst();
        if (foundForUid.isPresent()) {
          return foundForUid;
        } else {
          // try to find for provided file name as provided uid
          final List<FileItem> foundAsDefinedFileName = fileUid.stream()
              .flatMap(
                  x ->
                      fileMap.entrySet().stream()
                          .filter(
                              y ->
                                  StringUtils.isNotBlank(
                                      y.getValue().asMmdFileAnnotation().fileName())
                                      && y.getValue().asMmdFileAnnotation().fileName().equals(x)))
              .map(Map.Entry::getValue)
              .collect(Collectors.toList());
          if (!foundAsDefinedFileName.isEmpty()) {
            if (foundAsDefinedFileName.size() == 1) {
              logWarning.accept("Found MMD target file through UID as file name",
                  this.getElement());
              return Optional.of(foundAsDefinedFileName.get(0));
            } else {
              throw new MmdAnnotationProcessorException(
                  this, format(
                  "Can't find target MMD file with UID but found %d MMD file annotations with defined file name: %s",
                  foundAsDefinedFileName.size(), fileUid.get()));
            }
          } else {
            // find file item which base class name equals
            final List<FileItem> foundAsBaseName = fileUid.stream()
                .flatMap(
                    x -> fileMap.entrySet().stream()
                        .filter(
                            y -> StringUtils.isBlank(y.getValue().asMmdFileAnnotation().fileName())
                        )
                        .filter(y -> y.getValue().getBaseName().equals(x))
                )
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
            if (foundAsBaseName.isEmpty()) {
              throw new MmdAnnotationProcessorException(
                  this,
                  format("Can't find direct file for UID with non-explicit base file name: %s",
                      fileUid.get()));
            } else if (foundAsBaseName.size() == 1) {
              logWarning.accept("Found MMD target file through UID as non-explicit base file name",
                  this.getElement());
              return Optional.of(foundAsBaseName.get(0));
            } else {
              throw new MmdAnnotationProcessorException(
                  this, format(
                  "Can't find direct file for UID but found %d target files with non-explicit base file name: %s",
                  foundAsBaseName.size(), fileUid.get()));
            }
          }
        }
      } else {
        final List<Pair<MmdFile, Element>> directFileMarks =
            findTargetFileAnnotations(types, this.getElement());

        final List<FileItem> fileItems =
            directFileMarks.stream()
                .flatMap(
                    directFileMark ->
                        fileMap.entrySet().stream()
                            .filter(
                                fileMapEntry -> fileMapEntry.getValue().getElement()
                                    .equals(directFileMark.getValue())
                            )
                            .findFirst()
                            .stream())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (fileItems.size() == 1) {
          return Optional.of(fileItems.get(0));
        } else if (fileItems.size() > 1) {
          throw new MultipleFileVariantsForTopicException(this, fileItems);
        } else {
          throw new MmdAnnotationProcessorException(
              this, "There is no any defined MMD file for element");
        }
      }
    } else {
      FileItem found = fileMap.get(this.asMmdTopicAnnotation().fileUid());
      if (found == null) {
        found =
            fileMap.values().stream()
                .filter(
                    x ->
                        FilenameUtils.getName(x.asMmdFileAnnotation().fileName())
                            .equals(this.asMmdTopicAnnotation().fileUid()))
                .findFirst()
                .orElse(null);
      }
      return Optional.ofNullable(found);
    }
  }


  public MmdTopic asMmdTopicAnnotation() {
    return this.asAnnotation();
  }

  public Optional<String> findFileUidAttribute(final Types typeUtils) {
    return findFileUidAmongParentTopics(typeUtils, this.getElement());
  }

}
