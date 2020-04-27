# iMetU

On of the biggest challenges that came up in current COVID-19 pandemic is tracking of who came in contact with whom to predict who can be infected. Social distancing has become a must in such times and thus there is need of an app that can tell us when and where it is safe to be. 

We developed iMetU app to solve this problem to some extent by using location data. The app is very easy to use and does not requires any login.

A user can search for a location and then check how many people are there nearby that place and how much is the average distance between them. If the distance is less than  6ft then the app alerts the user that it is not safe to go. Also, the user can check the same for his/her current location.

No login or registration needed, just download and start using. It is that simple. No privacy issues as the user can control when to share his #locationdata  and when to stop by a simple click of a button.

In the pipeline is an admin panel, which can access all the historical data stored by #androidapp in #firebase. This is useful to identify those app users who might have been infected by coming in proximity to an identified infected person. Admin can then send alerts to them to isolate themselves.

Use case scenario:

  Larry decides to go for groceries. But he wants to know if the place is crowded.
  
  Larry opens app and put in destination location in search bar.
  
  After map shows the marker with the location searched, Larry presses "Get Neighbours" button. It displays how many people are there in the vicinity of his destination shop. Based on the informationapp suggests if it is OK to go now or not.
  
  Larry searches again and finds it is safe now to go to the shop.

  Larry moves out of his home for groceries.
  
  He starts location sharing by pressing "Start_Sharing_location" button.
  
  When he reaches store, he presses "Get Neighbours" button to know how many people are in his close proximity.
  
  If there are too many people withing close proximity, app alerts Larry to move away from the place.
  
  Larry reaches home and stops location sharing using "Stop_Sharing_Location" button.
  
  USP of our app is that no login is needed to use. App uses device-ids to differentiate between users. We have a Search option, so that user can search a location and can check beforehand before going, how many people are there and what is the average proximity between two people. If the average proximity is less than 500m, app suggests not to go.
