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

package sonia.scm.jenkins.hooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.LookupCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

public class SvnHookHandlerHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SvnHookHandlerHelper.class);

  // This is the endpoint we trigger in Jenkins Subversion-Plugin:
  // https://github.com/jenkinsci/subversion-plugin/blob/master/src/main/java/hudson/scm/SubversionRepositoryStatus.java#L92
  public static final String URL_SUBVERSION = "/subversion/{0}/notifyCommit/?rev={1}";
  public static final String TYPE = "svn";

  private final RepositoryServiceFactory repositoryServiceFactory;

  public SvnHookHandlerHelper(RepositoryServiceFactory repositoryServiceFactory) {
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  String getContent(RepositoryHookEvent event) {
    StringBuilder content = new StringBuilder();
    try (RepositoryService service = repositoryServiceFactory.create(event.getRepository())) {
      String revision = event.getContext().getChangesetProvider().getChangesetList().get(0).getId();
      Modifications modifications = service.getModificationsCommand().revision(revision).getModifications();
      addModificationsToContent(content, modifications);
    } catch (IOException e) {
      LOG.error("Could not find modifications for changeset", e);
    }
    return content.toString();
  }

  void addModificationsToContent(StringBuilder content, Modifications modifications) {
    modifications.getAdded().forEach(m -> content.append("A").append("   ").append(m.getPath()).append("\n"));
    modifications.getCopied().forEach(m -> content.append("A").append("   ").append(m.getTargetPath()).append("\n"));
    modifications.getModified().forEach(m -> content.append("U").append("   ").append(m.getPath()).append("\n"));
    modifications.getRenamed().forEach(m -> content.append("U").append("   ").append(m.getNewPath()).append("\n"));
    modifications.getRemoved().forEach(m -> content.append("D").append("   ").append(m.getPath()).append("\n"));
  }

  String createUrl(RepositoryHookEvent event, String url, String uuid) {
    String revision = event.getContext().getChangesetProvider().getChangesetList().get(0).getId();
    String urlSuffix = MessageFormat.format(URL_SUBVERSION, uuid, revision);
    return HttpUtil.getUriWithoutEndSeperator(url).concat(urlSuffix);
  }

  Optional<String> lookupUUID(RepositoryService repositoryService) {
    LookupCommandBuilder command = repositoryService.getLookupCommand();
    Optional<String> uuid = command.lookup(String.class, "propget", "uuid", "/");
    LOG.debug("Lookup for svn repository uuid: {}", uuid.get());
    return uuid;
  }
}
