# Guild Tracker

A Kotlin-based microservice designed to track and manage guild information. This service retrieves guild and character data, updates a Google Sheet with the latest item levels, and sends notifications to a Discord channel via a webhook. It's built using the Micronaut framework and deployed as an AWS Lambda function for scalability and cost-effectiveness.

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Deployment](#deployment)
- [Contributing](#contributing)
    - [Possible Extension Tasks](#possible-extension-tasks)
- [License](#license)
- [Contact](#contact)

---

## Features

- **Data Retrieval from Blizzard API**: Fetches guild roster and character item levels.
- **Google Sheets Integration**: Updates a Google Sheet with the latest character information.
- **Discord Notifications**: Sends updates to a Discord channel via a webhook.
- **AWS Lambda Deployment**: Runs as a serverless function for efficient resource utilization.
- **Micronaut Framework**: Leverages Micronaut for building lightweight and fast applications.
- **Kotlin Coroutines**: Uses coroutines for asynchronous programming and improved performance.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 17** or higher.
- **Gradle** build tool.
- **AWS Account** with permissions to deploy Lambda functions.
- **Blizzard Developer Account** for API access.
- **Google Cloud Platform (GCP) Account** for Google Sheets API.
- **Discord Account** with permissions to create webhooks.

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/yourusername/guild-tracker-microservice.git
   cd guild-tracker-microservice

2. **Set Up Environment Variables**
Configure the necessary environment variables in your AWS Lambda function or local .env file:

* ``BLIZZARD_CLIENT_ID``: Your Blizzard API client ID.
* ``BLIZZARD_CLIENT_SECRET``: Your Blizzard API client secret.
* ``GOOGLE_SERVICE_ACCOUNT_KEY``: Your Google service account key (JSON format).
* ``GOOGLE_SPREADSHEET_ID``: The ID of the Google Sheet to update.
* ``DISCORD_WEBHOOK_URL``: The Discord webhook URL.

3. **Build the Project** 

```./gradlew clean shadowJar```

---

### Configuration

* **Blizzard API Credentials**
Obtain your client ID and client secret from the Blizzard Developer Portal.
* **Google Service Account**
  * Create a service account in GCP with access to the Google Sheets API.
  * Share your Google Sheet with the service account's email.
  * Download the service account key JSON file and set it as the GOOGLE_SERVICE_ACCOUNT_KEY.
* **Discord Webhook**
  * Create a webhook in your Discord server.
  * Set the webhook URL as the DISCORD_WEBHOOK_URL.
* **AWS Lambda Settings**
  * **Runtime**: Java 17
  * **Handler**: Specify the fully qualified class name and method, e.g., guild.tracker.Function::handleRequest.

---

### Usage

The application performs the following steps when triggered:

1. **Fetch Guild Roster**
Retrieves the list of guild members from the Blizzard API.
2. **Get Character Item Levels**
For each guild member, fetches their current item level.
3. **Update Google Sheet**
Updates the specified Google Sheet with the latest character item levels.
4. **Send Discord Notification**
Posts an update to a Discord channel via a webhook.

---

### Deployment

## AWS Lambda Deployment
The application is designed to be deployed as an AWS Lambda function.

1. **Build the Fat JAR**
```./gradlew clean shadowJar```
2. **Upload to AWS Lambda**
* Create a new Lambda function or update an existing one.
* Upload the generated JAR file from build/libs/.
3. **Configure the Lambda Function**
* Set the handler to guild.tracker.Function::handleRequest.
* Set the runtime to Java 17.
* Configure the necessary environment variables.

## Automated Deployment with GitHub Actions
A GitHub Actions workflow is set up to automate the build and deployment process upon merges to the ``main`` branch.

--- 

### Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## Possible Extension Tasks
* **Error Handling Improvements**
Implement more robust error handling and retry mechanisms for API calls.
* **Unit and Integration Tests**
Add comprehensive unit and integration tests to improve code reliability.
* **Database Integration**
Store guild and character data in a database for historical tracking and analysis.
* **Add pvp ratings** 
* **Add mythic plus ratings**
* **Add guild event creation integration**
* **Add guild participation statistics**

---

### License

This project is licensed under the MIT License. See the LICENSE file for details.

---

### Contact

For questions or support, please open an issue in the repository or contact sage.stainsbys@gmail.com

---

