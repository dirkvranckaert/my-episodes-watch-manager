package eu.vranckaert.episodeWatcher.enums;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 3, 2010
 * Time: 1:11:11 AM
 */
public class CustomTracker {
    public enum PageView {
        TEST("/episodeDetailsSubActivity");

        PageView(String pageView) {
            this.pageView = pageView;
        };

        private String pageView;

        public String getPageView() {
            return pageView;
        }
    }

    public enum Event {
        TEST1("changeEpisodeStatus","markAquired"),
        TEST2("changeEpisodeStatus","markWatched");

        Event(String category, String action) {
            this.category = category;
            this.action = action;
        }

        private String category;
        private String action;

        public String getCategory() {
            return category;
        }

        public String getAction() {
            return action;
        }
    }
}
