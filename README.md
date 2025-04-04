# Environmental License Automation System for Coopavel

## Overview

This project was developed during a hackathon and aims to automate the entire process of managing environmental license documents for Coopavel, a major Brazilian cooperative. The system eliminates the need for manual tracking via spreadsheets and ensures seamless control over license expiration dates and compliance requirements.

## Problem Statement

Coopavel's current process for handling environmental licenses involves several manual steps:
1. Responsible personnel receive license documents via email.
2. They manually open the emails, review the licenses, and extract key information such as expiration dates.
3. The extracted data is then recorded in an Excel spreadsheet to track expiration dates and compliance requirements.

This manual process is prone to errors, time-consuming, and inefficient. Failure to comply with license requirements or missing expiration dates can result in fines, operational disruptions, financial losses, and other penalties.

## Solution

The Environmental License Automation System automates the entire workflow, ensuring accuracy, efficiency, and compliance. The system performs the following tasks:
1. **Email Integration**: Automatically reads incoming emails containing environmental licenses.
2. **Document Parsing**: Scans the attached documents to extract key information such as expiration dates, names, titles, and compliance requirements.
3. **Data Storage**: Saves the extracted information in a dedicated system, eliminating the need for spreadsheets.
4. **Expiration Alerts**: Monitors license expiration dates and sends automated email notifications to responsible personnel when deadlines are approaching.
5. **Status Tracking**: Provides a clear status for each license, ensuring proactive management and compliance.

## Features

- **Automated Email Processing**: Reads and processes incoming emails to identify relevant license documents.
- **Document Analysis**: Extracts critical information such as dates, names, and compliance requirements from the documents.
- **Centralized Data Management**: Stores all license information in a structured system for easy access and tracking.
- **Expiration Notifications**: Sends timely alerts to responsible personnel to prevent missed deadlines.
- **Compliance Monitoring**: Ensures adherence to environmental regulations, reducing the risk of fines and operational disruptions.

## Benefits

- **Efficiency**: Eliminates manual data entry and tracking, saving time and resources.
- **Accuracy**: Reduces human errors in managing license information.
- **Proactive Compliance**: Ensures deadlines are met and compliance requirements are fulfilled.
- **Cost Savings**: Prevents fines and operational disruptions caused by missed deadlines or non-compliance.

## How It Works

1. The system integrates with the email inbox of Coopavel's responsible personnel.
2. It scans incoming emails for environmental license documents.
3. Using document parsing techniques, it extracts relevant information such as expiration dates, names, and compliance requirements.
4. The extracted data is stored in a centralized system, replacing the need for spreadsheets.
5. As expiration dates approach, the system updates the status of each license and sends automated email reminders to ensure timely action.

## Technologies Used

- **Backend**: Java and Spring Boot
- **Database**: PostgreSQL
- **Data Storage**: MinIO
- **Frontend**: NextJS - [external repository](https://github.com/hackaton-show-rural/front-hackathon-2025)
