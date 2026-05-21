package com.mdsg.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mdsg.wikidata.WikidataService;

@RestController
@RequestMapping("/api/wikidata")
public class WikidataSearchController {

	private final WikidataService wikidataService;

	public WikidataSearchController(WikidataService wikidataService) {
		this.wikidataService = wikidataService;
	}

	@GetMapping("/films/search")
	public List<FilmSearchResult> searchFilms(@RequestParam("q") String query) {
		return wikidataService.searchFilms(query);
	}

}
