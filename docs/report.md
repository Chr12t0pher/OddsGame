---
title: 'OddsGame Report (17COB155 Coursework)'
author: Christopher Stevens B620698
toc: true
geometry: a4paper
numbersections: true
header-includes: |
	\usepackage{fancyhdr}
	\pagestyle{fancy}
	\lhead{OddsGame Report (17COB155 Coursework)}
	\rhead{Christopher Stevens B620698}

---

\newpage
# Introduction

From my original specification:

> OddsGame will be an Android application that allows users to play the game commonly known as "Odds On" - this is where one player presents a motion to do something to another player, along with the 'odds' of doing so. Next, if both users agree on the motion and the chosen odds, both players count down from 3, and then shout out a number between 1 and the chosen odds. Should they match, then the player who agreed to play is expected to carry out the motion. Alternatively, if the numbers don't match then the latter player has the option of 'reversing' the odds on the proposing player - in this case, the previous step is repeated again. If after this the players did not match, then the game ends.
>
> Playing the game in person is easy as it's quite obvious that both players have said their number at the same time, however when playing remotely (for example through an online chat) - issues with latency and human honesty occur as there's no reliable way of knowing if a player sent their number before seeing the others'.

I chose this proposal as it would allow me to use a large subset of the Android ecosystem as well as other app-development technologies that are prevalent in the industry such as Google's _Firebase_. COA256 Object Oriented Programming coursework aside, this is my first significant Java project and has allowed me to become acquainted with the language in advance of going out on placement, where I'll be mainly programming in Java.

# Implementation

## Application Level
_OddsGame_ is made up of 6 distinct screens, and as you can see in Appendix 2, I have designed my application to have a simple user interface that is easy to use and coherent across all the screens.

### Login Page
When first opening my application, users are shown the Login Screen (Appx. 2 Fig. 14) where they can choose to either sign in using their Google or Facebook accounts - this is done in order to allow a unique user ID to be assigned to them within Firebase (more on that later). Once the user has signed in, the full functionality of _OddsGame_ is accessible. The Login Screen uses _FirebaseUI_, a collection of prefab UI components, for a consist login experience that users will be familiar with elsewhere - it also allows Google Smart Lock to operate, letting users automatically sign in with Google without even seeing the login screen.

### New Game
From the New Game screen (Appx. 2 Fig. 1), the user can choose an opponent from the list of _OddsGame_ users, select their chosen odds from 2 through to 50, and write a brief message explaining what it is the odds game is for.

The choice to set the maximum odds at 50 is purely personal, there is no technical limit however if the maximum was any higher then the usability of the slider would suffer as users wouldn't have much granularity towards the lower end of the scale. A solution for this in the future could be to use a logarithmic scale instead of a linear scale on the slider.

Another time-saving measure to keep the scope of the project manageable was to simply show all _OddsGame_ users, rather than including a way to filter or otherwise narrow down (eg. friends list) the list of users. This is fine for a proof-of-concept application, however if I was to release the app publicly then this would need to be implemented.

### In Game
The In Game screen (Appx. 2 Fig. 2) is the focal point of the application, it makes use of animations to dynamically change the content displayed within it depending on the game state, for example showing the 'Reverse' button only if the players haven't matched odds and the game hasn't yet been reversed. They are also used to build tension, slowly revealing the result of the game as opposed to simply displaying it instantly.

I chose to use a Number Picker for inputting the users chosen odds as it is much more fluid than typing in a number, although it does still allow for manual input which helps make the app accessible for users who may not be able to use gestures, or just if the maximum odds are very high.

In both the New and In Game screens, buttons are greyed out and disabled as soon as you user presses them - this prevents inadvertent requests being made to the database which could put the server and client out of synchronisation. It also shows the user that their input has been acknowledged.

### Game Requests
The Game Requests screen (Appx. 2 Fig. 8) shows a list of all games where the user has not yet chosen their odds, it includes all the details about the game including who it's with, the odds, and the message. Clicking any of the items in the list will open the game in the In Game screen.

### Game History
The Game History screen (Appx. 2 Fig. 9) shows a list of all games where the user has either won or lost, it doesn't show any games that resulted in no action - while it would be easy to do, I felt that there was not much use for the user to have this data available and would just serve to clutter the list. Clicking any of the items in this list will open the sharing menu (Appx 2. Fig. 10) which allows users to share their results with friends using any application on their device that supports sharing text.

## Programming Level
At a manifest level, _OddsGame_ registers two activities, `Container` and `SignIn` - `Container` holds the menu drawer and all the fragment screens in whilst `SignIn` is just for the sign in flow, this allows me to redirect users to `SignIn` if `Container` detects that the user is not authenticated when it starts. Two services are also registered for _Firebase Cloud Messaging_ (FCM), we'll talk about that later. Finally, a `ContentProvider` named `GameHistoryProvider` is registered.

### `Container.java`
When starting, the activity first checks to see if there is a valid _Firebase_ user, if not then the `SignIn` activity is started instead. Should there be a valid user, then the activity setup is continued and the `activity_main` view is loaded, followed by registering a listener for when an item is selected in the menu bar. Next, one of three things can happen:

* If there is `type=new_odds` in the intent extras, then the application knows that it has been started from a notification. In this case there will be an extra piece of data, `oddsId`, this is then stored into the `savedInstanceState` and the `InGameFragment` is loaded with the `oddsId` passed to it.

* If there is a fragment key in the `savedInstanceState`, restart that fragment again. This condition happens when there is a configuration change that restarts the activity, for example when the orientation changes.

* Finally, if there are no special conditions, then the `NewGameFragment` is started.

### `NewGameFragment.java`
The fragment first queries the _FireStore_ `users` collection for a list of all users, it provides this to the `UserListAdapter` that will produce the list items for each of the users. Next, a listener is registered with the seek bar that simply updates the text view next to it with the corresponding number.

If the 'New Game' button is pressed, then the button is firstly disabled. Next, some validation is performed to ensure that the user has selected a user and provided a message before all the required data is collected into an `OddsDocument` object. This document is then added to the _FireStore_ `odds` collection and once confirmation is received, `InGameFragment` is started and the user can play the game.

### `InGameFragment.java`
This is the most complex screen in the application, with lots of moving parts. It starts by getting a reference to the relevant document from the `odds` collection, we know which document to get as an `oddsId` has to be provided when creating a new instance of the fragment. Listeners for the 'Lock In' and 'Reverse' buttons are also registered, as well as a listener that will fire whenever the odds document is updated (eg. when someone locks in or initiates a reverse).

When the 'Lock In' button is pressed, the odds document is updated with either `a_odds` or `b_odds` being changed from `-1` to the users' chosen number, this update triggers the previously registered listener as it listens for both local and remote changes. If there is a match, then the result of the game is recorded into the local `gamehistory` SQLite database.

When the odds document updates, all the UI components (text views, check boxes, number picker, etc) are updated to match the latest copy of data from the database. If both `a_odds` and `b_odds` are greater than 0, this signifies that both players have selected a number (`-1` represents no choice) and we can reveal the result. To do so, a series of animations are used to firstly fade out the now defunct UI elements (check boxes, number picker, etc) before fading in the new elements (odds results, reverse button if applicable).

A similar flow occurs if the reverse button is pressed, with the only difference being that the odds document has the `reverse` field set to `true` - this in turn triggers the reverse animations to be triggered on the other players' device as the odds document listener checks to see if `reverse` has become true.

### `GameRequestFragment.java`
This fragment utilises a `RecyclerView `to produce a list of games where the user has yet to enter their odds. This is done by making two separate queries on the _FireStore_ `odds` collection as, unfortunately, the service doesn't yet support `OR` queries - as a workaround I simply perform each query and then join the two resulting lists together before passing them to the `GameReqListAdapter`.

### `GameHistoryFragment.java`
This fragment utilises a `ListView` to produce a list of games where the user has either won or lost, it's done using a custom `ContentProvider` and `Loader` in order to fulfil these specification points. A cursor adapter is used to map the database values onto the correct `TextView` in the list item.

Additionally, if the user clicks on one of the list items, a sharing chooser is opened with the details of the corresponding game included within the intent. As the type is defined as `text/plain`, a large majority of applications are able to handle this data such as email or social media apps.

### `Util/DownloadImageTask.java`
This class downloads a requested image asynchronously in a different thread, this therefore prevents the main UI thread from hanging whilst waiting for the (relatively) slow image download - the UI remains responsive and the image is added to the interface once it has been downloaded in the background.

### `Util/FirebaseInstanceIDService.java`
This service calls `onTokenRefresh()` whenever the system decides that the _FCM_ token needs to be changed, for example when the app is first started or the existing token expires. This token is required by _FCM_ in order to send messages to individual devices, to store them I include them in the _FireStore_ `users` documents, at the same time the users' display name and image URL are also stored.

### `Util/FirebaseMessagingService.java`
This service calls `onMessageReceived()` whenever a message is received from _FCM_. For _OddsGame_, these messages are informing the users' device that another user has started a game with them. The source of these messages isn't actually the initiating players device as _FCM_ doesn't allow clients to send messages, only the server can - I've solved this issue with the help of server-side code that is run within _Google Cloud Functions_.

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendOddsNotification = functions.firestore
    .document("odds/{oddsId}").onCreate((snapshot, context) => {
        const oddsData = snapshot.data();
        const oddsId = context.params.oddsId;
        const notifiedUser = oddsData.b_id;

        admin.firestore().collection("users").doc(notifiedUser).get().then(doc => {
            const fcmToken = doc.data().fcmToken;
            return {
                token: fcmToken,
                data: {
                    type: "new_odds",
                    oddsId: oddsId,
                    title: `${oddsData.a_name} has odds'd you!`,
                    body: `Odds of ${oddsData.odds} to ${oddsData.message}`
                },
                android: {
                    priority: "high"
                }
            };
        }).then(message => {
            return admin.messaging().send(message);
        }).then(res => {
            console.log("Success: ", res);
            return true;
        }).catch(err => {
            console.log("Failure: ", err);
            return false;
        });
    });
```

Whenever a new odds document is created in _FireStore_, this code is fired which gets the _FCM_ token of the user who needs to be notified and creates a message with the data needed for the recipient to join the game. When the message is received on the recipient device, an intent is created with extra data `type` and `oddsId` - these are used by the `Container` activity to start the `InGameFragment` with the correct odds game. The notification is then created with an icon, the intent, title, and body. Then there is some code that registers a notification channel (a new feature in Android 8.0 Oreo) before finally the notification is displayed on the users' phone. The use of `PRIORITY_HIGH`/`IMPORTANCE_HIGH` means that the notification will be a 'heads up notification' - this means that it is actually displayed on the screen rather than just showing as an icon in the notification bar.

## Data Structures

The majority of the data in _OddsGame_ is stored in _Firebase_ using their real-time NoSQL database service, _FireStore_. I chose this as, being a Google service, _Firebase_ is extremely well supported within Android and there is an endless amount of information and help available to me. It's real-time nature also is perfect for my use-case, especially when paired with the ability to register listeners on a certain document.

It would have been possible to decentralise the application and have the devices store all the data themselves, communicating via the Google Nearby API or my own bespoke application server as I suggested originally. However, this would've taken significantly longer and the final application would not have been as good - _Firebase_ has abstracted a lot of the server-side work for me which has allowed me to focus on developing the core application.

Locally, I use the `OddsDocument` and `UserDocument` classes to convert from the `DocumentSnapshot` that _FireStore_ provides.

| Name     | Type    | Use                                        | Example Data                   |
|----------|---------|--------------------------------------------|--------------------------------|
| a_id     | String  | The user ID of player A                    | `GobVaaqOdHas1P...`            |
| a_name   | String  | The name of player A                       | `Chris Stevens`                |
| a_odds   | Int     | The odds choice of player A                | `-1`, `7`                      |
| b_id     | String  | The user ID of player B                    | `GobVaaqOdHas1P...`            |
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

| Name      | Type    | Use                                                        | Example Data                                    |
|-----------|---------|------------------------------------------------------------|-------------------------------------------------|
| uid       | varchar | Document ID of the Odds in the Firestore `odds` collection | `fYDjuFYwCyD9Fhjur827`                          |
| opponent  | varchar | Name of the opponent the user played against               | `Chris Stevens`                                 |
| message   | varchar | The message for the odds                                   | `Pay for tonights takeaway`                     |
| odds      | integer | The maximum odds that could be chosen                      | `22`                                            |
| won       | boolean | Whether the user won the game or not                       | `false`                                         |
| list_text | varchar | The text to be displayed in the Game History listview      | `Lost odds of 22 to pay for tonights takeaway.` |

Table: The `gamehistory` SQLite table, this schema is defined locally at `biz.cstevens.oddsgame.GameHistoryDb.GameHistoryDb`

# Evaluation
Overall I feel that this coursework has gone very well, I had a clear vision for my application from the outset and followed through on it to completion. I used GitHub as a version control and project management system in order to keep development on track and it resulted in me completing the application in plenty of time. This therefore allowed me to get feedback from users I had released the application to and so make minor usability tweaks to make _OddsGame_ as good as possible.

As with most projects, getting the first pieces of functionality was the hardest and most time consuming - especially since I had never really programmed in Java before, and never at all in the context of Android development. Once I had completed the most technically complex parts of the application, `NewGameFragment` and `InGameFragment`, the rest of the development came along quickly as by that point I had gained familiarity with Java and Android.

My application has fulfilled all of the mandatory requirements and has fully implemented two of the optional requirements, partially implemented one further optional requirement, and has a WebView-based user guide. Making my own `ContentProvider` and using a custom Loader were the toughest parts of the mandatory requirements simply due to finding a way to fit them into my application - in the end I decided on storing wins and losses locally in a SQLite database in order to satisfy the requirement however, if I were to release the app publicly, this would also be done within _FireStore_.

__Mandatory Requirements__

* _Have a minimum of two distinct screens (excluding the user guide)_
  * My application has five distinct screens, plus the user guide.

* _Work properly with the app lifecycle (including rotate screen changes)_
  * My application deregisters listeners when the relevant fragment is removed from view and the state of the application is maintained on orientation changes.

* _Use permissions and use them responsibly_
  * My application uses the `INTERNET` permission.

* _Use Intents to move inside your app and to an outside app_
  * My application uses intents to start the login screen if necessary, and uses an intent to share an odds result with other applications.

* _Create and use your own `ContentProvider`_
  * My application uses my own `GameHistoryProvider` to interface with the SQLite database containing game results.

* _Create and use your own custom Loader_
  * My application uses my own custom Loader within the `GameHistoryFragment` to populate the associated List View.

* _Make use of local storage_
  * My application uses a SQLite database to store game histories and _Firebase_ caches data locally too.

* _Create a user guide [...] and include it as a WebView_
  * My application includes a WebView based user guide which contains a HTML webpage. It makes use of web technologies including SVGs, viewport units, and scaling images in line with the text size.

__Optional Requirements__

* _Receive Broadcast events and make use of them in meaningful ways_
  * Not implemented.

* _Create and use a Custom View_
  * Not implemented.

* _Implement `ShareActionProvider`_
  * My application uses a `ShareActionProvider` within the `GameHistoryFragment` to allow users to share their results with others.

* _Use Services_
  * My application makes use of the `FirebaseInstanceIdService` and `FirebaseMessagingService` although it does not use any custom services.

* _Use Notifications_
  * My application notifies users when a new odds game has been created with them.

* _Capture touch gestures and make reasonable use of them_
  * Not implemented.

## Evaluation of Test Results
A comprehensive test plan is extremely important in order to verify that the application I have produced is fully functional and bug-free, as can be seen in Appendix 1, I have created such a plan and validated my final application against it with complete success. All aspects of the application from the user interface to data validation are tested to ensure maximum coverage over the code-base. Using Android Studio as an IDE was a great help in preventing the occurrence of bugs or other unwanted behaviour thanks to its advanced code analysis that offers refactoring suggestions and spots potential mistakes as you type them.

If the coursework was longer, I could have implemented automated testing using tools such as _Espresso_ - by writing tests for these first and then developing the functionality of the application to pass the tests (a standard industry practise) I could be assured that my final application was fully functional without having to manually go through and test _OddsGame_.

## Evaluation against Specification
My final application fully meets all of the points in my specification and the UI design closely matches that of my original mock-ups, in this regard I would consider the project a resounding success. The only notable difference between my specification and the final product is that instead of using the Google Nearby API or WebSockets for communication between devices, _Firebase_ was chosen - this was due to offering similar functionality (a way to communicate between devices) but with a lot more extra functionality that I found useful whilst developing my project, for example _FireStore_.

---

# Appendices

## Appendix 1 - Test Plan and Results {-}

| ID    | Action                                      | Expected Outcome                                                                            | Actual Outcome                                                                              | Result  |
|-------|---------------------------------------------|---------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|---------|
| 1     | Open app for first time                     | Sign-in screen displayed                                                                    | Sign-in screen displayed                                                                    | Success |
| 2     | Attempt to leave Sign In screen             | Sign-in screen displayed                                                                    | Sign-in screen displayed                                                                    | Success |
| 3.1   | Log in with Facebook credentials            | Logged in, New Game screen displayed                                                        | Logged in, New Game screen displayed                                                        | Success |
| 3.2   | Log in with Google credentials              | Logged in, New Game screen displayed                                                        | Logged in, New Game screen displayed                                                        | Success |
| 3.3   | Log in with invalid credentials             | Not logged in, error message shown                                                          | Not logged in, error message shown                                                          | Success |
| 4.1   | Open New Game screen                        | New Game screen displayed                                                                   | New Game screen displayed                                                                   | Success |
| 4.2   | -                                           | Choose opponent list populated                                                              | Choose opponent list populated                                                              | Success |
| 4.3   | Select an opponent                          | Item highlighted                                                                            | Item highlighted                                                                            | Success |
| 4.4   | Select a different opponent                 | Only latest item highlighted                                                                | Only latest item highlighted                                                                | Success |
| 4.5   | Move slider                                 | Odds number reacts to slider changes                                                        | Odds number reacts to slider changes                                                        | Success |
| 4.6   | -                                           | Odds slider minimum is 2                                                                    | Odds slider minimum is 2                                                                    | Success |
| 4.7   | -                                           | Odds slider maximum is 50                                                                   | Odds slider maximum is 50                                                                   | Success |
| 4.8   | Press New Game without opponent selected    | Error toast is displayed                                                                    | Error toast is displayed                                                                    | Success |
| 4.9   | Press New Game without message              | Error toast is displayed                                                                    | Error toast is displayed                                                                    | Success |
| 4.10  | Press New Game with opponent selected       | In-Game screen is displayed                                                                 | In-Game screen is displayed                                                                 | Success |
| 5.1   | Open a game in In Game screen               | Odds displayed                                                                              | Odds displayed                                                                              | Success |
| 5.2   | -                                           | Message displayed                                                                           | Message displayed                                                                           | Success |
| 5.3   | -                                           | Correct players displayed                                                                   | Correct players displayed                                                                   | Success |
| 5.4   | -                                           | Correct odds range on number picker                                                         | Correct odds range on number picker                                                         | Success |
| 5.5   | Other player locks in odds                  | Green checkbox displayed, "Locked in!" displayed                                            | Green checkbox displayed, "Locked in!" displayed                                            | Success |
| 5.6   | Local player locks in odds                  | Green checkbox displayed, "Locked in!" displayed, number picker and lock in button disabled | Green checkbox displayed, "Locked in!" displayed, number picker and lock in button disabled | Success |
| 5.7.1 | Both players locked odds                    | UI fades out then result fades in                                                           | UI fades out then result fades in                                                           | Success |
| 5.7.2 | No match on first attempt                   | Reverse button fades in with result                                                         | Reverse button fades in with result                                                         | Success |
| 5.7.3 | Match on first attempt                      | Winner shown as initiating player, no reverse button                                        | Winner shown as initiating player, no reverse button                                        | Success |
| 5.7.4 | Reverse button pressed                      | UI fades out then game fades in again                                                       | UI fades out then game fades in again                                                       | Success |
| 5.7.5 | Win after reverse                           | Winner shown as receiving player, no reverse button                                         | Winner shown as receiving player, no reverse button                                         | Success |
| 5.7.6 | No match after reverse                      | No winner shown, no reverse button                                                          | No winner shown, no reverse button                                                          | Success |
| 6.1   | Open Requests screen                        | Requests screen displayed with list of games that user has not locked in odds for           | Requests screen displayed with list of games that user has not locked in odds for           | Success |
| 6.2   | -                                           | List items contain user name                                                                | List items contain user name                                                                | Success |
| 6.3   | -                                           | List items contain user profile image                                                       | List items contain user profile image                                                       | Success |
| 6.4   | -                                           | List items contain odds and message                                                         | List items contain odds and message                                                         | Success |
| 6.5.1 | Select a game                               | In-Game screen is displayed with correct game                                               | In-Game screen is displayed with correct game                                               | Success |
| 6.5.2 | Lock in odds then return to Requests screen | Game has been removed from requests screen                                                  | Game has been removed from requests screen                                                  | Success |
| 7.1   | Open History screen                         | History screen displayed with list of games user has won/lost                               | History screen displayed with list of games user has won/lost                               | Success |
| 7.2   | -                                           | List items contain user name                                                                | List items contain user name                                                                | Success |
| 7.3   | -                                           | List items contain odds and message                                                         | List items contain odds and message                                                         | Success |
| 7.4.1 | Click a history item                        | Share action provider opens with custom title                                               | Share action provider opens with custom title                                               | Success |
| 7.4.2 | Choose Gmail                                | Gmail opens with email body the result of the odds game                                     | Gmail opens with email body the result of the odds game                                     | Success |
| 8     | Open User Guide                             | User Guide screen displayed                                                                 | User Guide screen displayed                                                                 | Success |
| 9     | Press logout                                | Sign-in screen displayed                                                                    | Sign-in screen displayed                                                                    | Success |

## Appendix 2 - Screens {-}

![New Game Screen](https://img.cstevens.biz/gd9cyfs2k8.png)

![In Game Screen](https://img.cstevens.biz/rcl76sm5y8.png)

![In Game Screen, other player has 'locked in'](https://img.cstevens.biz/dsyfc41hag.png)

![In Game Screen, local player has 'locked in'](https://img.cstevens.biz/zmi3e5eo5f.png)

![In Game Screen, both players lock in and result revealed](https://img.cstevens.biz/5wn32s9jgx.png)

![In Game Screen, odds have matched and winner revealed](https://img.cstevens.biz/92glxgob2d.png)

![Notification, clicking opens In Game Screen](https://img.cstevens.biz/vw73u2nj6r.png)

![Game Requests Screen, clicking any opens In Game Screen](https://img.cstevens.biz/mgkclgggqf.png)

![Game History Screen, clicking any opens sharing menu](https://img.cstevens.biz/3nbvmlowu5.png)

![Game History Screen sharing menu](https://img.cstevens.biz/oxbba4pffa.png)

![Sharing Intent](https://img.cstevens.biz/4k8bb3rl1l.png)

![User Guide Screen](https://img.cstevens.biz/w3d03ee204.png)

![Menu Drawer](https://img.cstevens.biz/hvmswbv0g3.png)

![Sign-in Screen](https://img.cstevens.biz/pggr87t57p.png)
