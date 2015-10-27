package eu.vranckaert.episodeWatcher.controllers;

import java.util.ArrayList;
import java.util.List;

public class RowController {
	private List<String> openWatchRows = new ArrayList<String>();
	private List<String> openAcquireRows = new ArrayList<String>();
	private List<String> openComingRows = new ArrayList<String>();
	private static RowController Instance;
	
	public static RowController getInstance() {
		if (Instance == null)
			Instance = new RowController();
		return Instance;
	}

	public List<String> getOpenWatchRows() {
		return openWatchRows;
	}

	public void setOpenWatchRows(List<String> openWatchRows) {
		this.openWatchRows = openWatchRows;
	}

	public List<String> getOpenAcquireRows() {
		return openAcquireRows;
	}

	public void setOpenAcquireRows(List<String> openAcquireRows) {
		this.openAcquireRows = openAcquireRows;
	}

	public List<String> getOpenComingRows() {
		return openComingRows;
	}

	public void setOpenComingRows(List<String> openComingRows) {
		this.openComingRows = openComingRows;
	}
}
