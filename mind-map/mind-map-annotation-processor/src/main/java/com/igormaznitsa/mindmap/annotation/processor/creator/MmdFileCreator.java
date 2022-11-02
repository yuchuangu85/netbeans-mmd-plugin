package com.igormaznitsa.mindmap.annotation.processor.creator;

import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationFileItem;
import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationTopicItem;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class MmdFileCreator {

  private final Builder builder;

  private MmdFileCreator(final Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void process() {
    final Map<String, MmdAnnotationFileItem> fileMap = new HashMap<>();

    final AtomicBoolean error = new AtomicBoolean();

    this.builder.annotations.stream()
        .filter(x -> x.getAnnotation() instanceof MmdFile)
        .map(MmdAnnotationFileItem::new)
        .forEach(x -> {
          if (fileMap.containsKey(x.getUid())) {
            this.builder.getMessager()
                .printMessage(Diagnostic.Kind.ERROR,
                    String.format("Found duplicated file for UID: %s",
                        x.getUid()), x.getAnnotation().getElement());
            error.set(true);
          } else {
            fileMap.put(x.getUid(), x);
          }
        });

    if (error.get()) {
      return;
    }

    this.builder.annotations.stream()
        .filter(x -> x.getAnnotation() instanceof MmdTopic)
        .map(MmdAnnotationTopicItem::new)
        .forEach(x -> {
          if (!processTopic(fileMap, x)) {
            error.set(true);
          }
        });

    if (error.get()) {
      return;
    }

    this.writeFiles(fileMap);
  }

  private void writeFiles(final Map<String, MmdAnnotationFileItem> fileMap) {

  }

  private boolean processTopic(
      final Map<String, MmdAnnotationFileItem> fileMap,
      final MmdAnnotationTopicItem topic
  ) {
    return true;
  }

  public static final class Builder {
    private List<MmdAnnotation> annotations;
    private Path forceFolder;
    private boolean overwriteAllowed = true;
    private boolean dryStart;
    private boolean preferRelativePaths = true;
    private Messager messager;

    private Builder() {
    }

    public Optional<Path> getForceFolder() {
      return Optional.ofNullable(this.forceFolder);
    }

    public Builder setForceFolder(final Path forceFolder) {
      this.forceFolder = forceFolder;
      return this;
    }

    public List<MmdAnnotation> getAnnotations() {
      return this.annotations;
    }

    public Builder setAnnotations(
        final List<MmdAnnotation> annotations) {
      this.annotations = unmodifiableList(new ArrayList<>(annotations));
      return this;
    }

    public boolean isOverwriteAllowed() {
      return this.overwriteAllowed;
    }

    public Builder setOverwriteAllowed(final boolean overwriteAllowed) {
      this.overwriteAllowed = overwriteAllowed;
      return this;
    }

    public boolean isPreferRelativePaths() {
      return this.preferRelativePaths;
    }

    public Builder setPreferRelativePaths(final boolean preferRelativePaths) {
      this.preferRelativePaths = preferRelativePaths;
      return this;
    }

    public boolean isDryStart() {
      return this.dryStart;
    }

    public Builder setDryStart(final boolean dryStart) {
      this.dryStart = dryStart;
      return this;
    }

    public Messager getMessager() {
      return this.messager;
    }

    public Builder setMessager(final Messager messager) {
      this.messager = Objects.requireNonNull(messager);
      return this;
    }

    public MmdFileCreator build() {
      if (this.annotations == null || this.messager == null) {
        throw new IllegalStateException("Not all fields set");
      }
      return new MmdFileCreator(this);
    }
  }
}
