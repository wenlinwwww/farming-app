# AQ-Bluering

### Project Overview
In this project, we need to create an Android mobile application for our client AquaTerra Solutions Pty Ltd.  We aim to design a user-friendly mobile application so that users can conveniently access soil moisture and temperature data. Our mobile application's core functions will focus on user registration and login, field monitoring, sensor integration, gateway connectivity, and profile management.

To better develop the mobile application, we have a team with five members: Product Owner, Scrum Master, Front-end Lead, Back-end Lead, and developer. In the mobile application, we will follow the different stages of the sprint process to roll out features. At each stage, functionalities will be tested, reviewed, and redesigned based on feedback. Meeting notes will be regularly taken to track the sprint progress. 

### Client background
AquaTerra Solutions Pty Ltd
webapp: https://aqua-terra.com.au/index.html

Aquaterra is at the forefront of integrating intelligent farming and data technology to elevate farming operations' efficiency. Leveraging an online platform, Aquaterra allows farmers to seamlessly manage a diverse array of data collection hardware, ensuring they're equipped with critical information to make informed farming decisions.

The company's current interface is hosted as a web page, meticulously developed using AWS cloud services and backed by a robust PostgresSQL database. Customers can log onto this platform to access real-time and historical data from various zones of their fields, facilitating precise agricultural practices.

However, with the undeniable shift in user preferences, there's been a significant increase in mobile internet users, while desktop users are dwindling. Recognizing this trend, Aquaterra envisions a pivot to cater more efficiently to the mobile-centric audience, aiming to bring their services right into the hands of farmers, anytime, anywhere.

To this end, the primary objective of our project is to create an Android mobile application that mirrors the functionality of the current web platform. This app will not only ensure that Aquaterra is aligned with the digital shift but will also position the organization to offer farmers more flexible, on-the-go assistance, propelling their mobile business transformation.

### Goal
1: Support a user-friendly interface design for mobile application.

2: Meet users' needs and improve users' experience

Users can track the soil data timely.
Support historical data analysis .
Support reliable information and recommendations for users.
Users can manage field, sensor, gateway data.

### Folder Structure

**src**:
This dictionary contains the programming files and other resource files related to the final product.

```
AQ-BlueRing/
├── src/                                         # src code
    ├── App/                                    # Product App codes
    ├── Gradle/                                 # Enviornment configuration file
├── tests/
    ├── Aquaterra_v2.apk                            # product apk
    ├── Aquaterra_v1.apk
    ├── COMP90082_AQ_Bluering_CodeReview.xlsx
└── README.md
```
### Documentation and Guidelines:
- Provide detailed and clear documentation in the repository, including project structure, workflows, contribution guidelines, etc.
- Encourage the team to follow the same code styles with detailed comments which make sure the other team who take over the projects can continue the development through the documentation.

### Project Branching management policy:
To ensure the quality of the main brunch and avoid the issue caused by version management, our team agree to apply the merge commit policy:
1. No Update should be made directly on the main brunch
2. Create a new brunch when working individually
3. Frequently pull updates from the main brunch when working individually
4. No brunch merge unless quality is checked by at least two developers
5. The main branch stores all the merged code, and the development branch can be named according to different app functionalities.
6. Tag each sprint, and name the tag in the following format:  
*2023_SM2_AQ_Bluering_BL_SPRINT1*
