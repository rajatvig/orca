/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.pipeline.expressions.functions

import com.netflix.spinnaker.kork.artifacts.model.Artifact
import com.netflix.spinnaker.kork.artifacts.model.ExpectedArtifact
import com.netflix.spinnaker.kork.expressions.SpelHelperFunctionException
import com.netflix.spinnaker.orca.pipeline.model.DefaultTrigger
import spock.lang.Shared
import spock.lang.Specification

import static com.netflix.spinnaker.orca.pipeline.expressions.functions.ArtifactExpressionFunctionProvider.triggerResolvedArtifact
import static com.netflix.spinnaker.orca.pipeline.expressions.functions.ArtifactExpressionFunctionProvider.triggerResolvedArtifactByType
import static com.netflix.spinnaker.orca.pipeline.expressions.functions.ArtifactExpressionFunctionProvider.resolvedArtifacts
import static com.netflix.spinnaker.orca.test.model.ExecutionBuilder.pipeline
import static com.netflix.spinnaker.orca.test.model.ExecutionBuilder.stage


class ArtifactExpressionFunctionProviderSpec extends Specification {
  @Shared
  def pipeline1 = pipeline {
    def matchArtifact1 = Artifact.builder().type("docker/image").name("artifact1").build()
    def boundArtifact = Artifact.builder().type("docker/image").name("artifact1").reference("artifact1").build()

    trigger = new DefaultTrigger("manual", "artifact1")
    trigger.resolvedExpectedArtifacts = [
      ExpectedArtifact.builder().matchArtifact(matchArtifact1).boundArtifact(boundArtifact).build()
    ]

    stage {
      id = "1"
      refId = "1"
      name = "Stage 1"
      outputs = [artifacts: [Artifact.builder().type("type1").name("name1").reference("ref1").build()] ]
    }

    stage {
      id = "2"
      refId = "2"
      name = "Stage 2"
      outputs = [artifacts: [Artifact.builder().type("type2").name("name2").reference("ref2").build()] ]
    }

    stage {
      id = "3"
      refId = "3"
      name = "Stage 3"
      outputs = [artifacts: [Artifact.builder().type("type3").name("name3").reference("ref3").build()] ]
    }

    stage {
      id = "4"
      refId = "4"
      name = "Stage 4"
      outputs = [artifacts: [Artifact.builder().type("type4").name("name4").reference("ref4").build()] ]
    }
  }

  def "triggerResolvedArtifact returns resolved trigger artifact by name"() {
    when:
    def artifact = triggerResolvedArtifact(pipeline1, "artifact1")

    then:
    artifact.type == "docker/image"
    artifact.name == "artifact1"
  }

  def "triggerResolvedArtifactByType returns resolved trigger artifact by type"() {
    when:
    def artifact = triggerResolvedArtifactByType(pipeline1, "docker/image")

    then:
    artifact.type == "docker/image"
    artifact.name == "artifact1"
  }


  def "triggerResolvedArtifact throws when artifact is not found"() {
    when:
    triggerResolvedArtifact(pipeline1, "somename")

    then:
    thrown(SpelHelperFunctionException)
  }

  def "triggerResolvedArtifactByType throws if artifact is not found"() {
    when:
    triggerResolvedArtifactByType(pipeline1, "s3/object")

    then:
    thrown(SpelHelperFunctionException)
  }

  def "resolvedArtifacts returns artifacts in execution"() {
    when:
    def artifacts = resolvedArtifacts(pipeline1)

    then:
    artifacts.collect { it.name } == [ "name1", "name2", "name3", "name4" ]
    artifacts.collect { it.type } == [ "type1", "type2", "type3", "type4" ]
    artifacts.collect { it.reference } == [ "ref1", "ref2", "ref3", "ref4" ]
  }
}
