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
package org.gradle.language.nativebase.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Incubating;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.language.nativebase.internal.incremental.IncrementalCompilerBuilder;
import org.gradle.nativeplatform.platform.Platform;
import org.gradle.nativeplatform.platform.internal.PlatformInternal;
import org.gradle.nativeplatform.toolchain.ToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.ToolChainInternal;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Compiles native source files into object files.
 */
@Incubating
public abstract class AbstractNativeCompileTask extends DefaultTask {
    private ToolChainInternal toolChain;
    private PlatformInternal targetPlatform;
    private boolean positionIndependentCode;
    private File objectFileDir;
    private ConfigurableFileCollection includes;
    private ConfigurableFileCollection source;
    private Map<String, String> macros;
    private List<String> compilerArgs;

    public AbstractNativeCompileTask() {
        includes = getProject().files();
        source = getProject().files();
    }

    @Inject
    public IncrementalCompilerBuilder getIncrementalCompilerBuilder() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void compile(IncrementalTaskInputs inputs) {

        NativeCompileSpec spec = createCompileSpec();
        spec.setTempDir(getTemporaryDir());
        spec.setObjectFileDir(getObjectFileDir());
        spec.include(getIncludes());
        spec.source(getSource());
        spec.setMacros(getMacros());
        spec.args(getCompilerArgs());
        spec.setPositionIndependentCode(isPositionIndependentCode());
        spec.setIncrementalCompile(inputs.isIncremental());

        PlatformToolChain platformToolChain = toolChain.select(targetPlatform);
        WorkResult result = getIncrementalCompilerBuilder().createIncrementalCompiler(this, platformToolChain.newCompiler(spec), toolChain).execute(spec);

        setDidWork(result.getDidWork());
    }

    protected abstract NativeCompileSpec createCompileSpec();

    @Input
    public String getOutputType() {
        return toolChain.getOutputType() + ":" + targetPlatform.getCompatibilityString();
    }

    /**
     * The tool chain used for compilation.
     */
    public ToolChain getToolChain() {
        return toolChain;
    }

    public void setToolChain(ToolChain toolChain) {
        this.toolChain = (ToolChainInternal) toolChain;
    }

    /**
     * The platform being targeted.
     */
    public Platform getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(Platform targetPlatform) {
        this.targetPlatform = (PlatformInternal) targetPlatform;
    }

    /**
     * Should the compiler generate position independent code?
     */
    @Input
    public boolean isPositionIndependentCode() {
        return positionIndependentCode;
    }

    public void setPositionIndependentCode(boolean positionIndependentCode) {
        this.positionIndependentCode = positionIndependentCode;
    }

    /**
     * The directory where object files will be generated.
     */
    @OutputDirectory
    public File getObjectFileDir() {
        return objectFileDir;
    }

    public void setObjectFileDir(File objectFileDir) {
        this.objectFileDir = objectFileDir;
    }

    /**
     * Returns the header directories to be used for compilation.
     */
    @InputFiles
    public FileCollection getIncludes() {
        return includes;
    }

    /**
     * Add directories where the compiler should search for header files.
     */
    public void includes(Object includeRoots) {
        includes.from(includeRoots);
    }

    /**
     * Returns the source files to be compiled.
     */
    @InputFiles
    public FileCollection getSource() {
        return source;
    }

    /**
     * Adds a set of source files to be compiled. The provided sourceFiles object is evaluated as per {@link org.gradle.api.Project#files(Object...)}.
     */
    public void source(Object sourceFiles) {
        source.from(sourceFiles);
    }

    /**
     * Macros that should be defined for the compiler.
     */
    @Input
    public Map<String, String> getMacros() {
        return macros;
    }

    public void setMacros(Map<String, String> macros) {
        this.macros = macros;
    }

    /**
     * Additional arguments to provide to the compiler.
     */
    @Input
    public List<String> getCompilerArgs() {
        return compilerArgs;
    }

    public void setCompilerArgs(List<String> compilerArgs) {
        this.compilerArgs = compilerArgs;
    }

}
