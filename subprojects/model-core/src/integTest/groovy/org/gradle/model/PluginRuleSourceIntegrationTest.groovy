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

package org.gradle.model

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class PluginRuleSourceIntegrationTest extends AbstractIntegrationSpec {

    def "plugin class can expose model rules"() {
        when:
        buildScript """
            class MyPlugin implements Plugin<Project> {
                void apply(Project project) {
                }

                @RuleSource
                static class Rules {
                    @Model
                    List strings() {
                      []
                    }
                }
            }

            apply plugin: MyPlugin

            model {
                strings {
                    add "foo"
                }
            }

            // internal API here
            task value {
                doFirst { println "value: " + modelRegistry.get("strings", List) }
            }
        """

        then:
        succeeds "value"

        and:
        output.contains "value: [foo]"
    }

    def "configuration in script is not executed if not needed"() {
        when:
        buildScript """
            class MyPlugin implements Plugin<Project> {
                void apply(Project project) {
                }

                @RuleSource
                static class Rules {
                    @Model
                    List strings() {
                      []
                    }
                }
            }

            apply plugin: MyPlugin

            def called = false

            model {
                strings {
                    // this strategy for detecting if this was called might not work when we lock down outside access in rules
                    called = true
                    add "foo"
                }
            }

            // internal API here
            task value {
                doFirst { println "called: \$called" }
            }
        """

        then:
        succeeds "value"

        and:
        output.contains "called: false"
    }

    def "informative error message when rules are invalid"() {
        when:
        buildScript """
            class MyPlugin implements Plugin<Project> {
                void apply(Project project) {
                }

                @RuleSource
                class Rules {
                }
            }

            apply plugin: MyPlugin
        """

        then:
        fails "tasks"

        and:
        failure.assertHasCause("Failed to apply plugin [class 'MyPlugin']")
        failure.assertHasCause("Type MyPlugin\$Rules is not a valid model rule source: enclosed classes must be static and non private")
    }

}