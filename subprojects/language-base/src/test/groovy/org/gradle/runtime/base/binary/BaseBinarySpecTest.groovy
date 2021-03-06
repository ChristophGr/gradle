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

package org.gradle.runtime.base.binary
import org.gradle.internal.reflect.DirectInstantiator
import org.gradle.runtime.base.ModelInstantiationException
import org.gradle.runtime.base.internal.BinaryNamingScheme
import spock.lang.Specification

class BaseBinarySpecTest extends Specification {
    def instantiator = new DirectInstantiator()
    def binaryNamingScheme = Mock(BinaryNamingScheme)

    def "cannot instantiate directly"() {
        when:
        new BaseBinarySpec() {}

        then:
        def e = thrown ModelInstantiationException
        e.message == "Direct instantiation of a BaseBinarySpec is not permitted. Use a BinaryTypeBuilder instead."
    }

    def "cannot create instance of base class"() {
        when:
        BaseBinarySpec.create(BaseBinarySpec, binaryNamingScheme, instantiator)

        then:
        def e = thrown ModelInstantiationException
        e.message == "Cannot create instance of abstract class BaseBinarySpec."
    }

    def "binary has name and sensible display name"() {
        def binary = BaseBinarySpec.create(MySampleBinary, binaryNamingScheme, instantiator)

        when:
        _ * binaryNamingScheme.lifecycleTaskName >> "sampleBinary"

        then:
        binary.class == MySampleBinary
        binary.name == "sampleBinary"
        binary.displayName == "MySampleBinary: 'sampleBinary'"
    }

    def "create fails if subtype does not have a public no-args constructor"() {

        when:
        BaseBinarySpec.create(MyConstructedBinary, binaryNamingScheme, instantiator)

        then:
        def e = thrown ModelInstantiationException
        e.message == "Could not create binary of type MyConstructedBinary"
        e.cause instanceof IllegalArgumentException
        e.cause.message.startsWith "Could not find any public constructor for class"
    }

    static class MySampleBinary extends BaseBinarySpec {
    }
    static class MyConstructedBinary extends BaseBinarySpec {
        MyConstructedBinary(String arg) {}
    }
}
