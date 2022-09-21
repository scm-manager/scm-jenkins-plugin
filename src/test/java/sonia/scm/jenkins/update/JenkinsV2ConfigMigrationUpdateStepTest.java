/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.jenkins.update;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.jenkins.JenkinsConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JenkinsV2ConfigMigrationUpdateStepTest {

  private final V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  @Mock
  JenkinsConfigurationStoreFactory configurationStoreFactory;

  @Captor
  ArgumentCaptor<JenkinsConfiguration> globalConfigurationCaptor;
  @Captor
  ArgumentCaptor<String> repositoryIdCaptor;
  private JenkinsV2ConfigMigrationUpdateStep updateStep;

  @BeforeEach
  void captureStoreCalls() {
    lenient().doNothing().when(configurationStoreFactory).storeConfiguration(globalConfigurationCaptor.capture(), repositoryIdCaptor.capture());
  }

  @BeforeEach
  void initUpdateStep() {
    updateStep = new JenkinsV2ConfigMigrationUpdateStep(testUtil.getPropertyDAO(), configurationStoreFactory);
  }

  @Test
  void shouldMigrateRepositoryConfig() {
    ImmutableMap<Object, Object> mockedValues =
      ImmutableMap.builder()
        .put("jenkins.api-token", "123")
        .put("jenkins.project", "testproject")
        .put("jenkins.token", "abc")
        .put("jenkins.url", "http://jenkins.io")
        .put("jenkins.username", "admin")
        .put("jenkins.branches", "master,develop")
        .build();

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", (Map) mockedValues));

    updateStep.doUpdate();

    verify(configurationStoreFactory).storeConfiguration(any(), eq("repo"));

    Set<String> branches = ImmutableSet.of("master","develop");

    assertThat(globalConfigurationCaptor.getValue())
      .hasFieldOrPropertyWithValue("apiToken", "123")
      .hasFieldOrPropertyWithValue("project", "testproject")
      .hasFieldOrPropertyWithValue("token", "abc")
      .hasFieldOrPropertyWithValue("url", "http://jenkins.io")
      .hasFieldOrPropertyWithValue("username", "admin")
      .hasFieldOrPropertyWithValue("branches", branches);
  }

  @Test
  void shouldMigrateRepositoryConfigWithoutBranchesIfBranchesAreEmpty() {
    ImmutableMap<Object, Object> mockedValues =
      ImmutableMap.builder()
        .put("jenkins.api-token", "123")
        .put("jenkins.project", "testproject")
        .put("jenkins.token", "abc")
        .put("jenkins.url", "http://jenkins.io")
        .put("jenkins.username", "admin")
        .put("jenkins.branches", "")
        .build();

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", (Map) mockedValues));

    updateStep.doUpdate();

    verify(configurationStoreFactory).storeConfiguration(any(), eq("repo"));

    Set<String> branches = ImmutableSet.of();

    assertThat(globalConfigurationCaptor.getValue())
      .hasFieldOrPropertyWithValue("apiToken", "123")
      .hasFieldOrPropertyWithValue("project", "testproject")
      .hasFieldOrPropertyWithValue("token", "abc")
      .hasFieldOrPropertyWithValue("url", "http://jenkins.io")
      .hasFieldOrPropertyWithValue("username", "admin")
      .hasFieldOrPropertyWithValue("branches", branches);
  }

  @Test
  void shouldMigrateRepositoryConfigWithoutBranchesIfBranchesNull() {
    ImmutableMap<Object, Object> mockedValues =
      ImmutableMap.builder()
        .put("jenkins.api-token", "123")
        .put("jenkins.project", "testproject")
        .put("jenkins.token", "abc")
        .put("jenkins.url", "http://jenkins.io")
        .put("jenkins.username", "admin")
        .build();

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", (Map) mockedValues));

    updateStep.doUpdate();

    verify(configurationStoreFactory).storeConfiguration(any(), eq("repo"));

    Set<String> branches = ImmutableSet.of();

    assertThat(globalConfigurationCaptor.getValue())
      .hasFieldOrPropertyWithValue("apiToken", "123")
      .hasFieldOrPropertyWithValue("project", "testproject")
      .hasFieldOrPropertyWithValue("token", "abc")
      .hasFieldOrPropertyWithValue("url", "http://jenkins.io")
      .hasFieldOrPropertyWithValue("username", "admin")
      .hasFieldOrPropertyWithValue("branches", branches);
  }

  @Test
  void shouldSkipRepositoriesWithoutJenkinsConfig() {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "any", "value"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", mockedValues));

    updateStep.doUpdate();

    verify(configurationStoreFactory, never()).storeConfiguration(any(), eq("repo"));
  }
}
