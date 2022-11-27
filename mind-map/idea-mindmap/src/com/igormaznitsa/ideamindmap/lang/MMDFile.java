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

package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.search.SearchScope;
import javax.annotation.Nonnull;

public class MMDFile extends PsiFileBase {

  public MMDFile(final FileViewProvider fileViewProvider) {
    super(fileViewProvider, MMLanguage.INSTANCE);
  }

  @Nonnull
  @Override
  public SearchScope getUseScope() {
    final Module module = ModuleUtilCore.findModuleForPsiElement(this);
    return module != null ? IdeaUtils.moduleScope(getProject(), module) : super.getUseScope();
  }

  @Nonnull
  @Override
  public FileType getFileType() {
    return MindMapFileType.INSTANCE;
  }

  @Override
  public String toString() {
    final VirtualFile virtualFile = getVirtualFile();
    return "MMDFile: " + (virtualFile != null ? virtualFile.getName() : "<unknown>");
  }

}
