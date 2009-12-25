package eu.vranckaert.episodeWatcher.domain;

import java.io.Serializable;
import java.util.Date;

public class Episode implements Serializable {
    private String showName;
    private String name;
    private int season;
    private int episode;
    private Date airDate;
    private String myEpisodeID;

    /**
     * Gets the show name.
     *
     * @return The show name.
     */
    public String getShowName() {
        return showName;
    }

    /**
     * Sets the show name.
     *
     * @param showName The show name.
     */
    public void setShowName(String showName) {
        this.showName = showName;
    }

    /**
     * Gets the name of the episode.
     *
     * @return The name of the episode.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the episode.
     *
     * @param name The name of the episode.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the season number of the episode.
     *
     * @return The season number of the episode.
     */
    public int getSeason() {
        return season;
    }

    /***
     * Sets the season number of the episode.
     *
     * @param season The season number of the episode.
     */
    public void setSeason(int season) {
        this.season = season;
    }

    /**
     * Gets the number of the episode of a season.
     *
     * @return The number of the episode of a season.
     */
    public int getEpisode() {
        return episode;
    }

    /**
     * Sets the number of the episode of a season.
     *
     * @param episode The number of the episode of a season.
     */
    public void setEpisode(int episode) {
        this.episode = episode;
    }

    /**
     * Gets the air date of the episode.
     *
     * @return The air date of the episode.
     */
    public Date getAirDate() {
        return airDate;
    }

    /**
     * Sets the air date of the episode.
     *
     * @param airDate The air date of the episode.
     */
    public void setAirDate(Date airDate) {
        this.airDate = airDate;
    }

    /**
     * Gets the MyEpisode unique number for this episode.
     *
     * @return The MyEpisode unique number for this episode.
     */
    public String getMyEpisodeID() {
        return myEpisodeID;
    }

    /**
     * Sets the MyEpisdoe unique number for this episode.
     *
     * @param myEpisodeID The MyEpisode unique number for this episode.
     */
    public void setMyEpisodeID(String myEpisodeID) {
        this.myEpisodeID = myEpisodeID;
    }

    @Override
    public String toString() {
        return showName + " S" + season + "E" + episode + " - " + name + " (" + myEpisodeID + ") (" + airDate + ")";
    }
}
