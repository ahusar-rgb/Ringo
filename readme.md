<p>
    <img src="./images/Ringo-White.png" width=50 height=50 alt="image"/>
    <span style="color:white;font-weight:500;font-size:40px;font-family: '.AppleSystemUIFont',serif">
        Ringo
    </span>
    <sub style="color:white;font-weight:100;font-size:15px;font-family: '.AppleSystemUIFont',serif">
        Events
    </sub>
</p>

---
## What is Ringo?
**Ringo** is an _event sharing & ticketing_ platform that allows organisations to create and share their events.  

It is user-experience focused and aims to provide a seamless experience for both the event organisers and the attendees.
Users can search for events, view event details, save events, purchase tickets and more.

The application consists of a **web app** and a **mobile app**. The web app is used by the event organisers to create and manage their events. 
The mobile app is used by the participants to view and purchase tickets for the events. 

The platform also provides a dedicated app for scanning and validating tickets.

Some of the features of the application are:
- Sign-in using Google / Apple
- Event search by distance to the user's location
- Support for multiple currencies (with automatic conversion when searching)
- Support for multiple ticket types (early bird, regular, etc.)
- Support for registration forms for events
- Users are able to review and rate organisers

---
## Technical aspects

> **_NOTE:_**  As we worked on Ringo as a team with my friends, this repository only contains my contributions to the project (Backend).

The application's backend is written in Java using the Spring Boot framework. 
We use PostgreSQL as the database and Amazon S3 for storing images. Payments are handled using _Stripe_.

Some of the features are:
- Session-less authentication using JWT tokens
- Email verification
- Ticket QR generation and validation using secure, signed tokens
- Admin panel for managing categories, currencies, etc.

---
## Tech stack

### Backend
<img src="./images/java.svg" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Java
</span>
 &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160;
<img src="./images/spring-boot.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Spring Boot
</span> <br><br>
<img src="./images/postgres.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    PostgreSQL
</span> &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160;
<img src="./images/amazon-s3.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Amazon S3
</span>

### Web app
<img src="./images/javascript.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    JavaScript
</span>
 &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160;
<img src="./images/vue.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Vue.js
</span>

### Mobile app
<img src="./images/flutter.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Flutter
</span>
 &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160; &#160;
<img src="./images/dart-logo.png" width=20 height=20 alt="vue"/>
<span style="color:white;font-weight:100;font-size:20px;font-family: '.AppleSystemUIFont',serif">
    Dart
</span>

---
## Screenshots

### Mobile app
<img src="./images/screenshots/Feed.png" width=400/>
<img src="./images/screenshots/Event.png" width=400/>
<img src="./images/screenshots/Event2.png" width=400/>
<img src="./images/screenshots/Search.png" width=400/>
<img src="./images/screenshots/Profile.png" width=400/>
<img src="./images/screenshots/Ticket.png" width=400/>
<img src="./images/screenshots/RegistrationForm.png" width=400/>
<img src="./images/screenshots/ContactHost.png" width=400/>

### Web app





