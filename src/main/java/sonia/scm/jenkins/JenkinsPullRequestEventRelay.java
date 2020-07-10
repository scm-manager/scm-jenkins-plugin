package sonia.scm.jenkins;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import com.cloudogu.scm.review.pullrequest.*;

import static sonia.scm.HandlerEventType.DELETE;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class JenkinsPullRequestEventRelay {

  @Subscribe
  public void handle(PullRequestEvent event) {
    if (event.getEventType() == DELETE) {
      // Relay event
    }
  }
}
