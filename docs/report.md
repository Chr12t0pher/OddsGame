# Analysis

## Background

From my original specification:

> OddsGame will be an Android application that allows users to play the game commonly known as "Odds On" - this is where one player presents a motion to do something to another player, along with the 'odds' of doing so. Next, if both users agree on the motion and the chosen odds, both players count down from 3, and then shout out a number between 1 and the chosen odds. Should they match, then the player who agreed to play is expected to carry out the motion. Alternatively, if the numbers don't match then the latter player has the option of 'reversing' the odds on the proposing player - in this case, the previous step is repeated again. If after this the players did not match, then the game ends.
>
> Playing the game in person is easy as it's quite obvious that both players have said their number at the same time, however when playing remotely (for example through an online chat) - issues with latency and human honesty occur as there's no reliable way of knowing if a player sent their number before seeing the others'.

I chose this proposal as it would allow me to use a large subset of the Android ecosystem as well as other app-development technologies that are prevalent in the industry such as Google's Firebase. COA256 Object Oriented Programming coursework aside, this is my first significant Java project and has allowed me to become acquainted with the language in advance of going out on placement, where I'll be mainly programming in Java.

## Specification

> The application will allow users to...
>
> * create a new 'Odds On' against a certain user.
> * create a new 'Odds On' with a selectable odds.
> * create a new 'Odds On' with a custom message.
> * open an 'Odds On' after receiving a request.
> * choose a number, keeping it secret from the other user.
> * see the status of the other user in the game, be it choosing or locked in.
> * see the numbers chosen by each player once they have both chosen.
> * see clearly whether there was a winner, and if so who.
> * be able to reverse the odds if there was not a match on the first attempt.
> * share a winning result to social media (eg. Facebook Messenger).

---

# Design

## App Screens

## Data Structures

The majority of the data in _OddsGame_ is stored in Firebase using their real-time NoSQL database service, _FireStore_.

#### `odds` Firestore Table

| Name     | Type    | Use                                        | Example Data                   |
|----------|---------|--------------------------------------------|--------------------------------|
| a_id     | String  | The user ID of player A                    | `GobVaaqOdHas1PdaURMPY8ga5fr2` |
| a_name   | String  | The name of player A                       | `Chris Stevens`                |
| a_odds   | Int     | The odds choice of player A                | `-1`, `7`                      |
| b_id     | String  | The user ID of player B                    | `GobVaaqOdHas1PdaURMPY8ga5fr2` |
| b_name   | String  | The name of player B                       | `Chris Stevens`                |
| b_odds   | Int     | The odds choice of player B                | `-1`, `7`                      |
| message  | String  | The message for the odds                   | `Pay for tonights takeaway`    |
| odds     | Int     | The maximum odds that can be chosen        | `22`                           |
| reversed | Boolean | Whether this game has been reversed or not | `false`                        |

#### `users` Firestore Table

| Name     | Type   | Use                                                     | Example Data                                      |
|----------|--------|---------------------------------------------------------|---------------------------------------------------|
| name     | String | The display name of the user                            | `Chris Stevens`                                   |
| imgUri   | String | The URL of the users profile image                      | `https://lh5.googleusercontent.com/.../photo.jpg` |
| fcmToken | String | The Firebase Cloud Messaging token for the users device | `fkquLaO2_KG:emSI2km...`                          |



## Application Structure

_OddsGame_ is made up of only two activities, `Container` and `SignIn` - all of the actual screens are fragments that are then loaded into the `Container` screen.

---

# Testing

---

# Evaluation

## Evaluation of Test Results

## Evaluation against Specification

## Future Improvements
