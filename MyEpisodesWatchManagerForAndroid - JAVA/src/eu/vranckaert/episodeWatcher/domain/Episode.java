package eu.vranckaert.episodeWatcher.domain;

import java.io.Serializable;
import java.util.Date;

import eu.vranckaert.episodeWatcher.enums.EpisodeType;

public class Episode implements Serializable {
	private static final long serialVersionUID = -8433440792935384437L;
	private String showName;
    private String name;
    private int season;
    private int episode;
    private Date airDate;
    private String myEpisodeID;
    private EpisodeType type;

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

    /**
     * Gets the season number of the episode in String.
     *
     * @return The season number of the episode.
     */    
    public String getSeasonString() {
        if (season < 10)
        {
        	return "0" + season;
        }
        else
        {
        	return "" + season;
        }
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
     * Gets the number of the episode of a season in String.
     *
     * @return The number of the episode of a season.
     */
    public String getEpisodeString() {
        if (episode < 10)
        {
        	return "0" + episode;
        }
        else
        {
        	return "" + episode;
        }
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
    
    public EpisodeType getType() {
		return type;
	}

	public void setType(EpisodeType type) {
		this.type = type;
	}


    @Override
    public String toString() {
        return showName + " S" + getSeasonString() + "E" + getEpisodeString() + " - " + name + " (" + myEpisodeID + ") (" + airDate + ")";
    }
}
