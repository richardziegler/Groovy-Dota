import groovy.json.JsonException
import groovy.json.JsonSlurper

class GetWinRate {
    static void main(String[] args) {

        def steamAPIKey = "xxx"
        def openDotaAPIKey = "xxx"

        println "   ___  ____  _________     ___  ___  ____  __________   ____\n" +
                "  / _ \\/ __ \\/_  __/ _ |   / _ \\/ _ \\/ __ \\/ __/  _/ /  / __/\n" +
                " / // / /_/ / / / / __ |  / ___/ , _/ /_/ / _/_/ // /__/ _/  \n" +
                "/____/\\____/ /_/ /_/ |_| /_/  /_/|_|\\____/_/ /___/____/___/  \n" +
                "                                                             "

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        print "Please enter your Steam Username:"
        def username = br.readLine()


        // Convert Steam Username to SteamID 32
        def usernameToId = new URL("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=${steamAPIKey}&vanityurl=${username}").getText()

        def userSlurper = new JsonSlurper()
        def userToIdJson = userSlurper.parseText(usernameToId)
        def userId64 = userToIdJson.response.steamid
        def steamId = userId64.substring(3) as Long
        steamId -= 61197960265728

        // Use generated Steam ID to pull wins and losses, then generate average
        def getWinsAndLosses = new URL("https://api.opendota.com/api/players/${steamId}/wl?api_key=${openDotaAPIKey}").getText()
        def jsonSlurper = new JsonSlurper()
        def winsAndLossesObject

        try {
            winsAndLossesObject = jsonSlurper.parseText(getWinsAndLosses)
        } catch (JsonException e) {
            println "URL is not valid"
            throw e
        }
        def totalGamesPlayed = winsAndLossesObject.win + winsAndLossesObject.lose
        def averageGames = (winsAndLossesObject.win / totalGamesPlayed) * 100

        // Get Player Profile
        def getPlayerProfile = new URL("https://api.opendota.com/api/players/${steamId}?api_key=${openDotaAPIKey}").getText()
        def playerProfileSlurper = new JsonSlurper()
        def playerProfileJson = playerProfileSlurper.parseText(getPlayerProfile)



        // Get Recent Matches
        def getRecentMatches = new URL("https://api.opendota.com/api/players/${steamId}/recentMatches?api_key=${openDotaAPIKey}").getText()
        def matchSlurper = new JsonSlurper()
        def recentMatchesJson = matchSlurper.parseText(getRecentMatches)
        // Pulling out Recent Heroes Played
        def recentHeroList = recentMatchesJson.hero_id[0..4].collect()
        // List starts at 0, Json starts at 1. Subtracting 1 from the hero ID here so it matches the Hero Stats List
        def recentHeroListMinusOne = recentHeroList.collect({it - 1})


        // Get Hero Stats List
        def getHeroList = new URL ("https://api.opendota.com/api/heroStats?api_key=${openDotaAPIKey}").getText()
        def heroSlurper = new JsonSlurper()
        def heroListJson = heroSlurper.parseText(getHeroList)
        // Put the hero names in a list and join them by commas as a string - Nicer formatting in console
        def lastFiveHeroesPlayed = []
        for (int num : recentHeroListMinusOne) {
            lastFiveHeroesPlayed.add(heroListJson.localized_name[num])
        }

        String lastFiveHeroesPlayedStr = lastFiveHeroesPlayed.join(", ")




        println "====================================================================================="
        println "Display Name: ${playerProfileJson.profile.personaname}"
        println "You have won ${winsAndLossesObject.win} games of Dota 2 and lost ${winsAndLossesObject.lose} games of Dota 2."
        println "Your win rate in is: ${averageGames.trunc(2)}%"

        print "Your 5 most recent heroes played were: "
        print lastFiveHeroesPlayedStr
        println ""
        if (playerProfileJson.profile.last_login != null) {
            println "Last Login: ${playerProfileJson.profile.last_login[0..9]}"
        } else {
            println "Last Login: Data not available."
        }
        println "====================================================================================="


    }
}
