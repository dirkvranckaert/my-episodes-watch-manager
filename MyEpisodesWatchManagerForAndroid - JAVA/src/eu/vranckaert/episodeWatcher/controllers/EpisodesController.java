package eu.vranckaert.episodeWatcher.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;

public class EpisodesController {
	private List<Episode> watchEpisodes = new ArrayList<Episode>();
	private List<Episode> acquireEpisodes = new ArrayList<Episode>();
	private List<Episode> comingEpisodes = new ArrayList<Episode>();
	private ArrayList<Show> shows;
	private static EpisodesController Instance;
	
	public List<Episode> getEpisodes(EpisodeType episodesType) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	return watchEpisodes;
			case EPISODES_TO_ACQUIRE: 
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:	return acquireEpisodes;
			case EPISODES_COMING: 		return comingEpisodes;
			default:					return null;
		}
	}
	
	public int getEpisodesCount(EpisodeType episodesType) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	return watchEpisodes.size();
			case EPISODES_TO_ACQUIRE: 
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:return acquireEpisodes.size();
			case EPISODES_COMING: 		return comingEpisodes.size();
			default:					return 0;
		}
	}
	
	public void setEpisodes(EpisodeType episodesType, List<Episode> episodes) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	watchEpisodes = episodes; break;
			case EPISODES_TO_ACQUIRE:
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:acquireEpisodes = episodes; break;
			case EPISODES_COMING: 		comingEpisodes = episodes; break;
			default:					break;
		}
	}
	
	public void addEpisodes(EpisodeType episodesType, List<Episode> episodes) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	watchEpisodes.addAll(episodes); break;
			case EPISODES_TO_ACQUIRE: 
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:acquireEpisodes.addAll(episodes); break;
			case EPISODES_COMING: 		comingEpisodes.addAll(episodes); break;
			default:					break;
		}
	}
	
	public void deleteEpisode(EpisodeType episodesType, Episode episode) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	
				for (int i=0; i<watchEpisodes.size(); i++)
					if (watchEpisodes.get(i).toString().equals(episode.toString()))
						watchEpisodes.remove(i);
				break;
			case EPISODES_TO_ACQUIRE: 
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
				for (int i=0; i<acquireEpisodes.size(); i++)
					if (acquireEpisodes.get(i).toString().equals(episode.toString()))
						acquireEpisodes.remove(i);
				break;
			case EPISODES_COMING:
				for (int i=0; i<comingEpisodes.size(); i++)
					if (comingEpisodes.get(i).toString().equals(episode.toString()))
						comingEpisodes.remove(i);
				break;
			default:
				break;
		}
	}
	
	public boolean areListsEmpty() {
		if (watchEpisodes.isEmpty()) {
			if (acquireEpisodes.isEmpty()) {
				if (comingEpisodes.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Episode getRandomWatchEpisode() {
		shows = new ArrayList<Show>();
        if (watchEpisodes != null && watchEpisodes.size() > 0) {
            for (Episode ep : watchEpisodes) {
                AddEpisodeToShow(ep);
            }
        }
		
		Random r = new Random();
		int randint = r.nextInt(shows.size());
		return shows.get(randint).getFirstEpisode();
	}
	
	public void deleteAll() {
		List<Episode> tempList = new ArrayList<Episode>();
		watchEpisodes = tempList; acquireEpisodes = tempList; comingEpisodes = tempList;
	}
	
	public void addEpisode(EpisodeType episodesType, Episode episode) {
		switch(episodesType) {
			case EPISODES_TO_WATCH: 	watchEpisodes.add(episode); break;
			case EPISODES_TO_ACQUIRE: 
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:acquireEpisodes.add(episode); break;
			case EPISODES_COMING: 		comingEpisodes.add(episode); break;
			default:					break;
		}
	}
	
	public static EpisodesController getInstance() {
		if (Instance == null)
			Instance = new EpisodesController();
		return Instance;
	}
	
    private void AddEpisodeToShow(Episode episode) {
        Show currentShow = CheckShowDublicate(episode.getShowName());
        if (currentShow == null) {
            Show tempShow = new Show(episode.getShowName());
            tempShow.addEpisode(episode);
            shows.add(tempShow);
        }
        else {
        	currentShow.addEpisode(episode);
        }
    }

    private Show CheckShowDublicate(String episodename)
    {
        for(Show show : shows)
        {
            if (show.getShowName().equals(episodename)) {
                return show;
            }
        }
        return null;
    }
}
