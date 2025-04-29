# A Tool for Managing Student Software Projects

## Information about this repository

This is the repository that you are going to use **individually** for developing your project. Please use the resources provided in the module to learn about **plagiarism** and how plagiarism awareness can foster your learning.

Regarding the use of this repository, once a feature (or part of it) is developed and **working** or parts of your system are integrated and **working**, define a commit and push it to the remote repository. You may find yourself making a commit after a productive hour of work (or even after 20 minutes!), for example. Choose commit message wisely and be concise.

Please choose the structure of the contents of this repository that suits the needs of your project but do indicate in this file where the main software artefacts are located.

# List of project files :

src/main/java/
controller/         # Spring Controllers (e.g., StudentController.java)
model/              # JPA Entity Models 
repository/         # Repository interfaces
service/            # Business logic layer
dto/                # Form/DTO objects
config/             # Security and app config

src/main/resources/
templates/          # Thymeleaf views
static/             # CSS & other static assets
application.properties # Main config file

# How to install and run

Need to ensure that you have these in order to run the program:

-Java 17 or later
-MySQL Server installed
-Gradle (or you can use included gradlew wrapper)


To install 

Option 1: Just download the repository as a zip folder and store where you want. 

Option 2: you can clone my repository into a folder or your choice. You can do this by using the command 

git clone https://campus.cs.le.ac.uk/gitlab/ug_project/24-25/ta301.git (This is the link to repository for me. It might be different for you)

Once installed ensure that you have a MySQl database. If you don't already have one, create a new MySQL database. 
Then configure the scr/main/resources/application.properties file in order to match your database:

spring.datasource.url=jdbc:mysql://localhost:3306/[your database server]
spring.datasource.username=[your SQL user name]
spring.datasource.password=[your SQL user password]

To run

Option 1: To run the project directly using Gradle in command prompt:

First navigate to the folder you cloned the repository to
Then in the command prompt and then type in:

gradlew bootRun

Option 2: To run the project using an IDE(like IntelliJ IDEA or Eclipse):

Open the project as a Gradle project.
Make sure the correct Java SDK (Java 17) is set.
Run the AToolforManagingStudentSoftwareProjectsApplication.java file (it contains the main method) directly.

The application should start and be available at http://localhost:8080/home . If you try to access any other page instead of the default homepage you will be re-directed to login which is still fine as you can carry on from there.

# Operating System Requirements

This project has been developed and tested specifically on Windows 11. 
It is recommended to use Windows 11 for the smoothest experience. Other operating systems may work if Java 17+ and Gradle are properly installed, but I have not formally tested them.



# Additional information

There is some test data in \src\main\java\com\example\atoolformanagingstudentsoftwareprojects\AToolforManagingStudentSoftwareProjectsApplication .
It is commented out and is recommended to be only ran once to create new students. Running more than once can create students with duplicate usernames as we are injecting the data into the database.
Once it is run once it can be commented out again or can be deleted. There is 2 types of student test data, one with preferences and one without. It is recommended to test with the students with preferences first then add the students with no preferences.
Convenors will have to be created manually through the register system on the webpage.


If you need help running the program, feel free to contact me through my university email:

ta301@student.le.ac.uk