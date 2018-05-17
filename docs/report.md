# Introduction

From my original specification:

> OddsGame will be an Android application that allows users to play the game commonly known as "Odds On" - this is where one player presents a motion to do something to another player, along with the 'odds' of doing so. Next, if both users agree on the motion and the chosen odds, both players count down from 3, and then shout out a number between 1 and the chosen odds. Should they match, then the player who agreed to play is expected to carry out the motion. Alternatively, if the numbers don't match then the latter player has the option of 'reversing' the odds on the proposing player - in this case, the previous step is repeated again. If after this the players did not match, then the game ends.
>
> Playing the game in person is easy as it's quite obvious that both players have said their number at the same time, however when playing remotely (for example through an online chat) - issues with latency and human honesty occur as there's no reliable way of knowing if a player sent their number before seeing the others'.

I chose this proposal as it would allow me to use a large subset of the Android ecosystem as well as other app-development technologies that are prevalent in the industry such as Google's _Firebase_. COA256 Object Oriented Programming coursework aside, this is my first significant Java project and has allowed me to become acquainted with the language in advance of going out on placement, where I'll be mainly programming in Java.

---

# Implementation

## Application Level
_OddsGame_ is made up of 6 distinct screens, and as you can see in Appendix 1, I have designed my application to have a simple user interface that is easy to use and coherent across all the screens.

#### Login Page
When first opening my application, users are shown the Login Screen (Appx. 1 Fig. TODO) where they can choose to either sign in using their Google or Facebook accounts - this is done in order to allow a unique user ID to be assigned to them within Firebase (more on that later). Once the user has signed in, the full functionality of _OddsGame_ is accessible. The Login Screen uses _FirebaseUI_, a collection of prefrab UI components, for a consist login experience that users will be familiar with elsewhere - it also allows Google Smart Lock to operate, letting users automatically sign in with Google without even seeing the login screen.

#### New Game
From the New Game screen (Appx. 1 Fig. 1), the user can choose an opponent from the list of _OddsGame_ users, select their chosen odds from 2 through to 50, and write a brief message explaining what it is the odds game is for.

The choice to set the maximum odds at 50 is purely personal, there is no technical limit however if the maximum was any higher then the usability of the slider would suffer as users wouldn't have much granularity towards the lower end of the scale. A solution for this in the future could be to use a logarithmic scale instead of a linear scale on the slider.

Another time-saving measure to keep the scope of the project manageable was to simply show all _OddsGame_ users, rather than including a way to filter or otherwise narrow down (eg. friends list) the list of users. This is fine for a proof-of-concept application, however if I was to release the app publicly then this would need to be implemented.

#### In Game
The In Game screen (Appx. 1 Fig. 2) is the focal point of the application, it makes use of animations to dynamically change the content displayed within it depending on the game state, for example showing the 'Reverse' button only if the players haven't matched odds and the game hasn't yet been reversed. They are also used to build tension, slowly revealing the result of the game as opposed to simply displaying it instantly.

I chose to use a Number Picker for inputting the users chosen odds as it is much more fluid than typing in a number, although it does still allow for manual input which helps make the app accessible for users who may not be able to use gestures, or just if the maximum odds are very high.

In both the New and In Game screens, buttons are greyed out and disabled as soon as you user presses them - this prevents inadvertent requests being made to the database which could put the server and client out of synchronisation. It also shows the user that their input has been acknowledged.

#### Game Requests
The Game Requests screen (Appx. 1 Fig. 8) shows a list of all games where the user has not yet chosen their odds, it includes all the details about the game including who it's with, the odds, and the message. Clicking any of the items in the list will open the game in the In Game screen.

#### Game History
The Game History screen (Appx. 1 Fig. 9) shows a list of all games where the user has either won or lost, it doesn't show any games that resulted in no action - while it would be easy to do, I felt that there was not much use for the user to have this data available, and would just serve to clutter the list. Clicking any of the items in this list will open the sharing menu (Appx 1. Fig. 10) which allows users to share their results with friends using any application on their device that supports sharing text.

## Programming Level

### Application Structure
At a manifest level, _OddsGame_ registers two activities, `Container` and `SignIn` - `Container` holds the menu drawer and all the fragment screens in whilst `SignIn` is just for the sign in flow, this allows me to redirect users to `SignIn` if `Container` detects that the user is not authenticated when it starts. Two services are also registered for _Firebase Cloud Messaging_ (FCM), we'll talk about that later. Finally, a `ContentProvider` named `GameHistoryProvider` is registered.

#### `Container.java`
When starting, the activity first checks to see if there is a valid _Firebase_ user, if not then the `SignIn` activity is started instead. Should there be a valid user, then the activity setup is continued and the `activity_main` view is loaded, followed by registering a listener for when an item is selected in the menu bar. Next, one of three things can happen:

* If there is `type=new_odds` in the intent extras, then the application knows that it has been started from a notification. In this case there will be an extra piece of data, `oddsId`, this is then stored into the `savedInstanceState` and the `InGameFragment` is loaded with the `oddsId` passed to it.

* If there is a fragment key in the `savedInstanceState`, restart that fragment again. This condition happens when there is a configuration change that restarts the activity, for example when the orientation changes.

* Finally, if there are no special conditions, then the `NewGameFragment` is started.

#### `NewGameFragment.java`
The fragment first queries the _FireStore_ `users` collection for a list of all users, it provides this to the `UserListAdapter` that will produce the list items for each of the users. Next, a listener is registered with the seek bar that simply updates the text view next to it with the corresponding number.

If the 'New Game' button is pressed, then the button is firstly disabled. Next, some validation is performed to ensure that the user has selected a user and provided a message before all the required data is collected into an `OddsDocument` object. This document is then added to the _FireStore_ `odds` collection and once confirmation is received, `InGameFragment` is started and the user can play the game.

#### `InGameFragment.java`
This is the most complex screen in the application, with lots of moving parts. It starts by getting a reference to the relevant document from the `odds` collection, we know which document to get as an `oddsId` has to be provided when creating a new instance of the fragment. Listeners for the 'Lock In' and 'Reverse' buttons are also registered, as well as a listener that will fire whenever the odds document is updated (eg. when someone locks in, or initiates a reverse).

When the 'Lock In' button is pressed, the odds document is updated with either `a_odds` or `b_odds` being changed from `-1` to the users' chosen number, this update triggers the previously registered listener as it listens for both local and remote changes. If there is a match then the result of the game is recorded into the local `gamehistory` SQLite database.

When the odds document updates, all the UI components (text views, check boxes, number picker, etc) are updated to match the latest copy of data from the database. If both `a_odds` and `b_odds` are greater than 0, this signifies that both players have selected a number (`-1` represents no choice) and we can reveal the result. To do so, a series of animations are used to firstly fade out the now defunct UI elements (check boxes, number picker, etc) before fading in the new elements (odds results, reverse button if applicable).

A similar flow occurs if the reverse button is pressed, with the only difference being that the odds document has the `reverse` field set to `true` - this in turn triggers the reverse animations to be triggered on the other players' device as the odds document listener checks to see if `reverse` has become true.

#### `GameRequestFragment.java`
This fragment utilises a `RecyclerView `to produce a list of games where the user has yet to enter their odds. This is done by making two separate queries on the _FireStore_ `odds` collection as, unfortunately, the service doesn't yet support `OR` queries - as a workaround I simply perform each query and then join the two resulting lists together before passing them to the `GameReqListAdapter`.

#### `GameHistoryFragment.java`
This fragment utilises a `ListView` to produce a list of games where the user has either won or lost, it's done using a custom `ContentProvider` and `Loader` in order to fulfil these specification points. A cursor adapter is used to map the database values onto the correct `TextView` in the list item.

Additionally, if the user clicks on one of the list items, a sharing chooser is opened with the details of the corresponding game included within the intent. As the type is defined as `text/plain`, a large majority of applications are able to handle this data such as email or social media apps.

#### `Util/FirebaseInstanceIDService.java`
This service calls `onTokenRefresh()` whenever the system decides that the _FCM_ token needs to be changed, for example when the app is first started or the existing token expires. This token is required by _FCM_ in order to send messages to individual devices, to store them I include them in the _FireStore_ `users` documents, at the same time the users' display name and image URL are also stored.

#### `Util/FirebaseMessagingService.java`
This service calls `onMessageReceived()` whenever a message is received from _FCM_. For _OddsGame_, these messages are informing the users' device that another user has started a game with them. The source of these messages isn't actually the initiating players device as _FCM_ doesn't allow clients to send messages, only the server can - I've solved this issue with the help of server-side code which will be explained later.

When the message is received, an intent is created with extra data `type` and `oddsId` - these are used by the `Container` activity to start the `InGameFragment` with the correct odds game. The notification is then created with an icon, the intent, title, and body. Then there is some code that registers a notification channel (a new feature in Android 8.0 Oreo) before finally the notification is displayed on the users' phone. The use of `PRIORITY_HIGH`/`IMPORTANCE_HIGH` means that the notification will be a 'heads up notification' - this means that it is actually displayed on the screen rather than just showing as an icon in the notification bar.

### Data Structures

The majority of the data in _OddsGame_ is stored in _Firebase_ using their real-time NoSQL database service, _FireStore_. I chose this as, being a Google service, _Firebase_ is extremely well supported within Android and there is an endless amount of information and help available to me. It's real-time nature also is perfect for my use-case, especially when paired with the ability to register listeners on a certain document.

Locally, I use the `OddsDocument` and `UserDocument` classes to convert from the `DocumentSnapshot` that _FireStore_ provides.

| Name     | Type    | Use                                        | Example Data                   |
|----------|---------|--------------------------------------------|--------------------------------|
| a_id     | String  | The user ID of player A                    | `GobVaaqOdHas1P...` |
| a_name   | String  | The name of player A                       | `Chris Stevens`                |
| a_odds   | Int     | The odds choice of player A                | `-1`, `7`                      |
| b_id     | String  | The user ID of player B                    | `GobVaaqOdHas1P...` |
| b_name   | String  | The name of player B                       | `Chris Stevens`                |
| b_odds   | Int     | The odds choice of player B                | `-1`, `7`                      |
| message  | String  | The message for the odds                   | `Pay for tonights takeaway`    |
| odds     | Int     | The maximum odds that can be chosen        | `22`                           |
| reversed | Boolean | Whether this game has been reversed or not | `false`                        |

Table: The `odds` Firestore table, this schema is defined locally at `biz.cstevens.oddsgame.Documents.OddsDocument`.

| Name     | Type   | Use                                                     | Example Data                                      |
|----------|--------|---------------------------------------------------------|---------------------------------------------------|
| name     | String | The display name of the user                            | `Chris Stevens`                                   |
| imgUri   | String | The URL of the users profile image                      | `https://googleusercontent.com/...` |
| fcmToken | String | The Firebase Cloud Messaging token for the users device | `fkquLaO2_KG:emSI2km...`                          |

Table: The `odds` Firestore table, this schema is defined locally at `biz.cstevens.oddsgame.Documents.UsersDocument`.

#### `gamehistory` SQLite Table

| Name      | Type    | Use                                                        | Example Data                                    |
|-----------|---------|------------------------------------------------------------|-------------------------------------------------|
| uid       | varchar | Document ID of the Odds in the Firestore `odds` collection | `fYDjuFYwCyD9Fhjur827`                          |
| opponent  | varchar | Name of the opponent the user played against               | `Chris Stevens`                                 |
| message   | varchar | The message for the odds                                   | `Pay for tonights takeaway`                     |
| odds      | integer | The maximum odds that could be chosen                      | `22`                                            |
| won       | boolean | Whether the user won the game or not                       | `false`                                         |
| list_text | varchar | The text to be displayed in the Game History listview      | `Lost odds of 22 to pay for tonights takeaway.` |

Table: The `gamehistory` SQLite table, this schema is defined locally at `biz.cstevens.oddsgame.GameHistoryDb.GameHistoryDb`

---

# Evaluation

## Evaluation against Requirements

__Mandatory Requirements__

* _Have a minimum of two distinct screens (excluding the user guide)._
  * My application has five distinct screens, plus the user guide.

* _Work properly with the app lifecycle (including rotate screen changes)._
  * My application deregisters listeners when the relevant fragment is removed from view and the state of the application is maintained on orientation changes.

* _Use permissions and use them responsibly._
  * My application uses the `INTERNET` permission.

* _Use Intents to move inside your app and to an outside app._
  * My application uses intents to start the login screen if necessary, and uses an intent to share an odds result with other applications.
 
* _Create and use your own `ContentProvider`._
  * My application uses my own `GameHistoryProvider` to interface with the SQLite database containing game results.
  
* _Create and use your own custom Loader._
  * My application


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

