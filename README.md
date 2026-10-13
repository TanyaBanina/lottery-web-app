# Lottery Web Application

A full-stack web application for a fictional state lottery, built as a team project in the Software Engineering lab (Softwaretechnologie-Praktikum) at TU Dresden. Users can play the 6-out-of-49 number lottery and a football pools game (Fußballtoto) based on Bundesliga matches, manage an account balance, and join betting communities. Administrators get statistics and management tools. The interface is in German.

<img width="700" alt="homepage" src="https://github.com/user-attachments/assets/9dc2db3a-1616-49e9-ab36-14b9014956b2" />

<img width="600" alt="numbers" src="https://github.com/user-attachments/assets/ea0579a1-5487-408b-a65d-247cf73af82d" />


## Features

**Number lottery (6 aus 49)**
- Betting slips with up to 12 number sets, plus a super number
- Random number generation for a single set or the whole slip
- Single bets and recurring bets (monthly, half-yearly, yearly)
- Automated weekly draw, scheduled every Sunday at midnight
- Automatic evaluation into 12 winning categories, with payouts credited to the user's account and a notification message
- Submission closes one day before the draw; balance is checked against pending bets
- Archive of past draw results

**Football pools (Fußballtoto)**
- Betting on 1st and 2nd Bundesliga match results

**Accounts and communities**
- Registration, login, and role-based access (user / admin)
- Account balance, transaction history, and notifications
- Betting communities where members place bets together and share winnings

**Admin**
- Revenue, payout, and betting slip statistics per draw
- Manual draw trigger, match result management, user management

## My contributions

This was a team project. I was responsible for:

- The complete **number lottery** module: betting slips, draw mechanics, winning evaluation, payouts, and recurring bets
- The **base class and inheritance structure** for betting slips shared by Lotto and Toto
- **Front-end design** of the home page and the lottery pages
- **Unit tests** for the lottery module
- **Javadoc documentation** for all lottery classes
- Much of the **admin page functionality**

## Tech stack

- Java 21, Spring Boot, Spring Security, Spring Data JPA
- Salespoint framework
- Thymeleaf, HTML, CSS
- Maven, JUnit
- GitHub Actions (CI) and SonarQube (code quality)

## How to run

Requires Java 21.

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080.

## Documentation

The project includes a full requirements specification (Pflichtenheft, in German) with use cases, an analysis class diagram, GUI prototypes, and acceptance tests: [`src/main/asciidoc/pflichtenheft.adoc`](src/main/asciidoc/pflichtenheft.adoc).

## Credits

- Built by a team of students in the Software Engineering lab at TU Dresden.
- Based on the *Kickstart* project template provided by the Chair of Software Technology, TU Dresden, licensed under the Apache License 2.0.
