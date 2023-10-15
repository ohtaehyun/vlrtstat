package vlrtstat.gg.summoner.service;

import vlrtstat.gg.summoner.domain.Summoner;
import vlrtstat.gg.summoner.domain.SummonerProfile;

public interface SummonerService {
    Summoner searchSummoner(String summonerName);

    SummonerProfile searchSummonerProfile(String summonerName);
}