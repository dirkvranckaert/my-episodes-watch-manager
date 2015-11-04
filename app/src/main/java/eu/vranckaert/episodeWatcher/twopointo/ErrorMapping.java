package eu.vranckaert.episodeWatcher.twopointo;

/**
 * Date: 04/11/14
 * Time: 11:43
 *
 * @author Dirk Vranckaert
 */
public class ErrorMapping {
    private String title;
    private String message;
    private boolean tryAgain;
    private String retryButton;

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTryAgain() {
        return tryAgain;
    }

    public String getRetryButton() {
        return retryButton;
    }

    public static class Builder {
        private String title;
        private String message;
        private boolean tryAgain;
        private String retryButton;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTryAgain(boolean tryAgain) {
            this.tryAgain = tryAgain;
            return this;
        }

        public Builder setRetryButton(String retryButton) {
            this.retryButton = retryButton;
            return this;
        }

        public ErrorMapping build() {
            ErrorMapping errorMapping = new ErrorMapping();
            errorMapping.title = title;
            errorMapping.message = message;
            errorMapping.tryAgain = tryAgain;
            errorMapping.retryButton = retryButton;
            return errorMapping;
        }
    }
}
