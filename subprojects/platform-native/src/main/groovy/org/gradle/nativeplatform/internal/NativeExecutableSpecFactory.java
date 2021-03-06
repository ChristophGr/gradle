/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.nativeplatform.internal;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.base.ProjectSourceSet;
import org.gradle.nativeplatform.NativeExecutableSpec;
import org.gradle.runtime.base.ComponentSpecIdentifier;
import org.gradle.runtime.base.internal.DefaultComponentSpecIdentifier;

public class NativeExecutableSpecFactory implements NamedDomainObjectFactory<NativeExecutableSpec> {
    private final Instantiator instantiator;
    private ProjectSourceSet sources;
    private final Project project;

    public NativeExecutableSpecFactory(Instantiator instantiator, ProjectSourceSet sources, Project project) {
        this.instantiator = instantiator;
        this.sources = sources;
        this.project = project;
    }

    public NativeExecutableSpec create(String name) {
        ComponentSpecIdentifier id = new DefaultComponentSpecIdentifier(project.getPath(), name);
        return instantiator.newInstance(DefaultNativeExecutableSpec.class, id, sources.maybeCreate(name));
    }
}
