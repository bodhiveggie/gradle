/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.internal.artifacts.transform;

import com.google.common.collect.ImmutableList;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.file.BaseDirFileResolver;
import org.gradle.api.tasks.util.internal.PatternSets;
import org.gradle.internal.file.PathToFileResolver;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DefaultArtifactTransformOutputs implements ArtifactTransformOutputsInternal {

    private final ImmutableList.Builder<File> outputsBuilder = ImmutableList.builder();
    private final Set<File> outputDirectories = new HashSet<>();
    private final Set<File> outputFiles = new HashSet<>();
    private final PathToFileResolver resolver;
    private final File primaryInput;
    private final File outputDir;
    private final String primaryInputPrefix;
    private final String outputDirPrefix;

    public DefaultArtifactTransformOutputs(File primaryInput, File outputDir) {
        this.resolver = new BaseDirFileResolver(outputDir, PatternSets.getNonCachingPatternSetFactory());
        this.primaryInput = primaryInput;
        this.outputDir = outputDir;
        this.primaryInputPrefix = primaryInput.getPath() + File.separator;
        this.outputDirPrefix = outputDir.getPath() + File.separator;
    }

    @Override
    public ImmutableList<File> getRegisteredOutputs() {
        ImmutableList<File> outputs = outputsBuilder.build();
        for (File output : outputs) {
            ArtifactTransformOutputsInternal.validateOutputExists(output);
            if (outputFiles.contains(output) && !output.isFile()) {
                throw new InvalidUserDataException("Transform output file " + output.getPath() + " must be a file, but is not.");
            }
            if (outputDirectories.contains(output) && !output.isDirectory()) {
                throw new InvalidUserDataException("Transform output directory " + output.getPath() + " must be a directory, but is not.");
            }
        }
        return outputs;
    }

    @Override
    public File dir(Object path) {
        File outputDir = resolveAndRegister(path);
        outputDirectories.add(outputDir);
        return outputDir;
    }

    @Override
    public File file(Object path) {
        File outputFile = resolveAndRegister(path);
        outputFiles.add(outputFile);
        return outputFile;
    }

    private File resolveAndRegister(Object path) {
        File output = resolver.resolve(path);
        ArtifactTransformOutputsInternal.validateOutputLocation(output, primaryInput, primaryInputPrefix, outputDir, outputDirPrefix);
        outputsBuilder.add(output);
        return output;
    }
}
