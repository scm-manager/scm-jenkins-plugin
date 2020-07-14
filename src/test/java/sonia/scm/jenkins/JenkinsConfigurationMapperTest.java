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
package sonia.scm.jenkins;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static sonia.scm.jenkins.JenkinsConfigurationMapper.DUMMY_SECRET;

@ExtendWith(MockitoExtension.class)
class JenkinsConfigurationMapperTest {

  private final static Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final URI baseUri = URI.create("http://example.com/base/");
  private URI expectedBaseUri;

  @Mock
  private Subject subject;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  JenkinsConfigurationMapperImpl mapper;

  @BeforeEach
  void init() {
    lenient().when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve("v2/config/jenkins/");
  }

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @Test
  void shouldMapAttributesToDto() {
    JenkinsConfiguration configuration = createConfiguration();
    JenkinsConfigurationDto dto = mapper.map(configuration, REPOSITORY);
    assertThat(dto.getApiToken()).isEqualTo(DUMMY_SECRET);
    assertThat(dto.getBranches().iterator().next()).isEqualTo("master");
    assertThat(dto.getProject()).isEqualTo(configuration.getProject());
    assertThat(dto.getToken()).isEqualTo(DUMMY_SECRET);
    assertThat(dto.isCsrf()).isEqualTo(configuration.isCsrf());
    assertThat(dto.getUrl()).isEqualTo(configuration.getUrl());
    assertThat(dto.getUsername()).isEqualTo(configuration.getUsername());
    assertThat(dto.getBuildParameters().iterator().next().getName())
      .isEqualTo(configuration.getBuildParameters().iterator().next().getName());
    assertThat(dto.getBuildParameters().iterator().next().getValue())
      .isEqualTo(configuration.getBuildParameters().iterator().next().getValue());
  }

  @Test
  void shouldAddHalLinksToDto() {
    when(subject.isPermitted("repository:jenkins:" + REPOSITORY.getId())).thenReturn(true);

    JenkinsConfigurationDto dto = mapper.map(createConfiguration(), REPOSITORY);

    String expectedUrl = expectedBaseUri.toString() + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
    assertThat(expectedUrl).isEqualTo(dto.getLinks().getLinkBy("self").get().getHref());
    assertThat(expectedUrl).isEqualTo(dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
    JenkinsConfigurationDto dto = mapper.map(createConfiguration(), REPOSITORY);
    assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
  }

  @Test
  void shouldMapAttributesFromDto() {
    JenkinsConfiguration oldConfiguration = createConfiguration();
    JenkinsConfigurationDto dto = createDto();
    JenkinsConfiguration configuration = mapper.map(dto, oldConfiguration);
    assertThat(configuration.getApiToken()).isEqualTo(oldConfiguration.getApiToken());
    assertThat(configuration.getToken()).isEqualTo(oldConfiguration.getToken());
    assertThat(configuration.getBranches()).isEqualTo(dto.getBranches());
    assertThat(configuration.getUrl()).isEqualTo(dto.getUrl());
    assertThat(configuration.getUsername()).isEqualTo(dto.getUsername());
    assertThat(configuration.getProject()).isEqualTo(dto.getProject());
    assertThat(configuration.getBuildParameters().iterator().next().getName())
      .isEqualTo(dto.getBuildParameters().iterator().next().getName());
    assertThat(configuration.getBuildParameters().iterator().next().getValue())
      .isEqualTo(dto.getBuildParameters().iterator().next().getValue());
  }

  @Test
  void shouldReplacePasswordAfterMappingDto() {
    JenkinsConfigurationDto dto = mapper.map(createConfiguration(), REPOSITORY);
    assertThat(dto.getApiToken()).isEqualTo(DUMMY_SECRET);
    assertThat(dto.getToken()).isEqualTo(DUMMY_SECRET);
  }

  @Test
  void shouldRestorePasswordAfterMappingFromDto() {
    JenkinsConfigurationDto dto = createDto();
    JenkinsConfiguration oldConfig = createConfiguration();
    dto.setToken(DUMMY_SECRET);
    dto.setApiToken(DUMMY_SECRET);

    JenkinsConfiguration configuration = mapper.map(dto, oldConfig);
    assertThat(configuration.getApiToken()).isEqualTo(oldConfig.getApiToken());
    assertThat(configuration.getToken()).isEqualTo(oldConfig.getToken());
  }

  private JenkinsConfiguration createConfiguration() {
    JenkinsConfiguration configuration = new JenkinsConfiguration();
    configuration.setApiToken("API_TOKEN");
    configuration.setBranches(ImmutableSet.of("master"));
    configuration.setProject("Project_X");
    configuration.setToken("SOME_TOKEN");
    configuration.setUrl("http://jenkins.io/scm");
    configuration.setUsername("trillian");
    configuration.setCsrf(false);
    configuration.setBuildParameters(ImmutableSet.of(new BuildParameter("author", "trillian")));
    return configuration;
  }

  private JenkinsConfigurationDto createDto() {
    JenkinsConfigurationDto configuration = new JenkinsConfigurationDto();
    configuration.setApiToken("__DUMMY__");
    configuration.setBranches(ImmutableSet.of("develop"));
    configuration.setProject("Project_Y");
    configuration.setToken("__DUMMY__");
    configuration.setUrl("http://jenkins.io/scm2");
    configuration.setUsername("dent");
    configuration.setCsrf(true);
    configuration.setBuildParameters(ImmutableSet.of(new BuildParameterDto("env", "testing")));
    return configuration;
  }
}
