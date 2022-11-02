package com.igormaznitsa.mindmap.annotation.processor;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.annotation.processor.creator.MmdFileCreator;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdFiles;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class MmdAnnotationProcessor extends AbstractProcessor {

  public static final String KEY_MMD_FORCE_FOLDER = "mmd.doc.force.folder";
  public static final String KEY_MMD_DRY_START = "mmd.doc.dry.start";
  public static final String KEY_MMD_FOLDER_CREATE = "mmd.doc.folder.create";
  public static final String KEY_MMD_RELATIVE_PATHS = "mmd.doc.path.relative";
  public static final String KEY_MMD_FILE_OVERWRITE = "mmd.doc.file.overwrite";
  private static final String MSG_PREFIX = "MMD: ";
  private static final Set<String> SUPPORTED_OPTIONS =
      Set.of(
          KEY_MMD_FORCE_FOLDER,
          KEY_MMD_FOLDER_CREATE,
          KEY_MMD_RELATIVE_PATHS,
          KEY_MMD_FILE_OVERWRITE,
          KEY_MMD_DRY_START);
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Map.of(
          MmdTopic.class.getName(), MmdTopic.class,
          MmdFiles.class.getName(), MmdFiles.class,
          MmdFile.class.getName(), MmdFile.class
      );
  private Trees trees;
  private SourcePositions sourcePositions;
  private Messager messager;
  private Path optionForceFolder;
  private boolean optionPreferRelativePaths;
  private boolean optionFileOverwrite;
  private boolean optionDryStart;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ANNOTATIONS.keySet();
  }

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.trees = Trees.instance(processingEnv);
    this.sourcePositions = this.trees.getSourcePositions();
    this.messager = processingEnv.getMessager();

    this.optionDryStart = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_DRY_START, "false"));

    if (this.optionDryStart) {
      this.messager.printMessage(WARNING,
          MSG_PREFIX + "Started in DRY mode");
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_FORCE_FOLDER)) {
      this.optionForceFolder = Paths.get(processingEnv.getOptions().get(KEY_MMD_FORCE_FOLDER));
      if (!(Files.isDirectory(this.optionForceFolder) || this.optionDryStart)) {
        this.messager.printMessage(WARNING,
            MSG_PREFIX + "Folder not-exists: " + this.optionForceFolder);
        if (Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, "false"))) {
          try {
            this.optionForceFolder = Files.createDirectories(this.optionForceFolder);
            this.messager.printMessage(NOTE,
                MSG_PREFIX + "Folder created: " + this.optionForceFolder);
          } catch (IOException ex) {
            this.messager.printMessage(ERROR,
                MSG_PREFIX + "Can't create requested target folder " + this.optionForceFolder);
          }
        } else {
          this.messager.printMessage(ERROR,
              MSG_PREFIX + "Can't find folder (use " + KEY_MMD_FOLDER_CREATE +
                  " flag to make it): " +
                  this.optionForceFolder);
        }
      }

      if (this.optionForceFolder != null) {
        this.messager.printMessage(WARNING,
            String.format(MSG_PREFIX + "Force MMD folder: %s", this.optionForceFolder));
      }
    }

    this.optionPreferRelativePaths = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_RELATIVE_PATHS, "true"));

    this.optionFileOverwrite = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_FILE_OVERWRITE, "true"));

    this.messager.printMessage(NOTE,
        String.format(MSG_PREFIX + "Prefer generate relative paths: %s",
            this.optionPreferRelativePaths));
  }

  @Override
  public Set<String> getSupportedOptions() {
    return SUPPORTED_OPTIONS;
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {

    final List<MmdAnnotation> mmdAnnotationList = new ArrayList<>();

    for (final TypeElement annotation : annotations) {
      final Set<? extends Element> annotatedElements =
          roundEnv.getElementsAnnotatedWith(annotation);

      final Class<? extends Annotation> annotationClass =
          ANNOTATIONS.get(annotation.getQualifiedName().toString());
      requireNonNull(annotationClass,
          () -> "Unexpectedly annotation class not found for " + annotation.getQualifiedName());

      annotatedElements.forEach(element -> {
        final Annotation annotationInstance = element.getAnnotation(annotationClass);
        final UriLine position = findPosition(element);

        if (annotationInstance instanceof MmdFiles) {
          for (MmdFile mmdFile : ((MmdFiles) annotationInstance).value()) {
            mmdAnnotationList.add(
                new MmdAnnotation(element, mmdFile, new File(position.uri).toPath(),
                    position.line));
          }
        } else {
          mmdAnnotationList.add(
              new MmdAnnotation(element, annotationInstance, new File(position.uri).toPath(),
                  position.line));
        }
      });
    }

    this.messager.printMessage(
        NOTE, format(MSG_PREFIX + "Detected %d annotated items", mmdAnnotationList.size()));

    if (!mmdAnnotationList.isEmpty()) {
      MmdFileCreator.builder()
          .setMessager(this.messager)
          .setForceFolder(this.optionForceFolder)
          .setDryStart(this.optionDryStart)
          .setOverwriteAllowed(this.optionFileOverwrite)
          .setPreferRelativePaths(this.optionPreferRelativePaths)
          .setAnnotations(mmdAnnotationList)
          .build().process();
    }

    return true;
  }

  private UriLine findPosition(final Element element) {
    final TreePath treePath = trees.getPath(element);
    final CompilationUnitTree compilationUnit = treePath.getCompilationUnit();
    final long startPosition =
        this.sourcePositions.getStartPosition(compilationUnit, treePath.getLeaf());
    final long lineNumber = compilationUnit.getLineMap().getLineNumber(startPosition);
    return new UriLine(compilationUnit.getSourceFile().toUri(), lineNumber);
  }

  private static final class UriLine {
    private final URI uri;
    private final long line;

    UriLine(final URI uri, final long line) {
      this.uri = requireNonNull(uri);
      this.line = line;
    }

  }
}
