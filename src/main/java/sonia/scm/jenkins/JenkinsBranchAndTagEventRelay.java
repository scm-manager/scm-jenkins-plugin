package sonia.scm.jenkins;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;

import static sonia.scm.HandlerEventType.DELETE;

@Extension
@EagerSingleton
public class JenkinsBranchAndTagEventRelay {

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    if (event.getEventType() == DELETE) {
      // Relay event
    }
  }
}
