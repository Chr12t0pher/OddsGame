# Introduction

From my original specification:

> OddsGame will be an Android application that allows users to play the game commonly known as "Odds On" - this is where one player presents a motion to do something to another player, along with the 'odds' of doing so. Next, if both users agree on the motion and the chosen odds, both players count down from 3, and then shout out a number between 1 and the chosen odds. Should they match, then the player who agreed to play is expected to carry out the motion. Alternatively, if the numbers don't match then the latter player has the option of 'reversing' the odds on the proposing player - in this case, the previous step is repeated again. If after this the players did not match, then the game ends.
>
> Playing the game in person is easy as it's quite obvious that both players have said their number at the same time, however when playing remotely (for example through an online chat) - issues with latency and human honesty occur as there's no reliable way of knowing if a player sent their number before seeing the others'.

I chose this proposal as it would allow me to use a large subset of the Android ecosystem as well as other app-development technologies that are prevalent in the industry such as Google's Firebase. COA256 Object Oriented Programming coursework aside, this is my first significant Java project and has allowed me to become acquainted with the language in advance of going out on placement, where I'll be mainly programming in Java.

---

# Implementation

## Application Level

_OddsGame_ is made up of 6 distinct screens, and as you can see in Appendix 1, I have designed my application to have a simple user interface that is easy to use and coherant across all the screens.

#### Login Page

When first opening my application, users are shown the login page where they can choose to either sign in using their Google or Facebook accounts - this is done in order to allow a unique user ID to be assigned to them within Firebase (more on that later). Once the user has signed in, the full functionality of _OddsGame_ is accessible.

#### New Game

From the New Game screen (Appx. 1 Fig. 1), the user can choose an opponent from the list of _OddsGame_ users, select their chosen odds from 2 through to 50, and write a brief message explaining what it is the odds game is for.

The choice to set the maximum odds at 50 is purely personal, there is no technical limit however if the maximum was any higher then the usability of the slider would suffer as users wouldn't have much granularity towards the lower end of the scale. A solution for this in the future could be to use a logarithmic scale instead of a linear scale on the slider.

Another time-saving measure to keep the scope of the project managable was to simply show all _OddsGame_ users, rather than including a way to filter or otherwise narrow down (eg. friends list) the list of users. This is fine for a proof-of-concept application, however if I was to release the app publically then this would need to be implemented.

#### In Game

The In Game screen (Appx. 1 Fig. 2) is the focal point of the application, it makes use of animations to dynamically change the content displayed within it depending on the game state, for example showing the 'Reverse' button only if the players haven't matched odds and the game hasn't yet been reversed. They are also used to build tension, slowly revealing the result of the game as opposed to simply displaying it instantly.

I chose to use a Number Picker for inputting the users chosen odds as it is much more fluid than typing in a number, although it does still allow for manual input which helps make the app accessible for users who may not be able to use guestures, or just if the maximum odds is very high.

In both the New and In Game screens, buttons are greyed out and disabled as soon as you user presses them - this prevents inadvertant requests being made to the database which could put the server and client out of synchronisation. It also serves to show the user that their input has been acknowledged.

#### Game Requests

The Game Requests screen shows a list of all games where the user has not yet chosen their odds, it includes all the details about the game including 

## Programming Level

### Application Structure

### Data Structures

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

#### `gamehistory` SQLite Table

| Name      | Type    | Use                                                        | Example Data                                    |
|-----------|---------|------------------------------------------------------------|-------------------------------------------------|
| uid       | varchar | Document ID of the Odds in the Firestore `odds` collection | `fYDjuFYwCyD9Fhjur827`                          |
| opponent  | varchar | Name of the opponent the user played against               | `Chris Stevens`                                 |
| message   | varchar | The message for the odds                                   | `Pay for tonights takeaway`                     |
| odds      | integer | The maximum odds that could be chosen                      | `22`                                            |
| won       | boolean | Whether the user won the game or not                       | `false`                                         |
| list_text | varchar | The text to be displayed in the Game History listview      | `Lost odds of 22 to pay for tonights takeaway.` |

---

# Testing

---

# Evaluation

## Evaluation of Test Results

## Evaluation against Specification

## Future Improvements

---

## Appendix 1 - Screens

![New Game Screen](https://img.cstevens.biz/gd9cyfs2k8.png)

![In Game Screen](https://img.cstevens.biz/rcl76sm5y8.png)

![In Game Screen, other player has 'locked in'](https://img.cstevens.biz/dsyfc41hag.png)

![In Game Screen, local player has 'locked in'](https://img.cstevens.biz/zmi3e5eo5f.png)

![In Game Screen, both players lock in and result revealed](https://img.cstevens.biz/5wn32s9jgx.png)

![In Game Screen, odds have matched and winner revealed](https://img.cstevens.biz/92glxgob2d.png)

![OddsGame Notification, clicking opens In Game Screen](https://img.cstevens.biz/vw73u2nj6r.png)

![Game Requests Screen, clicking any opens In Game Screen](https://img.cstevens.biz/mgkclgggqf.png)

![Game History Screen, clicking any opens sharing menu](https://img.cstevens.biz/3nbvmlowu5.png)

![Game History Screen sharing menu](https://img.cstevens.biz/oxbba4pffa.png)

![Sharing Intent](https://img.cstevens.biz/4k8bb3rl1l.png)

![User Guide Screen](https://img.cstevens.biz/w3d03ee204.png)

![Menu Drawer](https://img.cstevens.biz/hvmswbv0g3.png)

