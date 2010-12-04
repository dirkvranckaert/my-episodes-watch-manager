package eu.vranckaert.episodeWatcher.enums;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 3, 2010
 * Time: 1:11:11 AM
 */
public class CustomTracker {
    public enum PageView {
        EPISODE_DETAILS("/episodeDetailsSubActivity"),
        EPISODE_LIST("/episodesWatchListActivity"),
        PREFERENCES_GENERAL("/generalPreferences"),
        SHOW_MANAGEMENT("/manageShows"),
        LOGIN("loginSubActivity"),
        REGISTER_USER("registerSubActivity"),
        SHOW_MANAGEMENT_FAVOS("/favouriteShowsActivity"),
        SHOW_MANAGEMENT_IGNORED("/ignoredShowsActivity"),
        SHOW_MANAGEMENT_SEARCH("/showSearchActivity");

        PageView(String pageView) {
            this.pageView = pageView;
        };

        private String pageView;

        public String getPageView() {
            return pageView;
        }
    }

    public enum Event {
        MARK_WATCHED("MarkAsWatched","ContextMenu-EpisodesWatchListActivity"),
        MARK_ACQUIRED("MarkAsAcquire","ContextMenu-EpisodesWatchListActivity"),
        LOGOUT("Logout", "MenuButton-EpisodesWatchListActivity"),
        SHOW_INGORE("IgnoreShow", "FavosAndIngoredShowManagementActivity"),
        SHOW_UNIGNORE("UnignoreShow", "FavosAndIngoredShowManagementActivity"),
        SHOW_DELETE("DeleteShow", "FavosAndIngoredShowManagementActivity"),
        SHOW_ADD_NEW("AddNewShow","ContextMenu-ShowSearchActivity");

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
