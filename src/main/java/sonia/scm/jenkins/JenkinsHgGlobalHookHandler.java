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

import com.google.inject.Provider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.api.RepositoryServiceFactory;

class JenkinsHgGlobalHookHandler extends JenkinsGlobalHookHandler {

  public static final String TYPE_MERCURIAL = "hg";
  private static final String URL_MERCURIAL = "/mercurial/notifyCommit";

  private final GlobalJenkinsConfiguration configuration;

  JenkinsHgGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider, GlobalJenkinsConfiguration configuration, RepositoryServiceFactory repositoryServiceFactory) {
    super(httpClientProvider, configuration, repositoryServiceFactory);
    this.configuration = configuration;
  }

  String createUrlSuffix() {
    if (!configuration.isDisableMercurialTrigger()) {
      return URL_MERCURIAL;
    }
    return null;
  }
}
