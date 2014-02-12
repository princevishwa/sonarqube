/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.scan.filesystem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.InputFile;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.scan.filesystem.internal.InputFiles;
import org.sonar.api.utils.SonarException;
import org.sonar.batch.bootstrap.AnalysisMode;

import javax.annotation.CheckForNull;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This class can't be immutable because of execution of maven plugins that can change the project structure (see MavenPluginHandler and sonar.phase)
 *
 * @since 3.5
 */
public class DefaultModuleFileSystem implements ModuleFileSystem {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultModuleFileSystem.class);

  private final String moduleKey;
  private final FileIndex index;
  private final Settings settings;

  private File baseDir, workingDir, buildDir;
  private List<File> sourceDirs = Lists.newArrayList();
  private List<File> testDirs = Lists.newArrayList();
  private List<File> binaryDirs = Lists.newArrayList();
  private List<File> sourceFiles = Lists.newArrayList();
  private List<File> testFiles = Lists.newArrayList();
  private AnalysisMode analysisMode;
  private ComponentIndexer componentIndexer;
  private boolean initialized;

  public DefaultModuleFileSystem(Project module, Settings settings, FileIndex index, ModuleFileSystemInitializer initializer, AnalysisMode analysisMode,
    ComponentIndexer componentIndexer) {
    this.componentIndexer = componentIndexer;
    this.moduleKey = module.getKey();
    this.settings = settings;
    this.index = index;
    this.analysisMode = analysisMode;
    this.baseDir = initializer.baseDir();
    this.workingDir = initializer.workingDir();
    this.buildDir = initializer.buildDir();
    this.sourceDirs = initializer.sourceDirs();
    this.testDirs = initializer.testDirs();
    this.binaryDirs = initializer.binaryDirs();
    this.sourceFiles = initializer.additionalSourceFiles();
    this.testFiles = initializer.additionalTestFiles();
  }

  public boolean isInitialized() {
    return initialized;
  }

  public String moduleKey() {
    return moduleKey;
  }

  @Override
  public File baseDir() {
    return baseDir;
  }

  @Override
  @CheckForNull
  public File buildDir() {
    return buildDir;
  }

  @Override
  public List<File> sourceDirs() {
    return sourceDirs;
  }

  @Override
  public List<File> testDirs() {
    return testDirs;
  }

  @Override
  public List<File> binaryDirs() {
    return binaryDirs;
  }

  @Override
  public File workingDir() {
    return workingDir;
  }

  List<File> sourceFiles() {
    return sourceFiles;
  }

  List<File> testFiles() {
    return testFiles;
  }

  /**
   * Should not be used - only for old plugins
   * @deprecated since 4.0
   */
  @Deprecated
  void addSourceDir(File dir) {
    throw new UnsupportedOperationException("Modifications of the file system are not permitted");
  }

  /**
   * Should not be used - only for old plugins
   * @deprecated since 4.0
   */
  @Deprecated
  void addTestDir(File dir) {
    throw new UnsupportedOperationException("Modifications of the file system are not permitted");
  }

  @Override
  public Charset sourceCharset() {
    final Charset charset;
    String encoding = settings.getString(CoreProperties.ENCODING_PROPERTY);
    if (StringUtils.isNotEmpty(encoding)) {
      charset = Charset.forName(StringUtils.trim(encoding));
    } else {
      charset = Charset.defaultCharset();
    }
    return charset;
  }

  boolean isDefaultSourceCharset() {
    return !settings.hasKey(CoreProperties.ENCODING_PROPERTY);
  }

  /**
   * @since 4.0
   */
  @Override
  public Iterable<InputFile> inputFiles(FileQuery query) {
    if (!initialized) {
      LOG.warn("Accessing the filesystem before the Sensor phase is deprecated and will not be supported in the future. Please update your plugin.");
      index.index(this);
    }
    List<InputFile> result = Lists.newArrayList();
    FileQueryFilter filter = new FileQueryFilter(analysisMode, query);
    for (InputFile input : index.inputFiles(moduleKey)) {
      if (filter.accept(input)) {
        result.add(input);
      }
    }
    return result;
  }

  @Override
  public InputFile inputFile(File ioFile) {
    if (!ioFile.isFile()) {
      throw new SonarException(ioFile.getAbsolutePath() + "is not a file");
    }
    return index.inputFile(this, ioFile);
  }

  @Override
  public List<File> files(FileQuery query) {
    return InputFiles.toFiles(inputFiles(query));
  }

  public void resetDirs(File basedir, File buildDir, List<File> sourceDirs, List<File> testDirs, List<File> binaryDirs) {
    if (initialized) {
      throw new SonarException("Module filesystem is already initialized. Modification of the filesystem are only allowed during Initializer phase.");
    }
    Preconditions.checkNotNull(basedir, "Basedir can't be null");
    this.baseDir = basedir;
    this.buildDir = buildDir;
    this.sourceDirs = existingDirs(sourceDirs);
    this.testDirs = existingDirs(testDirs);
    this.binaryDirs = existingDirs(binaryDirs);
  }

  public void index() {
    if (initialized) {
      throw new SonarException("Module filesystem can only be indexed once");
    }
    initialized = true;
    index.index(this);
    componentIndexer.execute(this);
  }

  private List<File> existingDirs(List<File> dirs) {
    ImmutableList.Builder<File> builder = ImmutableList.builder();
    for (File dir : dirs) {
      if (dir.exists() && dir.isDirectory()) {
        builder.add(dir);
      }
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultModuleFileSystem that = (DefaultModuleFileSystem) o;
    return moduleKey.equals(that.moduleKey);
  }

  @Override
  public int hashCode() {
    return moduleKey.hashCode();
  }
}
