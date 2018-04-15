# OddsGame

OddsGame will be an Android application that allows users to play the game commonly known as "Odds On" - this is where one player presents a motion to do something to another player, along with the 'odds' of doing so. Next, if both users agree on the motion and the chosen odds, both players count down from 3, and then shout out a number between 1 and the chosen odds. Should they match, then the player who agreed to play is expected to carry out the motion. Alternatively, if the numbers don't match then the latter player has the option of 'reversing' the odds on the proposing player - in this case, the previous step is repeated again. If after this the players did not match, then the game ends.

Playing the game in person is easy as it's quite obvious that both players have said their number at the same time, however when playing remotely (for example through an online chat) - issues with latency and human honesty occur as there's no reliable way of knowing if a player sent their number before seeing the others'.

### Specification

The application will allow users to...

* create a new 'Odds On' against a certain user.
* create a new 'Odds On' with a selectable odds.
* create a new 'Odds On' with a custom message.
* open an 'Odds On' after recieving a request.
* choose a number, keeping it secret from the other user.
* see the status of the other user in the game, be it choosing or locked in.
* see the numbers chosen by each player once they have both chosen.
* see clearly whether there was a winner, and if so who.
* be able to reverse the odds if there was not a match on the first attempt.
* share a winning result to social media (eg. Facebook Messenger).

#### Notes

Whilst the mock-ups show the list of selectable users being those nearby, after further thoughts the use-case of this functionality is non-existent; if the users were nearby they could play 'Odds On' in person, which is a much better experience for the players and those around them. Instead a way of playing over the internet will be explored, most likely using WebSockets for communication between users and a central server, and with users having a unique ID they can share to allow other users to send 'Odds On' requests to them.
