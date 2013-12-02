/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.ide.visualstudio.internal;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.nativebinaries.*;
import org.gradle.nativebinaries.internal.NativeComponentInternal;
import org.gradle.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisualStudioProjectRegistry {
    private final FileResolver fileResolver;
    private final FlavorContainer allFlavors;
    private final Map<String, VisualStudioProject> projects = new HashMap<String, VisualStudioProject>();
    private final VisualStudioProjectResolver projectResolver;

    public VisualStudioProjectRegistry(FileResolver fileResolver, VisualStudioProjectResolver projectResolver, FlavorContainer allFlavors) {
        this.fileResolver = fileResolver;
        this.allFlavors = allFlavors;
        this.projectResolver = projectResolver;
    }

    public VisualStudioProjectConfiguration getProjectConfiguration(NativeBinary nativeBinary) {
        String projectName = projectName(nativeBinary);
        return projects.get(projectName).getConfiguration(nativeBinary);
    }

    public void addProjectConfiguration(NativeBinary nativeBinary) {
        VisualStudioProject project = getOrCreateProject(nativeBinary);
        project.addConfiguration(nativeBinary);
    }

    private VisualStudioProject getOrCreateProject(NativeBinary nativeBinary) {
        String projectName = projectName(nativeBinary);
        VisualStudioProject vsProject = projects.get(projectName);
        if (vsProject == null) {
            vsProject = new VisualStudioProject(projectName, nativeBinary.getComponent(), fileResolver, projectResolver);
            projects.put(projectName, vsProject);
        }
        return vsProject;
    }

    public List<VisualStudioProject> getAllProjects() {
        return CollectionUtils.toList(projects.values());
    }

    private String projectName(NativeBinary nativeBinary) {
        return projectBaseName(nativeBinary) + projectSuffix(nativeBinary);
    }

    private String projectBaseName(NativeBinary nativeBinary) {
        NativeComponent component = nativeBinary.getComponent();
        if (getFlavors(component).size() <=1) {
            return component.getBaseName();
        }
        return nativeBinary.getFlavor().getName() + StringUtils.capitalize(component.getBaseName());
    }

    private String projectSuffix(NativeBinary nativeBinary) {
        return nativeBinary instanceof StaticLibraryBinary ? "Lib"
                : nativeBinary instanceof SharedLibraryBinary ? "Dll"
                : "Exe";
    }

    // TODO:DAZ This needs to be a method on NativeComponentInternal
    private Set<Flavor> getFlavors(final NativeComponent component) {
        return ((NativeComponentInternal) component).chooseFlavors(allFlavors);
    }
}
