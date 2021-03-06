package org.kaleta.lolstats.backend.service;

import org.eclipse.jetty.util.ajax.JSON;
import org.kaleta.lolstats.backend.entity.Config;
import org.kaleta.lolstats.backend.entity.GameInfo;
import org.kaleta.lolstats.backend.entity.Player;
import org.kaleta.lolstats.backend.entity.Season;
import org.kaleta.lolstats.frontend.ErrorDialog;
import org.kaleta.lolstats.frontend.Initializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 11.02.2016.
 *
 * TODO doc.
 */
public class LolApiService {
    private final String recentMatchUrl;
    private final String champByIdUrl;
    private final String matchByIdUrl;
    private final String matchListUrl;
    private final String champListUrl;

    private final String playerId;
    private Object[] allPlayerMatches;
    private HashMap<String,String> champsMap;

    public LolApiService() {
        //todo ectract this to api manager
        Config.LolApi apiConfig = DataSourceService.getLolApiConfig();
        String region = apiConfig.getRegion();
        String apiKey = apiConfig.getAccessKey();
        String playerNick = apiConfig.getNick();
        String protocol = "https://";
        String domain = region + ".api.pvp.net";
        String keyParameter = "?api_key=" + apiKey;
        String sumByNamePath = "/api/lol/"+region+"/v1.4/summoner/by-name/";
        HashMap players = loadData(protocol + domain + sumByNamePath + playerNick + keyParameter);
        playerId = String.valueOf(((HashMap)players.get(playerNick.toLowerCase().replace(" ",""))).get("id"));

        recentMatchUrl = protocol + domain + "/api/lol/" + region + "/v1.3/game/by-summoner/" + playerId + "/recent" + keyParameter;
        champByIdUrl = protocol + "global.api.pvp.net" + "/api/lol/static-data/" + region + "/v1.2/champion/" + "{ID}" + keyParameter;
        matchByIdUrl = protocol + domain + "/api/lol/" + region + "/v2.2/match/{ID}" + keyParameter;
        matchListUrl = protocol + domain + "/api/lol/" + region + "/v2.2/matchlist/by-summoner/" + playerId + keyParameter;
        champListUrl = protocol + domain + "/api/lol/" + region + "/v1.2/champion" + keyParameter;
    }

    private HashMap loadData(String url){
        int retryCounter = 0;
        while (retryCounter < 60){
            try {
                URLConnection connection = new URL(url).openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return (HashMap) JSON.parse(sb.toString());
            } catch (IOException e) {
                if (e.getMessage().contains("Server returned HTTP response code:")){
                    try {
                        if (retryCounter < 10){
                            Thread.sleep(1000);
                        } else {
                            Thread.sleep(2000);
                        }
                    } catch (InterruptedException e1) {}
                    retryCounter++;
                } else {
                    Initializer.LOG.warning(ErrorDialog.getExceptionStackTrace(e));
                    throw new ServiceFailureException(e);
                }
            }
        }
        String msg = "time-out longer than 60 seconds!";
        Initializer.LOG.warning(msg);
        throw new ServiceFailureException(msg);
    }

    /**
     * TODO doc.
     */
    public List<GameInfo> getRecentRankedGamesInfo() {
        Object[] recentGames = (Object[]) loadData(recentMatchUrl).get("games");
        List<GameInfo> retrievedInfos = new ArrayList<>();
        for (int i = 0; i < recentGames.length; i++) {
            if ((((HashMap) recentGames[i]).get("subType")).equals("RANKED_SOLO_5x5")) {
                GameInfo info = new GameInfo();
                info.setId(String.valueOf(((HashMap)recentGames[i]).get("gameId")));
                info.setDateInMillis(String.valueOf(((HashMap)recentGames[i]).get("createDate")));
                info.setWin(String.valueOf(((HashMap)((HashMap)recentGames[i]).get("stats")).get("win")));
                info.setK(String.valueOf(((HashMap)((HashMap)recentGames[i]).get("stats")).get("championsKilled")));
                info.setD(String.valueOf(((HashMap)((HashMap)recentGames[i]).get("stats")).get("numDeaths")));
                info.setA(String.valueOf(((HashMap)((HashMap)recentGames[i]).get("stats")).get("assists")));
                retrievedInfos.add(info);
            }
        }
        return retrievedInfos;
    }

    /**
     * TODO doc.
     */
    public Season.Game getGameById(String gameId, boolean singleGameRequest){
        if (!singleGameRequest){
            if (champsMap == null){
                champsMap = new HashMap<>();
                Object[] champList = (Object[]) loadData(champListUrl).get("champions");
                for (Object champ : champList){
                    String id = String.valueOf(((HashMap) champ).get("id"));
                    HashMap champInfo = loadData(champByIdUrl.replace("{ID}",id));
                    champsMap.put(id, String.valueOf(champInfo.get("name")));
                }
            }
        }

        Season.Game gameOutput = new Season.Game();
        HashMap gameData = loadData(matchByIdUrl.replace("{ID}",gameId));

        String date = new SimpleDateFormat("ddMMyyyy").format(gameData.get("matchCreation"));
        gameOutput.setDate(date);

        String duration = new SimpleDateFormat("mmss").format(((Long)gameData.get("matchDuration"))*1000);
        gameOutput.setLength(duration);

        long userId = -1;
        int userIndex = -1;
        HashMap user = null;
        long userTeamId = -1;
        int userTeamIndex = -1;
        HashMap userTeam = null;
        List<HashMap> userMates = new ArrayList<>();
        List<HashMap> opponents = new ArrayList<>();

        Object[] identities = (Object[]) gameData.get("participantIdentities");
        for (int i=0;i<identities.length;i++){
            String sId = String.valueOf(((HashMap)((HashMap)identities[i]).get("player")).get("summonerId"));
            if (sId.equals(playerId)){
                userId = (long) ((HashMap)identities[i]).get("participantId");
                userIndex = i;
            }
        }
        if (((HashMap)((Object[])gameData.get("participants"))[userIndex]).get("participantId").equals(userId)){
            user = (HashMap)((Object[])gameData.get("participants"))[userIndex];
            userTeamId = (long) user.get("teamId");
            Object[] teams = (Object[]) gameData.get("teams");
            for (int i=0;i<teams.length;i++){
                if (((HashMap)teams[i]).get("teamId").equals(userTeamId)){
                    userTeamIndex = i;
                }
            }
        } else {
            throw new IllegalArgumentException("participant id and index mismatch");
        }
        if (((HashMap)((Object[])gameData.get("teams"))[userTeamIndex]).get("teamId").equals(userTeamId)){
            userTeam = (HashMap)((Object[])gameData.get("teams"))[userTeamIndex];
        } else {
            throw new IllegalArgumentException("team id and index mismatch " + gameId);
        }

        Object[] participants = ((Object[])gameData.get("participants"));
        for (Object participant : participants) {
            HashMap iParticipant = (HashMap) participant;
            if (iParticipant.get("teamId").equals(userTeamId)) {
                if (!iParticipant.get("participantId").equals(userId)) {
                    userMates.add(iParticipant);
                }
            } else {
                opponents.add(iParticipant);
            }
        }
        if (userMates.size() != 4){
            throw new IllegalArgumentException("found " + userMates.size() +" team mates - must be 4!");
        }
        if (opponents.size() != 5){
            throw new IllegalArgumentException("found " + opponents.size() +" opponents - must be 5!");
        }

        gameOutput.getUserTeam().setBaron(String.valueOf(userTeam.get("baronKills")));
        gameOutput.getUserTeam().setDragon(String.valueOf(userTeam.get("dragonKills")));
        gameOutput.getUserTeam().setFirstBlood(String.valueOf(userTeam.get("firstBlood")));
        gameOutput.getUserTeam().setInhibitors(String.valueOf(userTeam.get("inhibitorKills")));
        gameOutput.getUserTeam().setTurrets(String.valueOf(userTeam.get("towerKills")));
        gameOutput.getUserTeam().setWin(String.valueOf(userTeam.get("winner")));

        HashMap opponentTeam = (HashMap)((Object[])gameData.get("teams"))[((userTeamIndex + 1) % 2)];

        gameOutput.getEnemyTeam().setBaron(String.valueOf(opponentTeam.get("baronKills")));
        gameOutput.getEnemyTeam().setDragon(String.valueOf(opponentTeam.get("dragonKills")));
        gameOutput.getEnemyTeam().setFirstBlood(String.valueOf(opponentTeam.get("firstBlood")));
        gameOutput.getEnemyTeam().setInhibitors(String.valueOf(opponentTeam.get("inhibitorKills")));
        gameOutput.getEnemyTeam().setTurrets(String.valueOf(opponentTeam.get("towerKills")));
        gameOutput.getEnemyTeam().setWin(String.valueOf(opponentTeam.get("winner")));

        HashMap userStats = (HashMap) user.get("stats");

        gameOutput.getUser().getScore().setKills(String.valueOf(userStats.get("kills")));
        gameOutput.getUser().getScore().setDeaths(String.valueOf(userStats.get("deaths")));
        gameOutput.getUser().getScore().setAssists(String.valueOf(userStats.get("assists")));
        gameOutput.getUser().getScore().setMaxKillingSpree(String.valueOf(userStats.get("largestKillingSpree")));
        gameOutput.getUser().getScore().getMulti().setDouble(String.valueOf(userStats.get("doubleKills")));
        gameOutput.getUser().getScore().getMulti().setTriple(String.valueOf(userStats.get("tripleKills")));
        gameOutput.getUser().getScore().getMulti().setQuadra(String.valueOf(userStats.get("quadraKills")));
        gameOutput.getUser().getScore().getMulti().setPenta(String.valueOf(userStats.get("pentaKills")));
        gameOutput.getUser().getDmg().setTotal(String.valueOf(userStats.get("totalDamageDealt")));
        gameOutput.getUser().getDmg().setToChamps(String.valueOf(userStats.get("totalDamageDealtToChampions")));
        gameOutput.getUser().getWard().setPlaced(String.valueOf(userStats.get("wardsPlaced")));
        gameOutput.getUser().getWard().setDestroyed(String.valueOf(userStats.get("wardsKilled")));
        if (singleGameRequest){
            gameOutput.getUser().setChamp(String.valueOf(loadData(champByIdUrl.replace("{ID}",String.valueOf(user.get("championId")))).get("name")));
        } else {
            gameOutput.getUser().setChamp(champsMap.get(String.valueOf(user.get("championId"))));
        }
        gameOutput.getUser().setFarm(String.valueOf(((long) userStats.get("minionsKilled") + (long) userStats.get("neutralMinionsKilled"))));
        gameOutput.getUser().setGold(String.valueOf(userStats.get("goldEarned")));
        gameOutput.getUser().setFb(String.valueOf(userStats.get("firstBloodKill")));
        gameOutput.getUser().setTurrets(String.valueOf(userStats.get("towerKills")));
        gameOutput.getUser().setRole(((HashMap)user.get("timeline")).get("lane") +" "+((HashMap)user.get("timeline")).get("role"));

        for (HashMap userMate : userMates){
            HashMap userMateStats = (HashMap) userMate.get("stats");

            Player player = new Player();
            player.getScore().setKills(String.valueOf(userMateStats.get("kills")));
            player.getScore().setDeaths(String.valueOf(userMateStats.get("deaths")));
            player.getScore().setAssists(String.valueOf(userMateStats.get("assists")));
            player.getScore().setMaxKillingSpree(String.valueOf(userMateStats.get("largestKillingSpree")));
            player.getScore().getMulti().setDouble(String.valueOf(userMateStats.get("doubleKills")));
            player.getScore().getMulti().setTriple(String.valueOf(userMateStats.get("tripleKills")));
            player.getScore().getMulti().setQuadra(String.valueOf(userMateStats.get("quadraKills")));
            player.getScore().getMulti().setPenta(String.valueOf(userMateStats.get("pentaKills")));
            player.getDmg().setTotal(String.valueOf(userMateStats.get("totalDamageDealt")));
            player.getDmg().setToChamps(String.valueOf(userMateStats.get("totalDamageDealtToChampions")));
            player.getWard().setPlaced(String.valueOf(userMateStats.get("wardsPlaced")));
            player.getWard().setDestroyed(String.valueOf(userMateStats.get("wardsKilled")));
            if (singleGameRequest){
                player.setChamp(String.valueOf(loadData(champByIdUrl.replace("{ID}",String.valueOf(userMate.get("championId")))).get("name")));
            } else {
                player.setChamp(champsMap.get(String.valueOf(userMate.get("championId"))));
            }
            player.setFarm(String.valueOf(((long) userMateStats.get("minionsKilled") + (long) userMateStats.get("neutralMinionsKilled"))));
            player.setGold(String.valueOf(userMateStats.get("goldEarned")));
            player.setFb(String.valueOf(userMateStats.get("firstBloodKill")));
            player.setTurrets(String.valueOf(userMateStats.get("towerKills")));
            player.setRole(((HashMap)userMate.get("timeline")).get("lane") +" "+((HashMap)userMate.get("timeline")).get("role"));
            gameOutput.getUserMate().add(player);
        }


        for (HashMap opponent : opponents){
            HashMap opponentStats = (HashMap) opponent.get("stats");

            Player player = new Player();
            player.getScore().setKills(String.valueOf(opponentStats.get("kills")));
            player.getScore().setDeaths(String.valueOf(opponentStats.get("deaths")));
            player.getScore().setAssists(String.valueOf(opponentStats.get("assists")));
            player.getScore().setMaxKillingSpree(String.valueOf(opponentStats.get("largestKillingSpree")));
            player.getScore().getMulti().setDouble(String.valueOf(opponentStats.get("doubleKills")));
            player.getScore().getMulti().setTriple(String.valueOf(opponentStats.get("tripleKills")));
            player.getScore().getMulti().setQuadra(String.valueOf(opponentStats.get("quadraKills")));
            player.getScore().getMulti().setPenta(String.valueOf(opponentStats.get("pentaKills")));
            player.getDmg().setTotal(String.valueOf(opponentStats.get("totalDamageDealt")));
            player.getDmg().setToChamps(String.valueOf(opponentStats.get("totalDamageDealtToChampions")));
            player.getWard().setPlaced(String.valueOf(opponentStats.get("wardsPlaced")));
            player.getWard().setDestroyed(String.valueOf(opponentStats.get("wardsKilled")));
            if (singleGameRequest){
                player.setChamp(String.valueOf(loadData(champByIdUrl.replace("{ID}",String.valueOf(opponent.get("championId")))).get("name")));
            } else {
                player.setChamp(champsMap.get(String.valueOf(opponent.get("championId"))));
            }
            player.setFarm(String.valueOf(((long) opponentStats.get("minionsKilled") + (long) opponentStats.get("neutralMinionsKilled"))));
            player.setGold(String.valueOf(opponentStats.get("goldEarned")));
            player.setFb(String.valueOf(opponentStats.get("firstBloodKill")));
            player.setTurrets(String.valueOf(opponentStats.get("towerKills")));
            player.setRole(((HashMap)opponent.get("timeline")).get("lane") +" "+((HashMap)opponent.get("timeline")).get("role"));
            gameOutput.getOpponent().add(player);
        }

        return gameOutput;
    }

    /**
     * TODO doc.
     */
    public List<Season.Game> tryFindGames(int seasonNumber, String date, String[] queues) {
        if (allPlayerMatches == null) {
            allPlayerMatches = (Object[]) loadData(matchListUrl).get("matches");
        }
        List<Season.Game> outputGames = new ArrayList<>();
        for (int g = allPlayerMatches.length - 1; g >= 0; g--) {
            String season = "SEASON201" + seasonNumber;
            String preSeason = "PRESEASON201" + (seasonNumber+1);
            if (season.equals(((HashMap) allPlayerMatches[g]).get("season"))
                    || preSeason.equals(((HashMap) allPlayerMatches[g]).get("season"))) {
                String apiQueue = (String) ((HashMap) allPlayerMatches[g]).get("queue");
                boolean isQueue = false;
                for (int i=0;i<queues.length;i++){
                    if (queues[i].equals(apiQueue)){
                        isQueue = true;
                    }
                }
                if (isQueue){
                    if (date.equals("")){
                        long matchId = (long) ((HashMap) allPlayerMatches[g]).get("matchId");
                        try {
                            Season.Game game = getGameById(String.valueOf(matchId),false);
                            outputGames.add(game);
                        } catch (Exception e){
                            Season.Game errGame = new Season.Game();
                            errGame.setDate(new SimpleDateFormat("ddMMyyyy").format(((HashMap) allPlayerMatches[g]).get("timestamp")));
                            outputGames.add(errGame);
                            System.out.println("ERROR!!!"+g +" "+matchId+" "+e.getMessage()+" "+e.getClass().toString());
                        }
                    } else {
                        if (date.equals(new SimpleDateFormat("ddMMyyyy").format(((HashMap) allPlayerMatches[g]).get("timestamp")))) {
                            long matchId = (long) ((HashMap) allPlayerMatches[g]).get("matchId");
                            Season.Game game = getGameById(String.valueOf(matchId),false);
                            outputGames.add(game);
                        }
                    }
                }
            }
        }
        return outputGames;
    }

    /**
     * TODO doc.
     */
    public List<String> getChampionList(){
        if (champsMap == null){
            champsMap = new HashMap<>();
            Object[] champList = (Object[]) loadData(champListUrl).get("champions");
            for (Object champ : champList){
                String id = String.valueOf(((HashMap) champ).get("id"));
                HashMap champInfo = loadData(champByIdUrl.replace("{ID}",id));
                champsMap.put(id, String.valueOf(champInfo.get("name")));
            }
        }
        return new ArrayList<>(champsMap.values());
    }
}
