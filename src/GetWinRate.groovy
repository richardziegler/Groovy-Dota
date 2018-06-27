import groovy.json.*

class GetWinRate {
    static void main(String[] args) {

        def steamAPIKey
        def openDotaAPIKey

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


        def test = new URL("https://api.opendota.com/api/players/${steamId}/wl?api_key=${openDotaAPIKey}").getText()
        def jsonSlurper = new JsonSlurper()
        def object

        try {
            object = jsonSlurper.parseText(test)
        } catch (JsonException e) {
            println "URL is not valid"
            throw e
        }

        def totalGamesPlayed = object.win + object.lose
        def averageGames = (object.win / totalGamesPlayed) * 100

        println "You have won ${object.win} games of Dota 2 and lost ${object.lose} games of Dota 2."
        println "Your win rate in is: ${averageGames.trunc(2)}%"


    }
}
